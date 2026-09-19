import dev.kikugie.fletching_table.extension.FletchingTableExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.expand
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import javax.inject.Inject

abstract class GenerateModManifestTask : DefaultTask() {
    @get:Input
    abstract val content: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(content.get())
    }
}

abstract class MinecraftArtifactsLock : BuildService<BuildServiceParameters.None>

abstract class ModPlatformPlugin @Inject constructor() : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val inferredLoader = Loader.of(project.buildFile.name.substringAfter('.').replace(".gradle.kts", ""))

        val extension = extensions.create("platform", ModPlatformExtension::class.java).apply {
            loader.convention(inferredLoader.id)
        }

        when (inferredLoader) {
            is Loader.Fabric -> {
                extension.jarTask.convention(providers.provider {
                    extensions.getByType<dev.kikugie.loomx.LoomCompatProjectExtension>().modJar.name
                })
                extension.sourcesJarTask.convention(providers.provider {
                    extensions.getByType<dev.kikugie.loomx.LoomCompatProjectExtension>().modSourcesJar.name
                })
            }

            is Loader.Forge -> {
                extension.jarTask.convention("reobfJar")
                extension.sourcesJarTask.convention("sourcesJar")
            }

            else -> {
                extension.jarTask.convention("jar")
                extension.sourcesJarTask.convention("sourcesJar")
            }
        }

        listOf(
            "org.jetbrains.kotlin.jvm",
            "com.google.devtools.ksp",
            "dev.kikugie.fletching-table",
            "me.modmuss50.mod-publish-plugin"
        ).forEach {
            apply(
                plugin = it
            )
        }

        // Publishing reads version-specific properties during plugin application,
        // before the platform build script can configure these tags.
        project.sc.properties.tags(project.sc.current.version, inferredLoader.id)

        val ctx = Context(
            project = this,
            extension = extension,
            loader = Loader.of(extension.loader.get()),
            stonecutter = project.sc
        )
        configureProject(ctx)
    }

    private fun Project.configureProject(ctx: Context) {
        version = ctx.fullVersion

        listOf("java", "idea").forEach { apply(plugin = it) }
        ctx.extension.requiredJava.set(ctx.javaVersion)

        if (ctx.loader.isFabricLike) {
            ctx.extension.dependencies {
                required("java") { fabricLikeVersionRange = ">=${ctx.javaVersion.majorVersion}" }
            }
        }

        configureFletchingTable(ctx)
        registerGenerateManifestTask(ctx)
        configureJarTask(ctx)
        configureIdea()
        configureProcessResources(ctx)
        configureJava(ctx)
        configureStandaloneTests(ctx)
        registerBuildAndCollectTask(ctx)
        limitMinecraftArtifactGenerationConcurrency()

        configureModPublishing(ctx)

        if (envTrue("PUB_MAVEN_ENABLE")) {
            configureMavenPublishing(ctx)
        }
    }

    private fun Project.limitMinecraftArtifactGenerationConcurrency() {
        val lock = gradle.sharedServices.registerIfAbsent(
            "minecraftArtifactsLock",
            MinecraftArtifactsLock::class.java
        ) {
            maxParallelUsages.set(2)
        }
        tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
            usesService(lock)
        }
    }

    private fun Project.configureJava(ctx: Context) {
        extensions.configure<JavaPluginExtension>("java") {
            withSourcesJar()
            withJavadocJar()
            sourceCompatibility = ctx.javaVersion
            targetCompatibility = ctx.javaVersion
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(ctx.javaVersion.majorVersion))
            }
        }
    }

    private fun Project.configureStandaloneTests(ctx: Context) {
        val testSources = the<JavaPluginExtension>().sourceSets.named("test")
        val launcher = extensions.getByType<JavaToolchainService>().launcherFor {
            languageVersion.set(JavaLanguageVersion.of(ctx.javaVersion.majorVersion))
        }
        val controllerTest = tasks.register<JavaExec>("testDynamicScale") {
            group = "verification"
            description = "Run the standalone Dynamic Scale controller assertions"
            dependsOn("testClasses")
            classpath = testSources.get().runtimeClasspath
            mainClass.set("dev.zelo.renderscale.DynamicScaleControllerTest")
            javaLauncher.set(launcher)
        }
        tasks.named<Test>("test") {
            // The existing controller test uses main(), rather than a JUnit test engine.
            // Run its assertions explicitly; Gradle 9 otherwise fails test discovery.
            dependsOn(controllerTest)
            failOnNoDiscoveredTests.set(false)
        }
    }

    private fun Project.registerGenerateManifestTask(ctx: Context) {
        val manifestOutputDir = layout.buildDirectory.dir("generated/modManifest")
        val generateTask = tasks.register<GenerateModManifestTask>("generateModManifest") {
            content.set(ctx.loader.generateManifest(ctx))
            outputFile.set(layout.buildDirectory.file("generated/modManifest/${ctx.loader.modManifestPath}"))
        }

        the<JavaPluginExtension>().sourceSets.named("main") { resources.srcDir(manifestOutputDir) }
        tasks.named<ProcessResources>("processResources") { dependsOn(generateTask) }
    }

    private fun Project.configureProcessResources(ctx: Context) {
        tasks.matching { it.name == "kspKotlin" }.configureEach {
            dependsOn("stonecutterGenerate")
        }
        val javaVersionValue = "JAVA_${ctx.javaVersion.majorVersion}"
        val excluded = ctx.loader.excludedResources
        tasks.named<ProcessResources>("processResources") {
            dependsOn(tasks.named("stonecutterGenerate"), "kspKotlin")
            filesMatching("*.mixins.json") {
                expand("java" to javaVersionValue)
            }
            if (ctx.loader is Loader.Forge) {
                from(rootProject.file("src/main/resources/aw/${ctx.currentMcVersion}.cfg")) {
                    into("META-INF")
                    rename { "accesstransformer.cfg" }
                }
            }
            // #moj_import doesn't exist before 1.21.6 so we gotta hide em
            // but fsr shaders are 1.21.11 anyway so
            if (ctx.stonecutter.eval(ctx.currentMcVersion, "<1.21.11")) {
                exclude("assets/${ctx.modId}/shaders/**")
            }
            // TODO: Perhaps use stonecutter to replace it
            if (ctx.stonecutter.eval(ctx.currentMcVersion, ">=26.3")) {
                filesMatching("assets/${ctx.modId}/shaders/core/*.fsh") {
                    // RenderPearl compiles both backends with ShaderC.
                    filter { line: String ->
                        line.replace("#moj_import", "#include")
                            .replace("out vec4 fragColor;", "layout(location = 0) out vec4 fragColor;")
                    }
                }
            }
            exclude(excluded)
        }
    }

    private fun Project.configureJarTask(ctx: Context) {
        val generateTask = tasks.named("generateModManifest")
        tasks.withType<Jar>().configureEach {
            archiveBaseName.set(ctx.modId)
            dependsOn(generateTask)
            if (ctx.loader is Loader.Forge) {
                manifest.attributes(ctx.loader.mixinConfigAttribute to "${ctx.modId}.mixins.json")
            }
        }
    }

    private fun Project.configureIdea() {
        extensions.configure<IdeaModel>("idea") {
            module {
                isDownloadJavadoc = true
                isDownloadSources = true
            }
        }
    }

    private fun Project.configureFletchingTable(ctx: Context) {
        extensions.configure<FletchingTableExtension> {
            mixins.create("main") { mixin("default", "${ctx.modId}.mixins.json") }
            j52j.register("main") { extension("json", "**/*.json5") }
        }
    }

    private fun Project.registerBuildAndCollectTask(ctx: Context) {
        tasks.register<Copy>("buildAndCollect") {
            from(
                tasks.named(ctx.extension.jarTask.get()),
                tasks.named(ctx.extension.sourcesJarTask.get()),
                tasks.named("javadocJar")
            )
            into(rootProject.layout.buildDirectory.file("libs/${ctx.basicVersion}"))
            dependsOn("build")
            group = "build"
        }
    }
}
