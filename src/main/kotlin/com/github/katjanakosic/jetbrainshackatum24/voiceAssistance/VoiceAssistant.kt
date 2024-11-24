package com.github.katjanakosic.jetbrainshackatum24.voiceAssistance

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory


class VoiceAssistant : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.project ?: return

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Voice Assistant")
        if (toolWindow != null) {
            toolWindow.show()
        } else {
            Messages.showErrorDialog(project, "Voice Assistant Tool Window not found.", "Error")
        }
    }
}

class VoiceAssistantToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val voiceAssistantPanel = VoiceAssistantPanel()
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(voiceAssistantPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

