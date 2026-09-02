package com.sambabyte.gitblame.listener

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.sambabyte.gitblame.settings.GitBlameSettingsState
import java.util.concurrent.ConcurrentHashMap

/**
 * Attaches caret listeners to editors so moving the cursor refreshes line annotations when
 * "Show only on current line" is enabled.
 */
class GitBlameEditorListener : EditorFactoryListener {

    private val caretListeners = ConcurrentHashMap<Editor, CaretListener>()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        if (editor.project == null) return

        val listener = object : CaretListener {
            private var lastLine = -1

            override fun caretPositionChanged(e: CaretEvent) {
                val settings = GitBlameSettingsState.instance
                if (!settings.isEnabled || !settings.showOnlyOnCurrentLine) {
                    return
                }

                val currentLine = e.newPosition.line
                if (currentLine != lastLine) {
                    lastLine = currentLine
                    editor.contentComponent.repaint()
                }
            }
        }

        caretListeners[editor] = listener
        editor.caretModel.addCaretListener(listener)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor
        val listener = caretListeners.remove(editor)
        if (listener != null) {
            editor.caretModel.removeCaretListener(listener)
        }
    }
}
