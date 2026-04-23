plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

group = "net.hudkit"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.skriptlang.org/releases")
    maven("https://jitpack.io")
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    // Skript
    compileOnly("com.github.SkriptLang:Skript:2.15.0")

    // HudKit API — built from source:
    //   1. git clone https://forgejo.perny.dev/perny/hudkit.git
    //   2. cd hudkit && .\gradlew :api:jar
    //   3. copy api/build/libs/hudkit-api-*.jar into this project's libs/ folder
    compileOnly(fileTree("libs") { include("*.jar") })
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set("skript-hudkit")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}