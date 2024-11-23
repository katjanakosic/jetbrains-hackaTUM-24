package com.github.katjanakosic.jetbrainshackatum24
import javax.sound.sampled.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class AudioRecorder {
    private val audioFormat: AudioFormat = AudioFormat(16000F, 16, 1, true, false)
    private var targetDataLine: TargetDataLine? = null
    private val outputStream = ByteArrayOutputStream()
    private var recordingThread: Thread? = null

    fun startRecording() {
        try {
            val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
            targetDataLine = AudioSystem.getLine(info) as TargetDataLine
            targetDataLine?.open(audioFormat)
            targetDataLine?.start()

            val buffer = ByteArray(4096)
            recordingThread = Thread {
                try {
                    while (targetDataLine?.isOpen == true) {
                        val bytesRead = targetDataLine?.read(buffer, 0, buffer.size) ?: 0
                        outputStream.write(buffer, 0, bytesRead)
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

    fun stopRecording(): ByteArray {
        targetDataLine?.stop()
        targetDataLine?.close()
        recordingThread?.join()
        return outputStream.toByteArray()
    }
}
