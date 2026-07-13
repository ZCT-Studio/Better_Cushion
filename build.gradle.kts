import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

base {
    archivesName.set("${mod.id}-${mod.loader}")
} // fix name

modSettings {
    var mod_license = findProperty("mod.license") ?: ""
    var mod_authors = findProperty("mod.authors") ?: ""
    var mod_contributors = findProperty("mod.contributors") ?: ""

    var semver_mc_version_range = findProperty("semver_mc_version_range") ?: mod.minecraftVersion
    var maven_mc_version_range = findProperty("maven_mc_version_range") ?: "[${mod.minecraftVersion}]"
    var cushionsbackport_version = findProperty("cushionsbackport_version") ?: "*"

    variableReplacements = mapOf(
        "license" to mod_license,
        "authors" to mod_authors,
        "contributors" to mod_contributors,

        "semver_mc_version_range" to semver_mc_version_range,
        "maven_mc_version_range" to maven_mc_version_range,
        "cushionsbackport_version" to cushionsbackport_version
    )
}

// Example of overriding publishing settings
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        if (mod.isFabric) requires("fabric-api")
    }
}
