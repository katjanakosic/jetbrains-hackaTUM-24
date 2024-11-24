# Pandora's Toolbox
An AI-powered IntelliJ plugin that enables code explanations, unit test generation, and a real-time voice assistant

This repository is part of the hackaTUM 2024.


---


## Table of Contents

- [Project Description](#project-description)
- [Project Structure](#project-structure)
- [Installation & Usage](#installation)
- [Features](#features)



---


## Project Description
This project contains plugins for code explanation on selected code snippets, unit test generation for specific file functions, and a real-time voice assistant.

The plugins are built using Kotlin, Deepgram, and OpenAI.

---


## Project Structure

This project structure focuses on the most important files in this project.

```
SwiftScriptRunner/
├── SwiftScriptRunnerApp.swift            -> Main application
├── ContentView.swift                     -> Main SwiftUI View
├── Views/
│   ├── SyntaxHighlightingTextView.swift  -> Syntax highlighting logic
├── Models/
│   ├── ScriptError.swift                 -> Error handling structures
└── Utilities/
    ├── CustomTextView.swift              -> Custom NSTextView subclass for editor
    └── Notifications.swift               -> Notification extensions

```


---


## Requirements

- **IntelliJ**: Version 2024.3 or later
- **Gradle**: Version 8.10.2 or later
- **Okhttp3**: Version 4.12.0 or later
- **JSON**: Version 20240303 or later

---

## Installation & Usage

1. **Clone the Repository** `git clone https://github.com/katjanakosic/jetbrains-hackaTUM-24`

2. **Open the Project**

3. **Build and Run**
    - Select the 'Run Plugin' Gradle option at the top toolbar.
    - Run the plugin by pressing the run button.
    - Annother IntelliJ window will pop up where you can test the implementation. Open an existing project for this.
      
3. **Usage**
    - Code Explanation: Select a code snippet, right-click on it and select the `Ask Chat for Help!` button in the editor popup menu.
    - Unit Test Generator: 
    - Voice Assistant:

---


## Features

- **Code Explanation**

- **Unit Test Generator**

- **Real-Time Voice Assistant**


---

