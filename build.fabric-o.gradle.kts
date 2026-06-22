plugins {
    id("mod-platform")
    id("net.fabricmc.fabric-loom-remap") // 1.21.11 and lower
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)

    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
    }

    replacements.string(current.parsed >= "26.1.2") {
        replace("FabricDataOutput", "FabricPackOutput")
    }
}

platform {
    loader = "fabric-o"
    dependencies {
        // TODO: Remove minecraft dependency? Sodium & Iris do it
        required("minecraft") {
            fabricLikeVersionRange = prop("deps.minecraft")
        }
        required("fabric-api") {
            slug("fabric-api")
            fabricLikeVersionRange = ">=${prop("deps.fabric-api")}"
        }
        required("fabricloader") {
            fabricLikeVersionRange = ">=${prop("deps.fabric-loader")}"
        }
        required("cloth-config") {
            slug("cloth-config")
            fabricLikeVersionRange = ">=${prop("deps.cloth_config")}"
        }
        optional("iris") {
//            slug("iris")
            fabricLikeVersionRange = ">=${prop("deps.iris")}"
        }
        optional("modmenu") {}

        incompatible("resolutioncontrol-plus-plus") {}
        incompatible("resolutioncontrol-plus") {}
        incompatible("resolutioncontrol") {}
    }
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/aw/${sc.current.version}.accesswidener")
    runs.named("client") {
        client()
        ideConfigGenerated(true)
        runDir = "run/"
        environment = "client"
        programArgs("--username=Dev")
        configName = "Fabric Client"
    }
//    runs.named("server") {
//        server()
//        ideConfigGenerated(true)
//        runDir = "run/"
//        environment = "server"
//        configName = "Fabric Server"
//    }
}

fabricApi {
    configureDataGeneration {
        outputDirectory = file("${rootDir}/versions/datagen/${sc.current.version.split("-")[0]}/src/main/generated")
        client = true
    }
}

repositories {
    mavenCentral()
    strictMaven("https://maven.terraformersmc.com/", "com.terraformersmc") { name = "TerraformersMC" }
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
    strictMaven("https://maven.shedaniel.me/", "me.shedaniel.cloth") { name = "Shedaniel" }
    strictMaven("https://maven.caffeinemc.net/releases") { name = "CaffeineMC" }
}

configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")

    mappings(
        loom.layered {
            officialMojangMappings()
            if (hasProperty("deps.parchment")) parchment("org.parchmentmc.data:parchment-${prop("deps.parchment")}@zip")
        })

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
//    implementation(libs.moulberry.mixinconstraints)
//    include(libs.moulberry.mixinconstraints)
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
    modImplementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

    // config API
    modApi("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    // sodium >0.8, so 1.21.11+ and 1.21.1
    try {
        prop("deps.sodium")
        modImplementation("net.caffeinemc:sodium-fabric-api:${prop("deps.sodium")}+mc${prop("deps.minecraft")}")
    } catch (e: ExtraPropertiesExtension.UnknownPropertyException) {}

    //    modLocalRuntime("maven.modrinth:sodium:${property("deps.sodium")}-fabric")
    //    modLocalRuntime("maven.modrinth:iris:${property("deps.iris")}-fabric")
    modCompileOnly("maven.modrinth:iris:${property("deps.iris")}-fabric")
}