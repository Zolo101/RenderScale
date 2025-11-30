plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.6-fabric"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.terraformersmc.com/")
    }
}