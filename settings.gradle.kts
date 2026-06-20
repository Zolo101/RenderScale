pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
        exclusiveContent {
            forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
            filter { includeGroup("maven.modrinth") }
        }
    }
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.2"
}

stonecutter {
    create(rootProject) {
        fun match(version: String, vararg loaders: String) =
            loaders.forEach { version("$version-$it", version).buildscript = getBuildscript(it, version) }

        match("1.20.1", "fabric", "forge")
        match("1.21.1", "fabric", "neoforge") // and 1.21
        match("1.21.3", "fabric", "neoforge") // and 1.21.2
        match("1.21.4", "fabric", "neoforge")
        match("1.21.5", "fabric", "neoforge")
        match("1.21.8", "fabric", "neoforge") // and 1.21.6, 1.21.7
        match("1.21.10", "fabric", "neoforge") // and 1.21.9
        match("1.21.11", "fabric", "neoforge")
        // TODO: Above versions "publish.additionalVersions" property does not get put in the depends property in mods.toml!!!

        // After here, it doesn't have to be the latest hotfix build
        // since we can be confident that hotfix versions won't break the mod
        match("26.1", "fabric", "neoforge") // and 26.1.1, 26.1.2
        match("26.2", "fabric", "neoforge")

        // latest fabric version always
        vcsVersion = "26.2-fabric"
    }
}

private fun getBuildscript(loader: String, version: String): String {
    if (loader == "fabric") {
        return if (version.startsWith("1.")) {
            "build.fabric-o.gradle.kts"
        } else {
            "build.fabric-m.gradle.kts"
        }
    }
    return "build.$loader.gradle.kts"
}