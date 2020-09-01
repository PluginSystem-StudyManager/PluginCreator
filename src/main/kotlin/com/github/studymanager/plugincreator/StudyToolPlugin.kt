package com.github.studymanager.plugincreator

import groovy.lang.Closure
import io.github.rybalkinsd.kohttp.dsl.async.httpPostAsync
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.dsl.upload
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.codehaus.groovy.ast.tools.GeneralUtils.params
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
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

const val META_FILE = "plugin_load.json"

class StudyToolPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Plugin dependencies
        project.plugins.apply("java")
        project.plugins.apply("org.openjfx.javafxplugin")

        // Extension
        val ext = project.extensions.create<StudyToolExtension>("StudyTool", StudyToolExtension::class.java)

        // Config
        val extJar = project.tasks.getByName("jar")


        // Register tasks
        project.tasks.register<BuildPublishTask>(TASK_BUILD_PUBLISH, BuildPublishTask::class.java)
        project.tasks.register<PublishTask>(TASK_PUBLISH, PublishTask::class.java)
        project.tasks.register<CheckTask>(TASK_CHECK, CheckTask::class.java)
        project.tasks.register<TestPluginTask>(TASK_TEST, TestPluginTask::class.java)
        project.tasks.register<InitProjectFilesTask>(TASK_INIT, InitProjectFilesTask::class.java)

        // Edit tasks
        project.tasks.getByPath("jar").doFirst {
            if (it is Jar) {
                it.archiveFileName.set("${ext.id}.jar")
                // create meta file
                val json = Json(JsonConfiguration.Stable)
                val data = json.stringify(LoadDataExtension.serializer(), ext.loadDataSettings)
                println(data)
                val path = "build/resources/$META_FILE"
                File(path).writeText(data)
                // add meta file
                it.from(path)
            } else {
                // Empty: Project is wrong configured
            }
        }

        // Task dependencies
        project.tasks.getByPath(TASK_BUILD_PUBLISH).dependsOn("jar", TASK_CHECK)
        project.tasks.getByPath(TASK_PUBLISH).dependsOn(TASK_BUILD_PUBLISH)
    }
}

open class CheckTask : DefaultTask() {

    init {
        group = GROUP
    }

    @TaskAction
    fun checkPlugin() {
        println("Hello: Checking!")
        // Get Meta file
        // Check existence of fxml paths
        // Check existence of info files
    }
}

open class TestPluginTask : DefaultTask() {

    init {
        group = GROUP
    }

    @TaskAction
    fun testPlugin() {
        println("Hello: testing!")
    }
}

open class PublishTask : DefaultTask() {

    init {
        group = GROUP
    }

    @TaskAction
    fun publishStudyPlugin() {
        println("Hello: Publishing!")
        val task = project.tasks.getByPath(TASK_BUILD_PUBLISH)

        if (task is BuildPublishTask) {
            val filePath = File("${task.destinationDirectory.get()}", task.archiveFileName.get())
            val response = httpPost {
                url("http://127.0.0.1:8080/api/plugins/upload")

                header {
                    cookie {
                        // TODO: Adjust Cookie
                        "token" to "12345"
                        "pluginName" to "ExamplePlugin"
                    }
                }

                multipartBody("multipart/form-data") {
                    +part("file", filename = filePath.path) {
                        file(filePath)
                    }
                }
            }
            println("Upload: ${response.isSuccessful} with body: ${response.body()}")
        }
    }
}


open class BuildPublishTask : Zip() {

    init {
        group = GROUP

        archiveBaseName.set("publish")
        destinationDirectory.set(File("build/"))

        // info files TODO: get extensions value
        val ext = project.extensions.getByType(StudyToolExtension::class.java)
        val infoPath = project.projectDir.path.plus("\\${ext.infoPath}")
        val infoFiles = File(infoPath)
        if (!infoFiles.exists()) {
            throw FileNotFoundException("Info folder not found: $infoPath")
        }
        into("info") { inner ->
            inner.from(infoFiles)
        }

        // jar file
        from(File("build/libs"))
    }
}

open class InitProjectFilesTask : DefaultTask() {

    init {
        group = GROUP
    }

    @TaskAction
    fun initFiles() {
        val mapping = mapOf(
                "MainController.java" to "src/main/java/MainController.java",
                "SettingsController.java" to "src/main/java/SettingsController.java",
                "main.fxml" to "src/main/resources/main.fxml",
                "settings.fxml" to "src/main/resources/settings.fxml",
                "README.md" to "README.md",
                "plugin_info.yaml" to "info/plugin_info.yaml",
                "gitignore.txt" to ".gitignore" // .txt because otherwise can't find resource
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
    var infoPath: String = "info"
    var id: String = "TestID"
    var loadDataSettings: LoadDataExtension = LoadDataExtension()

    fun loadData(closure: Closure<Any>) {
        loadDataSettings = LoadDataExtension()
        closure.delegate = loadDataSettings
        closure.call()
    }
}

@Serializable
open class LoadDataExtension {
    var windowFxml: String = "main.fxml"
    var settingsFxml: String = "settings.fxml"
    var icon: String = "icon.png"
}
