plugins {
    alias(libs.plugins.stonecutter)
    alias(libs.plugins.mod.publish.plugin)
    alias(libs.plugins.loom.back.compat).apply(false)
    alias(libs.plugins.neoforged.moddev).apply(false)
    alias(libs.plugins.jsonlang.postprocess).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.devtools.ksp).apply(false)
    alias(libs.plugins.fletching.table).apply(false)
    alias(libs.plugins.legacyforge.moddev).apply(false)
    id("mod-root")
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["mod_id"] = "\"${properties.get<String>("mod.id")}\";"
    swaps["mod_name"] = "\"${properties.get<String>("mod.name")}\";"
    swaps["mod_group"] = "\"${properties.get<String>("mod.group")}\";"
    swaps["minecraft"] = "\"${current.version}\";"
    // TODO: node.project.property("deps.iris") gives missingPropertyException? why?
//    dependencies["iris"] = current.project.property("deps.iris") as String
//    dependencies["sodium"] = current.project.property("deps.sodium") as String

    constants["iris"] = current.project.substringAfterLast('-') != "forge"
    constants["sodium"] = current.parsed >= "1.21.11" || current.version == "1.21.1"

    // Versions affected by stuttering with Dynamic Scaling + Iris Shaders
    constants["stutter"] = current.version == "1.20.1" || current.version == "1.21.1"

}
