plugins {
    id("mod-platform")
    id("net.neoforged.moddev")
}

platform {
    loader = "neoforge"
    dependencies {
        required("minecraft") {
            forgeVersionRange = "[${prop("deps.minecraft")}]"
        }
        required("neoforge") {
            forgeVersionRange = "[1,)"
        }
        // WHY is it cloth_config in forge but cloth-config in fabric?????
        required("cloth_config") {
//            slug("cloth_config")
            forgeVersionRange = "[${prop("deps.cloth_config")},)"
        }
        optional("iris") {
//            slug("iris")
            forgeVersionRange = "[${prop("deps.iris")},)"
        }

        incompatible("resolutioncontrol-plus-plus") {}
        incompatible("resolutioncontrol-plus") {}
        incompatible("resolutioncontrol") {}
    }
}

neoForge {
    version = property("deps.neoforge") as String
    accessTransformers.from(rootProject.file("src/main/resources/aw/${stonecutter.current.version}.cfg"))
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
            ideName = "NeoForge Client (${stonecutter.active?.version})"
            programArgument("--username=Dev")
        }
//        register("server") {
//            server()
//            gameDirectory = file("run/")
//            ideName = "NeoForge Server (${stonecutter.active?.version})"
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
}

dependencies {
    implementation(libs.moulberry.mixinconstraints)
    jarJar(libs.moulberry.mixinconstraints)

    api("me.shedaniel.cloth:cloth-config-neoforge:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    compileOnly("maven.modrinth:iris:${property("deps.iris")}-neoforge")
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

stonecutter {
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
    }
}