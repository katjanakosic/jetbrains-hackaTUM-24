package com.github.katjanakosic.jetbrainshackatum24

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.Properties
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import org.jetbrains.annotations.Nullable

class CodeExplainer : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel: CaretModel = editor.caretModel
        val selectedText = caretModel.currentCaret.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showErrorDialog("No text selected.", "Error")
            return
        }

        // Get the entire file content
        val document = editor.document
        val fileContent = document.text

        // Read API key from config.properties
        val properties = Properties()
        val propertiesStream = javaClass.classLoader.getResourceAsStream("config.properties")
        if (propertiesStream == null) {
            Messages.showErrorDialog("Could not find config.properties.", "Error")
            return
        }
        properties.load(propertiesStream)
        val apiKey = properties.getProperty("OPENAI_API_KEY")
        if (apiKey.isNullOrBlank()) {
            Messages.showErrorDialog("API key not found in config.properties.", "Error")
            return
        }

        val project = e.project

        object : Task.Backgroundable(project, "Ask Chat for Help!", true) {
            var content: String? = null
            var errorMessage: String? = null

            override fun run(indicator: ProgressIndicator) {
                // Build the prompt including the entire file content
                val prompt = """
                    |Act as a coding expert.
                    |
                    |Consider the content of the file:
                    |$fileContent
                    |
                    |Please explain the following selected code in detail, considering the context of the entire file:
                    |$selectedText
                    """.trimMargin()

                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }

                val requestBodyJson = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", messagesArray)
                }

                val client = OkHttpClient()

                val mediaType = "application/json".toMediaType()
                val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(requestBody)
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        errorMessage = "Request failed: ${response.message}"
                        return
                    }

                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    content = message.getString("content")

                } catch (e: Exception) {
                    errorMessage = "An error occurred: ${e.message}"
                }
            }

            override fun onSuccess() {
                if (content != null) {
                    ApplicationManager.getApplication().invokeLater {
                        val dialog = ExplanationDialog(content!!)
                        dialog.show()
                    }
                } else if (errorMessage != null) {
                    Messages.showErrorDialog(errorMessage, "Error")
                }
            }

            override fun onThrowable(error: Throwable) {
                Messages.showErrorDialog("An error occurred: ${error.message}", "Error")
            }
        }.queue()
    }
}

class ExplanationDialog(private val explanation: String) : DialogWrapper(true) {

    init {
        init()
        title = "Chat's Response"
        setResizable(true)
    }

    @Nullable
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BorderLayout()

        val textArea = JBTextArea()
        textArea.text = explanation
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        val scrollPane = JBScrollPane(textArea)
        scrollPane.preferredSize = Dimension(600, 400) // Adjust size as needed

        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }
}
