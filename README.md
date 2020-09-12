# StudyTool: PluginCreator

A gradle plugin to create plugins for the StudyTool application.

## Tasks

The tool provides the following tasks:

- **initProject**: Creates all necessary files with dummy content. Execute this task when you want to create a new plugin.
- **testPlugin**: Run this plugin in the desktop application and see if it is working.
- **checkPlugin**: Checks if this is a valid plugin that can be uploaded.
- **buildPublish**: Build all files that are needed for publishing the plugin.
- **publishPlugin**: Publish the plugin and make it available to other users.

Normally you only have to execute the tasks: initProject, testPlugin, publishPlugin, because the others are executed automatically at the publishPlugin task.

## Develop a new plugin

Checkout the [developer guide](TODO).

## Develop this project

This will help you, if you want to make changes to this project.

### Prerequisites

- kotlin
- gradle
- Java

### Project structure

There are two gradle modules (PluginCreator, Sandbox). The Sandbox module is for testing purposes. 
The plugin code is in: [PluginCreator/src/main/kotlin/com/github/studymanager/plugincreator/StudyToolPlugin.kt](src/main/kotlin/com/github/studymanager/plugincreator/StudyToolPlugin.kt)

The `resources` folder contains template files that are needed for the `initProject` task.

### Test your changes

You can use the `publishToMavenLocal` task to test your changes without creating a new release.
