package com.github.studymanager.plugincreator

import groovy.lang.Closure
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.openjfx.gradle.JavaFXOptions
import java.io.File
import java.io.FileNotFoundException

/**
 * Tasks:
 * - publish plugin
 * - Check plugin
 * - test plugin installed
 * - test plugin not installed
 */

const val GROUP = "study tool"

const val TASK_PUBLISH = "publishPlugin"
const val TASK_BUILD_PUBLISH = "buildPublish"
const val TASK_CHECK = "checkPlugin"
const val TASK_TEST = "testPlugin"
const val TASK_INIT = "initProject"
const val TASK_CONFIGURE = "configStudyTool"

const val META_FILE = "plugin_load.json"

// Currently one token per account. Maybe change to one token per plugin. If so add attribute to Extension
const val TOKEN_PROPERTY = "studytool.token"
const val EXTENSION_NAME = "StudyTool"

class StudyToolPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Plugin dependencies
        project.plugins.apply("java")
        project.plugins.apply("org.openjfx.javafxplugin")

        // Extension
        val ext = project.extensions.create<StudyToolExtension>(EXTENSION_NAME, StudyToolExtension::class.java)
        val jfxExtension = project.extensions.getByName("javafx") as JavaFXOptions
        jfxExtension.modules = listOf("javafx.controls", "javafx.fxml", "javafx.web")

        // Config
        val extJar = project.tasks.getByName("jar")


        // Register tasks
        project.tasks.register<ConfigureTask>(TASK_CONFIGURE, ConfigureTask::class.java)
        project.tasks.register<BuildPublishTask>(TASK_BUILD_PUBLISH, BuildPublishTask::class.java)
        project.tasks.register<PublishTask>(TASK_PUBLISH, PublishTask::class.java)
        project.tasks.register<CheckTask>(TASK_CHECK, CheckTask::class.java)
        project.tasks.register<TestPluginTask>(TASK_TEST, TestPluginTask::class.java)
        project.tasks.register<InitProjectFilesTask>(TASK_INIT, InitProjectFilesTask::class.java)

        // Task dependencies
        project.tasks.getByPath(TASK_BUILD_PUBLISH).dependsOn("jar", TASK_CHECK, TASK_CONFIGURE)
        project.tasks.getByPath("jar").dependsOn(TASK_CONFIGURE)
        project.tasks.getByPath(TASK_PUBLISH).dependsOn(TASK_BUILD_PUBLISH)
    }
}

open class CheckTask : DefaultTask() {

    init {
        group = GROUP
        description = "Checks if the plugin is a valid plugin that can be published"
    }

    @TaskAction
    fun checkPlugin() {
        println("Hello: Checking!")
        // Get Meta file
        // Check existence of fxml paths
        // Check existence of info files
        // Check existence of icon + preview image
        // Check names and ids are correctly set
        // plugin_info.yaml is correct
    }
}

open class TestPluginTask : DefaultTask() {

    init {
        group = GROUP
        description = "Runs the plugin in the application"
    }

    @TaskAction
    fun testPlugin() {
        println("Hello: testing!")
        val ext = getExtension(project)
        println(ext.id)
        println(ext.infoPath)
        println(ext.loadDataSettings.windowFxml)
    }
}

open class PublishTask : DefaultTask() {

    init {
        group = GROUP
        description = "Publishes the plugin"
    }

    @TaskAction
    fun publishStudyPlugin() {
        val task = project.tasks.getByPath(TASK_BUILD_PUBLISH)
        val extension = getExtension(project)

        if (task is BuildPublishTask) {
            val filePath = File("${task.destinationDirectory}", task.archiveFileName.get())
            // Token is stored in GRADLE_HOME/gradle.properties
            val token = project.findProperty(TOKEN_PROPERTY) as String
            val pluginName = extension.id

            val response = httpPost {
                url("http://127.0.0.1:8080/api/plugins/upload")

                header {
                    cookie {
                        "token" to token
                        "pluginName" to pluginName
                    }
                }

                multipartBody("multipart/form-data") {
                    +part("file", filename = filePath.path) {
                        file(filePath)
                    }
                }
            }
            if (response.isSuccessful) {
                println("Successfully published")
            } else {
                println("Publish failed: ${response.message()} with message: ${response.body().toString()}")
            }
        }
    }
}


open class ConfigureTask : DefaultTask() {

    init {
        description = "Internal task to configure the other tasks"
    }

    @TaskAction
    fun configure() {
        val ext = getExtension(project)

        // build task
        val buildTask = project.tasks.getByPath(TASK_BUILD_PUBLISH) as BuildPublishTask
        buildTask.setup()

        // Jar task
        val jarTask = project.tasks.getByPath("jar") as Jar
        jarTask.archiveFileName.set("plugin_${ext.id}.jar")
        // create meta file
        val json = Json(JsonConfiguration.Stable)
        val data = json.stringify(LoadDataExtension.serializer(), ext.loadDataSettings)
        val path = "build/resources/$META_FILE"
        File(path).writeText(data)

        // add meta file
        jarTask.from(path)
    }

}

open class BuildPublishTask : Zip() {



    init {
        group = GROUP
        description = "Build all necessary files for publishing"

        archiveBaseName.set("publish")
        destinationDirectory.set(File("build/"))
    }

    fun setup() {
        val ext = getExtension(project)

        // add Info files
        val infoPath = project.projectDir.path.plus("\\${ext.infoPath}")
        val infoFiles = File(infoPath)
        if (!infoFiles.exists()) {
            throw FileNotFoundException("Info folder not found: $infoPath")
        }
        into("info") { inner ->
            inner.from(infoFiles)
        }

        // add Jar file
        from("build/libs")
    }
}

open class InitProjectFilesTask : DefaultTask() {

    init {
        group = GROUP
        description = "Create all files that are needed for a valid plugin"
    }

    @TaskAction
    fun initFiles() {
        val mapping = mapOf(
                "MainController.java" to "src/main/java/main/MainController.java",
                "SettingsController.java" to "src/main/java/main/SettingsController.java",
                "main.fxml" to "src/main/resources/main.fxml",
                "settings.fxml" to "src/main/resources/settings.fxml",
                "README.md" to "README.md",
                "plugin_info.yaml" to "info/plugin_info.yaml",
                "gitignore.txt" to ".gitignore", // .txt because otherwise can't find resource
                "icon.png" to "info/img/icon.png",
                "preview_1.png" to "info/img/preview_1.png"
        )
        for (e in mapping.entries) {
            val sourceStream = javaClass.getResourceAsStream("/templates/${e.key}")
            val dest = project.file(e.value)
            if (!dest.exists()) {
                dest.parentFile.mkdirs()
                dest.writeBytes(sourceStream.readAllBytes())
                println("Successfully created: ${e.value}")
            } else {
                println("Already exists: ${e.value}")
            }
        }
    }
}


open class StudyToolExtension {
    var infoPath: String = "invalid"
    var id: String = "invalid"
    var loadDataSettings: LoadDataExtension = LoadDataExtension()

    fun loadData(closure: Closure<Any>) {
        loadDataSettings = LoadDataExtension()
        closure.delegate = loadDataSettings
        closure.call()
    }
}

@Serializable
open class LoadDataExtension {
    var windowFxml: String = "invalid"
    var settingsFxml: String = "invalid"
    var icon: String = "invalid"
}


fun getExtension(project: Project): StudyToolExtension {
    return project.extensions.getByName(EXTENSION_NAME) as StudyToolExtension
}