package com.github.katjanakosic.jetbrainshackatum24.voiceAssistance

import javax.swing.*
import javax.swing.BoxLayout
import javax.swing.SwingUtilities
import java.util.Properties
import com.intellij.openapi.ui.Messages
import java.awt.Color
import javax.swing.text.*
import com.intellij.ui.JBColor

class VoiceAssistantPanel : JPanel() {

    private val recordButton: JButton = JButton("Record")
    private val chatHistoryArea: JTextPane = JTextPane()

    private var isRecording = false
    private var audioRecorder: AudioRecorder? = null
    private var deepgramClient: DeepgramStreamingClient? = null

    // Store all partial transcriptions
    private val partialTranscriptions = StringBuilder()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        chatHistoryArea.isEditable = false
        val scrollPane = JScrollPane(chatHistoryArea)

        // Initialize styles for different message types
        val doc = chatHistoryArea.styledDocument

        val systemStyle = chatHistoryArea.addStyle("SystemStyle", null)
        StyleConstants.setForeground(systemStyle, JBColor.GRAY)

        val userStyle = chatHistoryArea.addStyle("UserStyle", null)
        StyleConstants.setForeground(userStyle, JBColor.BLUE)

        val assistantStyle = chatHistoryArea.addStyle("AssistantStyle", null)
        StyleConstants.setForeground(assistantStyle, JBColor.GREEN)

        recordButton.addActionListener {
            if (!isRecording) {
                startRecording()
                recordButton.text = "Stop"
            } else {
                stopRecording()
                recordButton.text = "Record"
            }
            isRecording = !isRecording
        }

        add(scrollPane)
        add(Box.createVerticalStrut(10))
        add(recordButton)
    }

    private fun startRecording() {
        val properties = Properties()

        val propertiesStream = javaClass.classLoader.getResourceAsStream("config.properties")
        if (propertiesStream == null) {
            Messages.showErrorDialog("Could not find config.properties.", "Error")
            return
        }
        properties.load(propertiesStream)
        val deepgramApiKey = properties.getProperty("DEEPGRAM_API_KEY")
        if (deepgramApiKey.isNullOrBlank()) {
            Messages.showErrorDialog("API key not found in config.properties.", "Error")
            return
        }

        // Initialize Deepgram Streaming Client
        deepgramClient = DeepgramStreamingClient(
            apiKey = deepgramApiKey,
            transcriptionListener = { transcription, isFinal ->
                updateTranscription(transcription)
            },
            errorListener = { error ->
                displayMessage("System", error)
            }
        )
        deepgramClient?.connect()

        // Initialize Audio Recorder
        audioRecorder = AudioRecorder { audioData ->
            deepgramClient?.sendAudioData(audioData)
        }
        audioRecorder?.startRecording()
        displayMessage("System", "Recording started...")
        displayMessage("User", "")
    }

    private fun stopRecording() {
        audioRecorder?.stopRecording()
        audioRecorder = null
        deepgramClient?.disconnect()
        deepgramClient = null
        displayMessage("System", "Recording stopped.")

        // Send the consolidated transcription to OpenAI
        val userMessage = partialTranscriptions.toString().trim()
        if (userMessage.isNotEmpty()) {
            sendToOpenAI(userMessage)
            partialTranscriptions.clear()
        } else {
            displayMessage("System", "No transcription available to send.")
        }
    }

    private fun updateTranscription(transcription: String) {
        SwingUtilities.invokeLater {
            // Append new partial transcription
            partialTranscriptions.append(transcription).append(" ")
            val doc = chatHistoryArea.styledDocument
            val userStyle = chatHistoryArea.getStyle("UserStyle")
            try {
                doc.insertString(doc.length, transcription, userStyle)
            } catch (e: BadLocationException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendToOpenAI(message: String) {

        val properties = Properties()

        val propertiesStream = javaClass.classLoader.getResourceAsStream("config.properties")
        if (propertiesStream == null) {
            Messages.showErrorDialog("Could not find config.properties.", "Error")
            return
        }
        properties.load(propertiesStream)
        val openAiApiKey = properties.getProperty("OPENAI_API_KEY")
        if (openAiApiKey.isNullOrBlank()) {
            Messages.showErrorDialog("API key not found in config.properties.", "Error")
            return
        }

        val thread = Thread {
            try {
                val response = OpenAIClient.getChatResponse(message, openAiApiKey)
                displayMessage("Assistant", response)
            } catch (e: Exception) {
                e.printStackTrace()
                displayMessage("System", "Error: ${e.message}")
            }
        }
        thread.start()
    }

    private fun displayMessage(sender: String, message: String) {
        SwingUtilities.invokeLater {
            val doc = chatHistoryArea.styledDocument
            val style = chatHistoryArea.getStyle("${sender}Style") ?: chatHistoryArea.getStyle("SystemStyle")
            try {
                doc.insertString(doc.length, "\n\n$sender: $message", style)
            } catch (e: BadLocationException) {
                e.printStackTrace()
            }
        }
    }
}
