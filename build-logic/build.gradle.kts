plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

gradlePlugin {
    plugins {
        register("modPlatform") {
            id = "mod-platform"
            implementationClass = "ModPlatformPlugin"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    maven("https://jitpack.io") { name = "Jitpack" }
}

// No "no comment" warnings
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

dependencies {
    implementation(libs.kikugie.postprocess)
    implementation(libs.dotenv.gradle)
    implementation(libs.kikugie.stonecutter)
    implementation(libs.mod.publish.plugin)
    implementation(libs.foojay.resolver)
    implementation(libs.fletching.table)
    implementation(libs.vanniktech.maven.publish)
    implementation(libs.serialization.json)
    implementation(libs.serialization.toml)
}
