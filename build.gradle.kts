plugins {
    idea
    java
    kotlin("jvm") version "1.6.10"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.torui.coflmod"
version = "1.4.4-alpha"

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration:
loom {
    launchConfigs {
        "client" {
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/kotlin/main"))
}

// Dependencies:

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    mavenLocal()
    mavenCentral()
    maven("https://repo.sk1er.club/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-releases/")
    maven("https://jitpack.io")
    // If you don't want to log in with your real minecraft account, remove this line
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    // Add essential dependency so we can access elementa (Gui creator)
    // shadowImpl("gg.essential:loader-launchwrapper:1.1.3")
    // implementation("gg.essential:essential-1.8.9-forge:11640+g7f637cfee") {
    //     exclude(module = "asm")
    //     exclude(module = "asm-commons")
    //     exclude(module = "asm-tree")
    //     exclude(module = "gson")
    // }

    implementation("gg.essential:elementa-1.8.9-forge:575")
    implementation("gg.essential:vigilance-1.8.9-forge:280")
    
    annotationProcessor("org.spongepowered:mixin:0.8.4-SNAPSHOT")

    shadowImpl("com.neovisionaries:nv-websocket-client:2.14")

    // If you don't want to log in with your real minecraft account, remove this line
    modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.1.0")
    
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set("CoflMod")
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["Manifest-Version"] = "1.0"
        // this["TweakClass"] = "gg.essential.loader.stage0.EssentialSetupTweaker"
        // this["TweakOrder"] = "0"
    }
}


val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("all")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.shadowJar {
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.forEach {
            println("Config: ${it.files}")
        }
    }
}

tasks.assemble.get().dependsOn(tasks.remapJar)

