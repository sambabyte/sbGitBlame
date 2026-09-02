package com.sambabyte.gitblame.painter

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.sambabyte.gitblame.service.GitBlameService
import com.sambabyte.gitblame.settings.GitBlameSettingsState
import java.awt.Color
import java.awt.Font

class GitBlameLinePainter : EditorLinePainter() {

    override fun getLineExtensions(
        project: Project,
        file: VirtualFile,
        lineNumber: Int
    ): Collection<LineExtensionInfo>? {
        val settings = GitBlameSettingsState.instance
        if (!settings.isEnabled) {
            return null
        }

        return ApplicationManager.getApplication().runReadAction(Computable {
            if (project.isDisposed || !file.isValid) {
                return@Computable null
            }

            // Check if the line is empty or whitespace-only
            val document = FileDocumentManager.getInstance().getCachedDocument(file)
                ?: FileDocumentManager.getInstance().getDocument(file)
            if (document != null) {
                if (lineNumber < 0 || lineNumber >= document.lineCount) {
                    return@Computable null
                }
                if (settings.hideOnBlankLines) {
                    val startOffset = document.getLineStartOffset(lineNumber)
                    val endOffset = document.getLineEndOffset(lineNumber)
                    val lineContent = document.charsSequence.subSequence(startOffset, endOffset)
                    if (lineContent.isBlank()) {
                        return@Computable null
                    }
                }
            }

            // If "Only current line" is enabled, check if this line contains the active caret in an open editor
            if (settings.showOnlyOnCurrentLine) {
                val isCurrentLine = isLineCurrentCaretLine(file, lineNumber)
                if (!isCurrentLine) {
                    return@Computable null
                }
            }

            val blameInfo = GitBlameService.getInstance(project).getBlame(file, lineNumber) ?: return@Computable null

            val formattedText = blameInfo.formatText(
                template = settings.formatTemplate,
                useRelativeDate = settings.useRelativeDate,
                dateFormat = settings.dateFormat,
                maxMessageLength = settings.maxMessageLength
            )

            val fullText = settings.prefix + formattedText
            val attributes = getTextAttributes(settings.isItalic)
            listOf(LineExtensionInfo(fullText, attributes))
        })
    }

    private fun isLineCurrentCaretLine(file: VirtualFile, lineNumber: Int): Boolean {
        val editors = EditorFactory.getInstance().allEditors
        for (editor in editors) {
            if (editor.isDisposed) continue
            val doc = editor.document
            val editorFile = FileDocumentManager.getInstance().getFile(doc)
            if (editorFile == file) {
                if (editor.caretModel.allCarets.any { it.logicalPosition.line == lineNumber }) {
                    return true
                }
            }
        }
        return false
    }

    private fun getTextAttributes(isItalic: Boolean): TextAttributes {
        val scheme = EditorColorsManager.getInstance().globalScheme

        val commentAttrs = scheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)
        val fgColor = commentAttrs?.foregroundColor
            ?: scheme.getColor(EditorColors.LINE_NUMBERS_COLOR)
            ?: JBColor(Color(130, 130, 130), Color(140, 140, 140))

        val fontStyle = if (isItalic) Font.ITALIC else Font.PLAIN
        return TextAttributes(fgColor, null, null, null, fontStyle)
    }
}
