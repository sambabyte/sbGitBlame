package com.sambabyte.gitblame.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for Git Line Blame.
 */
@State(
    name = "com.sambabyte.gitblame.settings.GitBlameSettingsState",
    storages = [Storage("GitLineBlameSettings.xml")]
)
class GitBlameSettingsState : PersistentStateComponent<GitBlameSettingsState> {

    var isEnabled: Boolean = true
    var showOnlyOnCurrentLine: Boolean = true
    var hideOnBlankLines: Boolean = true
    var formatTemplate: String = "{author}, {date} • {message} • {hash}"
    var useRelativeDate: Boolean = true
    var dateFormat: String = "yyyy-MM-dd HH:mm"
    var maxMessageLength: Int = 60
    var prefix: String = "    "
    var isItalic: Boolean = true

    override fun getState(): GitBlameSettingsState = this

    override fun loadState(state: GitBlameSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: GitBlameSettingsState
            get() = ApplicationManager.getApplication().getService(GitBlameSettingsState::class.java)
    }
}
