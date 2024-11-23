package com.github.katjanakosic.jetbrainshackatum24
import javax.swing.*
import javax.swing.BoxLayout
import javax.swing.SwingUtilities
import java.util.Properties
import com.intellij.openapi.ui.Messages

class VoiceAssistantPanel : JPanel() {

    private val recordButton: JButton = JButton("Record")
    private val chatHistoryArea: JTextArea = JTextArea(15, 50)

    private var isRecording = false
    private var audioRecorder: AudioRecorder? = null

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        chatHistoryArea.isEditable = false
        val scrollPane = JScrollPane(chatHistoryArea)

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
        audioRecorder = AudioRecorder()
        audioRecorder?.startRecording()
        displayMessage("System", "Recording started...")
    }

    private fun stopRecording() {
        val audioData = audioRecorder?.stopRecording()
        audioRecorder = null
        displayMessage("System", "Recording stopped.")
        // Now send audioData to Deepgram for transcription
        if (audioData != null) {
            transcribeAudio(audioData)
        } else {
            displayMessage("System", "No audio data captured.")
        }
    }

    private fun transcribeAudio(audioData: ByteArray) {
        val properties = Properties()

        val propertiesStream = javaClass.classLoader.getResourceAsStream("config.properties")
        if (propertiesStream == null) {
            Messages.showErrorDialog("Could not find config.properties.", "Error")
            return
        }
        properties.load(propertiesStream)
        val openAiApiKey = properties.getProperty("OPENAI_API_KEY")
        val deepgramApiKey = properties.getProperty("DEEPGRAM_API_KEY")
        if (openAiApiKey.isNullOrBlank() && deepgramApiKey.isNullOrBlank()) {
            Messages.showErrorDialog("API key not found in config.properties.", "Error")
            return
        }


        val thread = Thread {
            try {
                val transcript = DeepgramClient.transcribeAudio(audioData, deepgramApiKey)
                displayMessage("User", transcript)
                // Now send the transcript to OpenAI
                val response = OpenAIClient.getChatResponse(transcript, openAiApiKey)
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
            chatHistoryArea.append("$sender: $message\n")
        }
    }
}
