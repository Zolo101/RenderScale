import org.gradle.kotlin.dsl.support.uppercaseFirstChar

//plugins {
//    // see https://fabricmc.net/develop/ for new versions
//    id 'fabric-loom' version '1.10-SNAPSHOT' apply false
//    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
//    id 'net.neoforged.moddev' version '2.0.78' apply false
//}

plugins {
    id("dev.isxander.modstitch.base") version "0.7.1-unstable"
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
    ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String
val loaderName: String = name.split("-")[1]

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
    javaVersion = when (minecraft) {
        "1.20.1" -> 17
        "1.21.6" -> 21
        else -> throw IllegalArgumentException("Please store the java version for ${property("deps.minecraft")} in build.gradle.kts!")
    }

    // If parchment doesnt exist for a version yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    // https://parchmentmc.org/docs/getting-started
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "renderscale"
        modName = "RenderScale"
        modVersion = "1.3.2"
        modGroup = "dev.zelo"
        modAuthor = "Zelo101"
        modLicense = "MIT"
        modDescription = "RenderScale allows you to change Minecraft's render resolution separately from the HUD elements."

        fun <K, V> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("fabric_loader_version", "0.16.14")
            put("neoforge_version", "21.6.5-beta")
            put("forge_version", "51.0.17")
            put("minecraft_version_range", "[1.21.6]")
            put("java", "21")

            put("cloth_config_version_range", property("deps.cloth_config") as String)
            put("mod_issue_tracker", "https://github.com/Zolo101/RenderScale/issues")
            // https://minecraft.wiki/w/Pack_format#List_of_pack_formats
            put("aw_file", when (property("deps.minecraft")) {
                "1.21.6" -> "1.21"
                else -> throw IllegalArgumentException("Couldn't find a access widener file for ${property("deps.minecraft")}! Please add it to build.gradle.kts!")
            })
            put("pack_format", when (property("deps.minecraft")) {
                "1.20.1" -> 15
                "1.21.6" -> 80
                else -> throw IllegalArgumentException("Please store the resource pack version for ${property("deps.minecraft")} in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            }.toString())
            put("loader", loaderName.uppercaseFirstChar())
        }
    }

    // Fabric Loom (Fabric)
    loom {
        // It's not recommended to store the Fabric Loader version in properties.
        // Make sure its up to date.
        // https://fabricmc.net/develop
        fabricLoaderVersion = "0.18.1"

        // Configure loom like normal in this block.
        configureLoom {
            accessWidenerPath = rootProject.file("./src/main/resources/accesswideners/1.21.accesswidener")
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        prop("deps.forge") { forgeVersion = it }
        prop("deps.neoform") { neoFormVersion = it }
        prop("deps.neoforge") { neoForgeVersion = it }
        prop("deps.mcp") { mcpVersion = it }
//        enable {
//        }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns(server = false)

        // https://projects.neoforged.net/neoforged/neoforge
//        neoForgeVersion = "21.1.216"

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here

        configureNeoForge {
            runs.all {
                disableIdeRun()
            }
        }

        tasks.named("stonecutterGenerate") {
            dependsOn("createMinecraftArtifacts")
        }
    }

    mixin {
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = true

        configs.register("renderscale")

        // Most of the time you wont ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        // if (isLoom) configs.register("examplemod-fabric")
        // if (isModDevGradleRegular) configs.register("examplemod-neoforge")
        // if (isModDevGradleLegacy) configs.register("examplemod-forge")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
stonecutter {
    constants {
        val loader: String = current.project.substringAfter('-')
        /* Same as
           put("fabric", loader == "fabric")
           put("forge", loader == "forge")
           put("neoforge", loader == "neoforge")*/
        match(loader, "fabric", "forge", "neoforge")
        put("vanilla", false)
    }
}


// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
dependencies {
    // Fabric only
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}+${property("deps.minecraft")}")

        // Mod menu
        modstitchModApi("com.terraformersmc:modmenu:${property("deps.modmenu")}")

        // config API for mod menu
        modstitchModImplementation("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
            exclude("net.fabricmc.fabric-api")
        }

    }

    modstitch.moddevgradle {
        if (modstitch.isModDevGradleLegacy) {
            // forge
        } else {
            modstitchModImplementation("me.shedaniel.cloth:cloth-config-neoforge:${property("deps.cloth_config")}") {
                exclude("net.fabricmc.fabric-api")
            }
        }
    }

    // TODO: This wont work for 1.20.1 forge
    // For iris related compatibility
    modstitchModCompileOnly("maven.modrinth:iris:${property("deps.iris")}+${property("deps.minecraft")}-fabric")

    // Anything else in the dependencies block will be used for all platforms.
}

tasks.withType<JavaCompile> {
    dependsOn("stonecutterGenerate")
}