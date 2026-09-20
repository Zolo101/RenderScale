plugins {
    id("mod-platform")
    id("dev.kikugie.loom-back-compat")
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

    replacements.string(current.parsed >= "26.1.2") {
        replace("FabricDataOutput", "FabricPackOutput")
    }
}

platform {
    loader = "fabric"
    dependencies {
        required("minecraft") {
            fabricLikeVersionRange = if (sc.current.parsed >= "26") "${prop("deps.minecraft")}.*" else prop("deps.minecraft")
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

        // Mods that do the same thing as RenderScale,
        // sometimes people add multiple of these which obviously causes massive bugs due to conflicts
        incompatible("resolutioncontrol-plus-plus") {}
        incompatible("resolutioncontrol-plus") {}
        incompatible("resolutioncontrol") {}
        incompatible("superresolution") {}
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
    // Client Gametest API only exists for 1.21.4+; older versions use the custom
    // autotest driver (RenderScaleAutoTest) which replicates the same test
    if (sc.eval(sc.current.version, ">=1.21.4")) {
        runs.register("clientGametest") {
            client()
            ideConfigGenerated(false)
            runDir = "run-gametest/"
            environment = "client"
            vmArg("-Dfabric.client.gametest")
            // Without a cap the JVM defaults to 1/4 of system RAM, which can get the
            // client OOM-killed when several versions run back to back
            vmArg("-Xmx2G")
            configName = "Fabric Client Gametest"
        }
    } else {
        runs.register("clientAutotest") {
            client()
            ideConfigGenerated(false)
            runDir = "run-gametest/"
            environment = "client"
            vmArg("-Drenderscale.autotest")
            vmArg("-Xmx2G")
            configName = "Fabric Client Autotest"
        }
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

    if (sc.current.parsed < "26") {
        mappings(loom.layered {
            officialMojangMappings()
            if (hasProperty("deps.parchment")) parchment("org.parchmentmc.data:parchment-${prop("deps.parchment")}@zip")
        })
    }

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
//    implementation(libs.moulberry.mixinconstraints)
//    include(libs.moulberry.mixinconstraints)
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}") {
        // fabric-api 0.112.2+1.21.4 ships client gametest module 1.0.1, which predates the
        // world builder API. Swap in the newer module (dev only, published deps unchanged).
        if (sc.eval(sc.current.version, "1.21.4")) {
            exclude(group = "net.fabricmc.fabric-api", module = "fabric-client-gametest-api-v1")
        }
        // 26.1's bundled module hangs on world close; retain the fixed dev module.
        if (sc.eval(sc.current.version, "26.1")) {
            exclude(group = "net.fabricmc.fabric-api", module = "fabric-client-gametest-api-v1")
        }
    }
    if (sc.eval(sc.current.version, "26.1")) {
        modImplementation("net.fabricmc.fabric-api:fabric-client-gametest-api-v1:5.1.0+0a5283664c")
    }
    if (sc.eval(sc.current.version, "1.21.4")) {
        modImplementation("net.fabricmc.fabric-api:fabric-client-gametest-api-v1:4.1.1+6816ccd404")
    }
    modImplementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

    // config API
    modApi("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    // sodium >0.8, so 1.21.11+ and 1.21.1
    if (hasProperty("deps.sodium")) {
        // compileOnly: the API stub jar declares mixin configs it doesn't contain,
        // so fabric-loader crashes if it ends up on the runtime classpath as a mod
//        val sodiumVersion = prop("deps.sodium") + if (sc.current.parsed < "26") "+mc${prop("deps.minecraft")}" else ""

        // make up your mind guys...
        modCompileOnly("net.caffeinemc:sodium-fabric-api:${prop("deps.sodium")}+mc${prop("deps.sodium_minecraft")}")
        modLocalRuntime("maven.modrinth:sodium:mc${prop("deps.sodium_minecraft")}-${prop("deps.sodium")}-fabric")
//        modLocalRuntime("maven.modrinth:iris:${property("deps.iris")}-fabric")
    }

    modCompileOnly("maven.modrinth:iris:${property("deps.iris")}-fabric")
}

// Game-test classes are dev-only: the fabric-client-gametest entrypoint is
// read by the gametest API in dev runs (which use the source set directly)
// and ignored by the loader in production. Keep the dead classes out of
// the shipped jar.
tasks.jar {
    exclude("dev/zelo/renderscale/gametest/**")
}
