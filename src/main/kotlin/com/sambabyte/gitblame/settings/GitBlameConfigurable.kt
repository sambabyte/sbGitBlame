package com.sambabyte.gitblame.settings

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.sambabyte.gitblame.model.BlameInfo
import java.awt.BorderLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class GitBlameConfigurable : Configurable {

    private var mainPanel: JPanel? = null
    private val enabledCheckBox = JBCheckBox("Enable sbGitBlame annotations")

    private val currentLineRadio = JBRadioButton("Show only on current cursor line (Recommended)")
    private val allLinesRadio = JBRadioButton("Show on all lines in editor")
    private val displayModeGroup = ButtonGroup()

    private val hideOnBlankLinesCheckBox = JBCheckBox("Hide annotations on empty / blank lines")
    private val italicCheckBox = JBCheckBox("Render text in italic font")
    private val relativeDateCheckBox = JBCheckBox("Use relative date format (e.g., '2 days ago', '3 hours ago')")
    private val formatTemplateField = JBTextField()
    private val dateFormatField = JBTextField()
    private val maxMessageLengthSpinner = JSpinner(SpinnerNumberModel(60, 10, 500, 5))
    private val prefixField = JBTextField()
    private val previewLabel = JBLabel()

    override fun getDisplayName(): String = "sbGitBlame"

    override fun createComponent(): JComponent {
        displayModeGroup.add(currentLineRadio)
        displayModeGroup.add(allLinesRadio)

        updatePreview()

        val listener = { updatePreview() }
        formatTemplateField.document.addDocumentListener(SimpleDocumentListener(listener))
        dateFormatField.document.addDocumentListener(SimpleDocumentListener(listener))
        relativeDateCheckBox.addActionListener {
            dateFormatField.isEnabled = !relativeDateCheckBox.isSelected
            updatePreview()
        }
        maxMessageLengthSpinner.addChangeListener { updatePreview() }
        prefixField.document.addDocumentListener(SimpleDocumentListener(listener))
        italicCheckBox.addActionListener { updatePreview() }

        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckBox)
            .addSeparator()
            .addLabeledComponent(JBLabel("Display Mode:"), currentLineRadio, 1, false)
            .addComponent(allLinesRadio)
            .addSeparator()
            .addComponent(hideOnBlankLinesCheckBox)
            .addComponent(italicCheckBox)
            .addSeparator()
            .addLabeledComponent(JBLabel("Format template:"), formatTemplateField, 1, false)
            .addComponent(
                JBLabel("<html><small>Placeholders: {author}, {date}, {hash}, {message}, {email}</small></html>").apply {
                    foreground = UIUtil.getContextHelpForeground()
                }
            )
            .addSeparator()
            .addComponent(relativeDateCheckBox)
            .addLabeledComponent(JBLabel("Absolute date format:"), dateFormatField, 1, false)
            .addLabeledComponent(JBLabel("Max message length:"), maxMessageLengthSpinner, 1, false)
            .addLabeledComponent(JBLabel("Prefix spacing:"), prefixField, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Live Preview:"), previewLabel, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        val wrapper = JPanel(BorderLayout())
        wrapper.border = JBUI.Borders.empty(10)
        wrapper.add(formPanel, BorderLayout.NORTH)
        mainPanel = wrapper

        return wrapper
    }

    private fun updatePreview() {
        val sampleBlame = BlameInfo(
            commitHash = "e89c7d41a5b8",
            author = "Alex Developer",
            authorMail = "alex@example.com",
            authorTime = System.currentTimeMillis() / 1000 - 86400 * 3, // 3 days ago
            summary = "Refactor user authentication and fix token expiration",
            isNotCommitted = false
        )
        val template = formatTemplateField.text.ifBlank { "{author}, {date} • {message} • {hash}" }
        val maxLen = (maxMessageLengthSpinner.value as? Int) ?: 60
        val text = sampleBlame.formatText(
            template = template,
            useRelativeDate = relativeDateCheckBox.isSelected,
            dateFormat = dateFormatField.text.ifBlank { "yyyy-MM-dd" },
            maxMessageLength = maxLen
        )
        val prefix = prefixField.text
        previewLabel.text = "<html><i>$prefix$text</i></html>"
    }

    override fun isModified(): Boolean {
        val settings = GitBlameSettingsState.instance
        return enabledCheckBox.isSelected != settings.isEnabled ||
                currentLineRadio.isSelected != settings.showOnlyOnCurrentLine ||
                hideOnBlankLinesCheckBox.isSelected != settings.hideOnBlankLines ||
                italicCheckBox.isSelected != settings.isItalic ||
                relativeDateCheckBox.isSelected != settings.useRelativeDate ||
                formatTemplateField.text != settings.formatTemplate ||
                dateFormatField.text != settings.dateFormat ||
                (maxMessageLengthSpinner.value as Int) != settings.maxMessageLength ||
                prefixField.text != settings.prefix
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = GitBlameSettingsState.instance
        settings.isEnabled = enabledCheckBox.isSelected
        settings.showOnlyOnCurrentLine = currentLineRadio.isSelected
        settings.hideOnBlankLines = hideOnBlankLinesCheckBox.isSelected
        settings.isItalic = italicCheckBox.isSelected
        settings.useRelativeDate = relativeDateCheckBox.isSelected
        settings.formatTemplate = formatTemplateField.text
        settings.dateFormat = dateFormatField.text
        settings.maxMessageLength = maxMessageLengthSpinner.value as Int
        settings.prefix = prefixField.text

        // Immediately repaint all open editors with the new settings
        EditorFactory.getInstance().allEditors.forEach {
            it.contentComponent.repaint()
        }
    }

    override fun reset() {
        val settings = GitBlameSettingsState.instance
        enabledCheckBox.isSelected = settings.isEnabled
        if (settings.showOnlyOnCurrentLine) {
            currentLineRadio.isSelected = true
        } else {
            allLinesRadio.isSelected = true
        }
        hideOnBlankLinesCheckBox.isSelected = settings.hideOnBlankLines
        italicCheckBox.isSelected = settings.isItalic
        relativeDateCheckBox.isSelected = settings.useRelativeDate
        formatTemplateField.text = settings.formatTemplate
        dateFormatField.text = settings.dateFormat
        maxMessageLengthSpinner.value = settings.maxMessageLength
        prefixField.text = settings.prefix
        dateFormatField.isEnabled = !settings.useRelativeDate
        updatePreview()
    }

    override fun disposeUIResources() {
        mainPanel = null
    }

    private class SimpleDocumentListener(private val onChange: () -> Unit) : javax.swing.event.DocumentListener {
        override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
        override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
        override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
    }
}
