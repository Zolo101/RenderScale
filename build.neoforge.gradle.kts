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
        required("config-config-neoforge") {
            forgeVersionRange = "[${prop("deps.cloth_config")}+${prop("deps.minecraft")}-neoforge]"
        }
        optional("iris") {
            forgeVersionRange = "[${prop("deps.iris")}+${prop("deps.minecraft")}-neoforge]"
        }
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
    implementation("maven.modrinth:iris:${property("deps.iris")}-neoforge")

    api("me.shedaniel.cloth:cloth-config-neoforge:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

stonecutter {
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")

        // AutoConfig -> AutoConfigClient (deprecation)
//        replace("AutoConfig", "AutoConfigClient")
    }
}


//modstitch {
//    minecraftVersion = minecraft
//
//    // Alternatively use stonecutter.eval if you have a lot of versions to target.
//    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
//    javaVersion = when {
//        minecraft.startsWith("1.20") -> 17
//        minecraft.startsWith("1.21") -> 21
//        else -> throw IllegalArgumentException("Please store the java version for ${property("deps.minecraft")} in build.gradle.kts!")
//    }
//
//    // If parchment doesnt exist for a version yet you can safely
//    // omit the "deps.parchment" property from your versioned gradle.properties
//    // https://parchmentmc.org/docs/getting-started
//    parchment {
//        prop("deps.parchment") { mappingsVersion = it }
//    }
//
//    // This metadata is used to fill out the information inside
//    // the metadata files found in the templates folder.
//    metadata {
//        modId = "renderscale"
//        modName = "RenderScale"
//        modVersion = "1.3.7"
//        modGroup = "dev.zelo"
//        modAuthor = "Zelo101"
//        modLicense = "MIT"
//        modDescription = "RenderScale allows you to change Minecraft's render resolution separately from the HUD elements."
//
//        fun <K: Any, V: Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
//            block()
//        }
//
//        replacementProperties.populate {
//            // You can put any other replacement properties/metadata here that
//            // modstitch doesn't initially support. Some examples below.
//            if (loaderName == "fabric") {
//                put("fabric_loader_version", "0.18.4")
//                put("fabric_api_version", property("deps.fabric_api") as String)
//            }
//            if (loaderName == "neoforge") {
//                put("neoforge_version", property("deps.neoforge") as String)
//            }
//            if (loaderName == "forge") {
//                put("forge_version", property("deps.forge") as String)
////                put("forge_mcp_version", property("deps.mcp") as String)
//            }
//            put("fabric_minecraft_version_range", when (property("deps.minecraft")) {
//                "1.20.1" -> "1.20.1"
//                "1.21.4" -> "1.21.4"
//                "1.21.5" -> "1.21.5"
//                "1.21.6" -> ">=1.21.6 <=1.21.8"
//                "1.21.9" -> ">=1.21.9 <=1.21.10"
//                "1.21.11" -> "1.21.11"
//                else -> throw IllegalArgumentException("Please store the minecraft version range for ${property("deps.minecraft")} in build.gradle.kts!")
//            })
//            put("neoforge_minecraft_version_range", when (property("deps.minecraft")) {
//                "1.20.1" -> "[1.20.1]"
//                "1.21.4" -> "[1.21.4]"
//                "1.21.5" -> "[1.21.5]"
//                "1.21.6" -> "[1.21.6, 1.21.8]"
//                "1.21.9" -> "[1.21.9, 1.21.10]"
//                "1.21.11" -> "[1.21.11]"
//                else -> throw IllegalArgumentException("Please store the minecraft version range for ${property("deps.minecraft")} in build.gradle.kts!")
//            })
//            put("java", "21")
//
//            put("cloth_config_version_range", property("deps.cloth_config") as String)
//            put("mod_issue_tracker", "https://github.com/Zolo101/RenderScale/issues")
//            // https://minecraft.wiki/w/Pack_format#List_of_pack_formats
//            put("aw_file", when {
//                property("deps.minecraft").toString().startsWith("1.20") -> "1.20"
//                property("deps.minecraft").toString().startsWith("1.21") -> "1.21"
//                else -> throw IllegalArgumentException("Couldn't find a access widener file for ${property("deps.minecraft")}! Please add it to build.gradle.kts!")
//            })
//            put("pack_format", when (property("deps.minecraft")) {
//                "1.20.1" -> 15
//                "1.21.1" -> 48
//                "1.21.2" -> 57
//                "1.21.3" -> 57
//                "1.21.4" -> 61
//                "1.21.5" -> 71
//                "1.21.6" -> 80
//                "1.21.7" -> 81
//                "1.21.8" -> 81
//                "1.21.9" -> 88.0
//                "1.21.10" -> 88.0
//                "1.21.11" -> 94.1
//                else -> throw IllegalArgumentException("Please store the resource pack version for ${property("deps.minecraft")} in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
//            }.toString())
//            put("loader", loaderName)//.uppercaseFirstChar())
//        }
//    }
//
//    // Fabric Loom (Fabric)
//    loom {
//        // It's not recommended to store the Fabric Loader version in properties.
//        // Make sure its up to date.
//        // https://fabricmc.net/develop
//        fabricLoaderVersion = "0.18.4"
//
//        // Configure loom like normal in this block.
//        //configureLoom {
//        //    accessWidenerPath = rootProject.file("./src/main/resources/accesswideners/1.21.accesswidener")
//        //}
//    }
//
//    // ModDevGradle (NeoForge, Forge, Forgelike)
//    moddevgradle {
//        prop("deps.forge") { forgeVersion = it }
//        prop("deps.neoform") { neoFormVersion = it }
//        prop("deps.neoforge") { neoForgeVersion = it }
//        prop("deps.mcp") { mcpVersion = it }
////        enable {
////        }
//
//        // Configures client and server runs for MDG, it is not done by default
//        defaultRuns(server = false)
//
//        // https://projects.neoforged.net/neoforged/neoforge
////        neoForgeVersion = "21.1.216"
//
//
////        accessTransformers.from(rootProject.file("./src/main/resources/accesstransformers/1.21.cfg"))
//        // This block configures the `neoforge` extension that MDG exposes by default,
//        // you can configure MDG like normal from here
//        configureNeoForge {
////            accessTransformers.from(rootProject.file("./src/main/resources/accesstransformers/1.21.cfg"))
//
//            runs.all {
//                disableIdeRun()
//            }
//        }
//
//        tasks.named("stonecutterGenerate") {
//            dependsOn("createMinecraftArtifacts")
//        }
//
//
//    }
//
//    mixin {
//        // You do not need to specify mixins in any mods.json/toml file if this is set to
//        // true, it will automatically be generated.
//        addMixinsToModManifest = true
//
//        configs.register("renderscale")
//
//        // Most of the time you wont ever need loader specific mixins.
//        // If you do, simply make the mixin file and add it like so for the respective loader:
//        // if (isLoom) configs.register("examplemod-fabric")
//        // if (isModDevGradleRegular) configs.register("examplemod-neoforge")
//        // if (isModDevGradleLegacy) configs.register("examplemod-forge")
//    }
//}
//
//// Stonecutter constants for mod loaders.
//// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
//stonecutter {
//    constants {
//        val loader: String = current.project.substringAfter('-')
//        /* Same as
//           put("fabric", loader == "fabric")
//           put("forge", loader == "forge")
//           put("neoforge", loader == "neoforge")*/
//        match(loader, "fabric", "forge", "neoforge")
//        put("vanilla", false)
//    }
//}
//
//
//// All dependencies should be specified through modstitch's proxy configuration.
//// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
//// If you want to create proxy configurations for more source sets, such as client source sets,
//// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
//dependencies {
//    // Fabric only
//    modstitch.loom {
////        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}+${property("deps.minecraft")}")
//
//
//        // Mod menu
//        modstitchModApi("com.terraformersmc:modmenu:${property("deps.modmenu")}")
//
//        // config API for mod menu
////        modstitchModImplementation("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
////            exclude("net.fabricmc.fabric-api")
////        }
//
//        modstitchModCompileOnly("maven.modrinth:iris:${property("deps.iris")}+${property("deps.minecraft")}-fabric")
//    }
//
//    modstitch.moddevgradle {
//        if (modstitch.isModDevGradleLegacy) {
//            // forge
//                modstitchModImplementation("me.shedaniel.cloth:cloth-config-forge:${property("deps.cloth_config")}") {
//                    exclude("net.fabricmc.fabric-api")
//                }
//        } else {
//            modstitchModCompileOnly("maven.modrinth:iris:${property("deps.iris")}+${property("deps.minecraft")}-fabric")
//
//            modstitchModImplementation("me.shedaniel.cloth:cloth-config-neoforge:${property("deps.cloth_config")}") {
//                exclude("net.fabricmc.fabric-api")
//            }
//        }
//    }
//
//    // TODO: This wont work for 1.20.1 forge
//    // For iris related compatibility
//
//
//    // Anything else in the dependencies block will be used for all platforms.
//}
//
//tasks.withType<JavaCompile> {
//    dependsOn("stonecutterGenerate")
//}