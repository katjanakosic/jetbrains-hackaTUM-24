# Pandora's Toolbox
An AI-powered IntelliJ plugin that enables code explanations, unit test generation, and a real-time voice assistant

This repository is part of the hackaTUM 2024.


---


## Table of Contents

- [Project Description](#project-description)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Installation & Usage](#installation--usage)
- [Features](#features)



---


## Project Description
<!-- Plugin description -->
Pandora's Toolbox comprises three IntelliJ plugins designed to enhance developer productivity and understanding:

1. Code Explanation: Offers detailed explanations of selected code snippets to aid comprehension.
2. Unit Test Generator: Automatically generates unit tests for specific functions or classes.
3. Real-Time Voice Assistant: Allows developers to interact with IntelliJ using voice commands for hands-free operation.

Built using Kotlin, OpenAI for intelligent code analysis and generation, and Deepgram for speech recognition, these 
plugins integrate seamlessly into the IntelliJ environment to provide a robust development experience.
<!-- Plugin description end -->

---


## Project Structure

This project structure focuses on the most important files in this project.

```
jetbrains-hackaTUM-24/
└── src/main/kotlin/
    └── com.github.katjanakosic.jetbrainshackatum24/
        ├── codeExplanation               -> Code Explanation plugin
        ├── unitTestGeneration            -> Unit Test Generation plugin
        └── voiceAssistance               -> Voice Assistant plugin

```


---


## Requirements

- **IntelliJ**: Version 2024.3 or later
- **Gradle**: Version 8.10.2 or later
- **Okhttp3**: Version 4.12.0 or later
- **JSON**: Version 20240303 or later
- **OpenAI API Key**: For code analysis and generation
- **Deepgram API Key**: For voice recognition capabilities 


---

## Installation & Usage

1. **Clone the Repository** `git clone https://github.com/katjanakosic/jetbrains-hackaTUM-24`

2. **Open the Project**

3. **Build and Run**
    - Open the Gradle tool window.
    - Navigate to each plugin's `build.gradle.kts` and ensure dependencies are resolved.
    - Select the `Run Plugin` Gradle task.
    - Press the run button to build and launch the plugins.
    - A new IntelliJ window will open where you can test the installed plugins. Open an existing project to begin.
      
3. **Usage**
    - Code Explanation: Select a code snippet, right-click on it and select the `Ask Chat for Help!` option in the editor popup menu.
    - Unit Test Generator: Select function code, right-click on it and select the `Generate Unit Test` option in the editor popup menu.
    - Voice Assistant: Click on the microphone icon added to the IntelliJ toolbar and start/stop the recording

---


## Features

- **Code Explanation**: 
   
   Provides comprehensive explanations for selected code snippets within the editor, enhancing understanding and facilitating learning.

- **Unit Test Generator**: 

   Automates the creation of unit tests for specific functions or classes, streamlining the testing process and ensuring code reliability.

- **Real-Time Voice Assistant**: 

   Enables developers to interact with IntelliJ using voice commands, promoting a hands-free and efficient workflow.


---

