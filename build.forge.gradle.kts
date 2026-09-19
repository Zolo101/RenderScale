plugins {
    id("mod-platform")
    id("net.neoforged.moddev.legacyforge")
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)

    // Match the Minecraft type only, preserving names such as Embeddium's OptionIdentifier.
    replacements.regex(current.parsed >= "1.21.11") {
        replace("""\bResourceLocation\b""", "Identifier", """\bIdentifier\b""", "ResourceLocation")
    }

    replacements.string(current.parsed >= "1.21.11") {
        replace("location()", "identifier()")
    }
}


platform {
    loader = "forge"
    dependencies {
        required("minecraft") {
            forgeLikeVersionRange = prop("deps.minecraft")
        }
        required("forge") {
            forgeLikeVersionRange.set("[1,)")
        }
        required("cloth_config") {
            forgeLikeVersionRange = prop("deps.cloth_config")
        }

        // Mods that do the same thing as RenderScale,
        // sometimes people add multiple of these which obviously causes massive bugs due to conflicts
        incompatible("resolutioncontrol-plus-plus") {
            forgeLikeVersionRange.set("[0,)")
        }

        incompatible("resolutioncontrol-plus") {
            forgeLikeVersionRange.set("[0,)")
        }

        incompatible("resolutioncontrol") {
            forgeLikeVersionRange.set("[0,)")
        }

        incompatible("superresolution") {
            forgeLikeVersionRange.set("[0,)")
        }
    }
}

legacyForge {
    version = "${property("deps.minecraft")}-${property("deps.forge")}"
    accessTransformers.from(rootProject.file("src/main/resources/aw/${sc.current.version}.cfg"))
    validateAccessTransformers = true


    runs {
        register("client") {
            client()
            gameDirectory = file("run/")
            ideName = "Forge Client (${sc.active?.version})"
            programArgument("--username=Dev")
        }
        // Screenshot autotest, see RenderScaleAutoTest and scripts/run-gametests.sh
        register("clientAutotest") {
            client()
            gameDirectory = file("run-gametest/")
            ideName = "Forge Client Autotest (${sc.active?.version})"
            jvmArgument("-Drenderscale.autotest")
            jvmArgument("-Xmx2G")
        }
//        register("server") {
//            server()
//            gameDirectory = file("run/")
//            ideName = "Forge Server (${sc.active?.version})"
//        }
    }


    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets.main.get(), "${prop("mod.id")}.mixins.refmap.json")
    config("${prop("mod.id")}.mixins.json")
}

repositories {
    mavenCentral()
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
    strictMaven("https://maven.shedaniel.me/", "me.shedaniel.cloth") { name = "Shedaniel" }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:${libs.versions.mixin.get()}:processor")

//    implementation(libs.moulberry.mixinconstraints)
//    jarJar(libs.moulberry.mixinconstraints)

    // mixin extras
    annotationProcessor(libs.mixinextras.common)
    compileOnly(libs.mixinextras.common)
    implementation(libs.mixinextras.forge)
    jarJar(libs.mixinextras.forge)

    modApi("me.shedaniel.cloth:cloth-config-forge:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    // Optional options-screen integration; Embeddium is not bundled or required.
    modCompileOnly("maven.modrinth:embeddium:0.3.31+mc1.20.1")
    modCompileOnly("maven.modrinth:oculus:1.20.1-1.8.0")

    // for testing and that
    modRuntimeOnly("maven.modrinth:embeddium:0.3.31+mc1.20.1")
    modRuntimeOnly("maven.modrinth:oculus:1.20.1-1.8.0")
    modRuntimeOnly("maven.modrinth:create:mc1.20.1-6.0.8")
}

sourceSets {
    main {
        resources.srcDir(
            "${rootDir}/versions/datagen/${sc.current.version.split("-")[0]}/src/main/generated"
        )
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}
