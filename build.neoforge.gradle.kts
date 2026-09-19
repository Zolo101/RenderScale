plugins {
    id("mod-platform")
    id("net.neoforged.moddev")
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)

    // TODO: Do we need this...??

    // Match the Minecraft type only, preserving names such as Embeddium's OptionIdentifier.
    replacements.regex(current.parsed >= "1.21.11") {
        replace("""\bResourceLocation\b""", "Identifier", """\bIdentifier\b""", "ResourceLocation")
    }

    replacements.string(current.parsed >= "1.21.11") {
        replace("location()", "identifier()")
    }
}

platform {
    loader = "neoforge"
    dependencies {
        required("minecraft") {
            forgeLikeVersionRange = prop("deps.minecraft")
        }
        required("neoforge") {
            forgeLikeVersionRange.set("[1,)")
        }
        // WHY is it cloth_config in forge but cloth-config in fabric?????
        required("cloth_config") {
            slug("cloth-config")
            forgeLikeVersionRange = prop("deps.cloth_config")
        }
        optional("iris") {
//            slug("iris")
            forgeLikeVersionRange = prop("deps.iris")
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

neoForge {
    version = property("deps.neoforge") as String
    accessTransformers.from(rootProject.file("src/main/resources/aw/${sc.current.version}.cfg"))
    validateAccessTransformers = true

    if (hasProperty("deps.parchment")) parchment {
        val (mc, ver) = (property("deps.parchment") as String).split(':')
        mappingsVersion = ver
        minecraftVersion = mc
    }

    runs {
        register("client") {
            client()
            gameDirectory = file("run/")
            ideName = "NeoForge Client (${sc.active?.version})"
            programArgument("--username=Dev")

            // mixin.debug countInjections causes iris to crash for
            // what I believe is because we're using compileOnly iris-fabric in deps...
            jvmArgument("-Dmixin.debug.verbose=true")
            jvmArgument("-Dmixin.debug.export=true")
            jvmArgument("-Dmixin.checks.interfaces=true")
        }
        // Screenshot autotest, see RenderScaleAutoTest and scripts/run-gametests.sh
        if (sc.eval(sc.current.version, "<1.21.4")) {
            register("clientAutotest") {
                client()
                gameDirectory = file("run-gametest/")
                ideName = "NeoForge Client Autotest (${sc.active?.version})"
                jvmArgument("-Drenderscale.autotest")
                jvmArgument("-Xmx2G")
            }
        }
//        register("server") {
//            server()
//            gameDirectory = file("run/")
//            ideName = "NeoForge Server (${sc.active?.version})"
//        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("${rootDir}/versions/datagen/${stonecutter.current.version.split("-")[0]}/src/main/generated")
}

repositories {
    mavenCentral()
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
    strictMaven("https://maven.shedaniel.me/", "me.shedaniel.cloth") { name = "Shedaniel" }
    strictMaven("https://maven.caffeinemc.net/releases") { name = "CaffeineMC" }
}

dependencies {
//    implementation(libs.moulberry.mixinconstraints)
//    jarJar(libs.moulberry.mixinconstraints)

    api("me.shedaniel.cloth:cloth-config-neoforge:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    // sodium >0.8, so 1.21.11+ and 1.21.1
    if (hasProperty("deps.sodium")) {
        implementation("net.caffeinemc:sodium-neoforge-api:${prop("deps.sodium")}")
    }

//    compileOnly("maven.modrinth:iris:${property("deps.iris")}-neoforge")

    // iris releases faster on fabric, this doesn't seem to cause any problems using it in neoforge
    compileOnly("maven.modrinth:iris:${property("deps.iris")}-fabric")

    // for testing
//    try {
////        prop("deps.kubejs")
//        implementation("maven.modrinth:sodium:5EEI3Guz")
//        implementation("maven.modrinth:kubejs:F2nzeC19")
//        implementation("maven.modrinth:rhino:SqkDvOLG")
//    } catch (e: ExtraPropertiesExtension.UnknownPropertyException) {}

}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}
