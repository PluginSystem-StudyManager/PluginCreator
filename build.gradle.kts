plugins {
    kotlin("jvm") version "1.3.72"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
    kotlin("plugin.serialization") version "1.3.70"
    id("net.anshulverma.gradle.fileupload") version "1.0.0"
}

repositories {
    mavenCentral()
    jcenter()
}

version = "0.1"
group = "com.github.studymanager.plugincreator"

dependencies {
    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation(group = "io.github.rybalkinsd", name = "kohttp", version = "0.12.0")
    implementation("net.anshulverma.gradle:gradle-fileupload-plugin:1.0.4")
}

gradlePlugin {
    plugins {
        create("plugincreator") {
            id = "com.github.studymanager.plugincreator"
            displayName = "Study Tool Plugin"
            description = "Develop and publish your own plugin for the 'StudyTool'."
            implementationClass = "com.github.studymanager.plugincreator.StudyToolPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.studymanager.plugincreator"
            artifactId = "plugincreator"
            version = "0.1"

            from(components["kotlin"])
        }
    }
}



pluginBundle {
    website = "https://github.com/JulianSobott/TimeManager"
    vcsUrl = "https://github.com/JulianSobott/TimeManager"
    tags = listOf("studymanager", "plugin", "java")
}

