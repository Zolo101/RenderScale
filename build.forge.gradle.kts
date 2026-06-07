plugins {
    id("mod-platform")
    id("net.neoforged.moddev.legacyforge")
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)

    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
    }
}


platform {
    loader = "forge"
    dependencies {
        required("minecraft") {
            forgeLikeVersionRange = "[${prop("deps.minecraft")}]"
        }
        required("forge") {
            forgeLikeVersionRange = "[1,)"
        }
        required("cloth-config-forge") {
            forgeLikeVersionRange = "[${prop("deps.minecraft")}]"
        }
    }
}

legacyForge {
    version = "${property("deps.minecraft")}-${property("deps.forge")}"

    validateAccessTransformers = true

    accessTransformers.from(
        rootProject.file("src/main/resources/aw/${sc.current.version}.cfg")
    )

    runs {
        register("client") {
            client()
            gameDirectory = file("run/")
            ideName = "Forge Client (${sc.active?.version})"
            programArgument("--username=Dev")
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
//    compileOnly("io.github.llamalad7:mixinextras-common:${libs.versions.mixinextras.get()}")
//    modImplmentation("io.github.llamalad7:mixinextras-forge:${libs.versions.mixinextras.get()}")

//    compileOnly(annotationProcessor(libs.mixinextras.common))

//    annotationProcessor(libs.mixinextras.common)
//    compileOnly(libs.mixinextras.common)

//    implementation(jarJar(libs.mixinextras.forge))

//    jarJar(libs.mixinextras.forge)
//    implementation(libs.mixinextras.forge)

//    jarJar(libs.mixinextras.forge)
//    implementation(jarJar(lib.llamalad7.mixinextras)) {
//        jarJar.ranged(it, "[0.5.3,)")
//    }


    modApi("me.shedaniel.cloth:cloth-config-forge:${property("deps.cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }
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