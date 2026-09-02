package com.sambabyte.gitblame.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.editor.EditorFactory
import com.sambabyte.gitblame.settings.GitBlameSettingsState

class ToggleGitBlameAction : ToggleAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return GitBlameSettingsState.instance.isEnabled
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        GitBlameSettingsState.instance.isEnabled = state
        // Repaint all open editors
        EditorFactory.getInstance().allEditors.forEach {
            it.contentComponent.repaint()
        }
    }
}
