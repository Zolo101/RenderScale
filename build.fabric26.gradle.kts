plugins {
    id("mod-platform")
    id("net.fabricmc.fabric-loom")
}

platform {
    loader = "fabric"
    dependencies {
        // TODO: Remove minecraft dependency? Sodium & Iris do it
        required("minecraft") {
//            versionRange = "${prop("deps.minecraft")},${prop("publish.additionalVersions")}"
            versionRange = ">=26.1-"
        }
        required("fabric-api") {
            slug("fabric-api")
            versionRange = ">=${prop("deps.fabric-api")}"
        }
        required("fabricloader") {
            versionRange = ">=${libs.fabric.loader.get().version}"
        }
        required("cloth-config") {
            slug("cloth-config")
            versionRange = ">=${prop("deps.cloth_config")}"
        }
        optional("iris") {
//            slug("iris")
            versionRange = ">=${prop("deps.iris")}"
        }
        optional("modmenu") {}
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
        outputDirectory = file("${rootDir}/versions/datagen/${stonecutter.current.version.split("-")[0]}/src/main/generated")
        client = true
    }
}

repositories {
    mavenCentral()
    strictMaven("https://maven.terraformersmc.com/", "com.terraformersmc") { name = "TerraformersMC" }
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
    strictMaven("https://maven.shedaniel.me/", "me.shedaniel.cloth") { name = "Shedaniel" }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")

    implementation(libs.fabric.loader)
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")

    implementation(libs.moulberry.mixinconstraints)
    include(libs.moulberry.mixinconstraints)
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
    implementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

    // config API for mod menu
    api("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    //    modLocalRuntime("maven.modrinth:sodium:${property("deps.sodium")}-fabric")
    //    modLocalRuntime("maven.modrinth:iris:${property("deps.iris")}-fabric")
//    ("maven.modrinth:sodium:${property("deps.sodium")}-fabric")
    compileOnly("maven.modrinth:iris:${property("deps.iris")}-fabric")
}

stonecutter {
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
    }
}