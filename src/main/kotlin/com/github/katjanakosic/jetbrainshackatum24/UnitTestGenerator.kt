package com.github.katjanakosic.jetbrainshackatum24

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.awt.BorderLayout
import java.io.IOException
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JScrollPane
import java.util.Properties

class UnitTestGenerator : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val project = event.project

        if (editor == null || project == null) {
            Messages.showErrorDialog("Editor or Project not found", "Error")
            return
        }

        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showInfoMessage("Please select a function to generate unit tests.", "No Selection")
            return
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating Unit Tests...") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val unitTestCode = generateUnitTests(selectedText)
                    ApplicationManager.getApplication().invokeLater {
                        UnitTestToolWindowFactory.displayUnitTests(project, unitTestCode)
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Error generating unit tests: ${e.message}", "Error")
                    }
                }
            }
        })
    }

    private fun generateUnitTests(functionCode: String): String {
        return callOpenAIApi(functionCode)
    }

    private fun callOpenAIApi(functionCode: String): String {

        val properties = Properties()

        val propertiesStream = javaClass.classLoader.getResourceAsStream("config.properties")
        if (propertiesStream == null) {
            Messages.showErrorDialog("Could not find config.properties.", "Error")
            return "There is no config.properties file"
        }
        properties.load(propertiesStream)
        val apiKey = properties.getProperty("OPENAI_API_KEY")
        if (apiKey.isNullOrBlank()) {
            Messages.showErrorDialog("API key not found in config.properties.", "Error")
            return "There is no config.properties file"
        }

        val apiUrl = "https://api.openai.com/v1/chat/completions"

        val prompt = "Generate comprehensive unit tests in Kotlin for the following function. Include meaningful test cases to improve code reliability. Provide only the code:\n\n$functionCode"

        val json = JSONObject()
        json.put("model", "gpt-3.5-turbo")
        val messages = JSONArray()
        val message = JSONObject()
        message.put("role", "user")
        message.put("content", prompt)
        messages.put(message)
        json.put("messages", messages)
        json.put("temperature", 0.7)

        val jsonBody = json.toString()
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                throw IOException("Unexpected code $response\n$errorBody")
            }

            val responseBody = response.body?.string()
            val responseJson = JSONObject(responseBody)
            val choices = responseJson.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val messageObject = firstChoice.getJSONObject("message")
            val generatedText = messageObject.getString("content")

            return generatedText.trim()
        }
    }
}

class UnitTestToolWindowFactory : ToolWindowFactory {
    companion object {
        private var unitTestPanel: UnitTestPanel? = null

        fun displayUnitTests(project: Project, unitTestCode: String) {
            val toolWindowManager = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow("Generated Unit Tests")
            if (toolWindow != null) {
                if (!toolWindow.isVisible) {
                    toolWindow.show(null)
                }
                unitTestPanel?.setUnitTestCode(unitTestCode)
            } else {
                Messages.showErrorDialog(project, "Unit Test Tool Window not found.", "Error")
            }
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        unitTestPanel = UnitTestPanel()
        val content = contentFactory.createContent(unitTestPanel!!.panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class UnitTestPanel {
    val panel: JPanel = JPanel(BorderLayout())
    private val textArea: JTextArea = JTextArea()

    init {
        textArea.isEditable = false
        val scrollPane = JScrollPane(textArea)
        panel.add(scrollPane, BorderLayout.CENTER)
    }

    fun setUnitTestCode(unitTestCode: String) {
        textArea.text = unitTestCode
    }
}
