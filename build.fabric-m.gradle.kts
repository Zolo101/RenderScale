plugins {
    id("mod-platform")
    id("net.fabricmc.fabric-loom")
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
    loader = "fabric-m"
    dependencies {
        required("minecraft") {
            // If mojang makes a breaking change in a patch version we just have to thug it out #toobad
            fabricLikeVersionRange = "${prop("deps.minecraft")}.*"
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
    accessWidenerPath = rootProject.file("src/main/resources/aw/${stonecutter.current.version}.accesswidener")
    runs.named("client") {
        client()
        ideConfigGenerated(true)
        runDir = "run/"
        environment = "client"
        programArgs("--username=Dev")
        configName = "Fabric Client"
    }
    runs.named("server") {
        server()
        ideConfigGenerated(true)
        runDir = "run/"
        environment = "server"
        configName = "Fabric Server"
    }
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

dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
    implementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
//    implementation(libs.moulberry.mixinconstraints)
//    include(libs.moulberry.mixinconstraints)
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
//    localRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
    implementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

    // config API
    api("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    implementation("net.caffeinemc:sodium-fabric-api:${prop("deps.sodium")}")

    //    modLocalRuntime("maven.modrinth:sodium:${property("deps.sodium")}-fabric")
    //    modLocalRuntime("maven.modrinth:iris:${property("deps.iris")}-fabric")
    compileOnly("maven.modrinth:iris:${property("deps.iris")}-fabric")
}