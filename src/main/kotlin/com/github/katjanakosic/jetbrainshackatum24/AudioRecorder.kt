package com.github.katjanakosic.jetbrainshackatum24
import javax.sound.sampled.*
import java.io.IOException


class AudioRecorder(private val audioDataListener: (ByteArray) -> Unit) {
    private val audioFormat: AudioFormat = AudioFormat(16000F, 16, 1, true, false)
    private var targetDataLine: TargetDataLine? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    fun startRecording() {
        try {
            val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
            targetDataLine = AudioSystem.getLine(info) as TargetDataLine
            targetDataLine?.open(audioFormat)
            targetDataLine?.start()
            isRecording = true

            val buffer = ByteArray(4096)
            recordingThread = Thread {
                try {
                    while (isRecording) {
                        val bytesRead = targetDataLine?.read(buffer, 0, buffer.size) ?: 0
                        if (bytesRead > 0) {
                            val audioChunk = buffer.copyOf(bytesRead)
                            audioDataListener(audioChunk)
                        }
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
            recordingThread?.start()
        } catch (ex: LineUnavailableException) {
            ex.printStackTrace()
        }
    }

    fun stopRecording() {
        isRecording = false
        targetDataLine?.stop()
        targetDataLine?.close()
        try {
            recordingThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}

