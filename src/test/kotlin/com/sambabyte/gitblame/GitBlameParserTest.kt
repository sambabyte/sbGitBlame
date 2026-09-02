package com.sambabyte.gitblame

import com.sambabyte.gitblame.model.BlameInfo
import com.sambabyte.gitblame.service.GitBlameParser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GitBlameParserTest {

    @Test
    fun `test parse git blame line porcelain output`() {
        val sampleOutput = """
e89c7d41a5b81234567890abcdef1234567890ab 1 1 2
author Max Mustermann
author-mail <max@example.com>
author-time 1700000000
author-tz +0100
committer Max Mustermann
committer-mail <max@example.com>
committer-time 1700000000
committer-tz +0100
summary Add initial project configuration
filename build.gradle.kts
	plugins {
e89c7d41a5b81234567890abcdef1234567890ab 2 2
author Max Mustermann
author-mail <max@example.com>
author-time 1700000000
author-tz +0100
committer Max Mustermann
committer-mail <max@example.com>
committer-time 1700000000
committer-tz +0100
summary Add initial project configuration
filename build.gradle.kts
	    id("java")
0000000000000000000000000000000000000000 3 3 1
author Not Committed Yet
author-mail <not.committed.yet>
author-time 0
author-tz +0000
committer Not Committed Yet
committer-mail <not.committed.yet>
committer-time 0
committer-tz +0000
summary Version bump
filename build.gradle.kts
	    id("org.jetbrains.kotlin.jvm")
""".trimIndent()

        val parsed = GitBlameParser.parse(sampleOutput)

        assertEquals(3, parsed.size)

        val line0 = parsed[0]
        assertNotNull(line0)
        assertEquals("Max Mustermann", line0!!.author)
        assertEquals("max@example.com", line0.authorMail)
        assertEquals("e89c7d41", line0.shortHash)
        assertEquals("Add initial project configuration", line0.summary)
        assertFalse(line0.isNotCommitted)

        val line1 = parsed[1]
        assertNotNull(line1)
        assertEquals("Max Mustermann", line1!!.author)

        val line2 = parsed[2]
        assertNotNull(line2)
        assertTrue(line2!!.isNotCommitted)
    }

    @Test
    fun `test format text template`() {
        val blame = BlameInfo(
            commitHash = "abcdef1234567890",
            author = "Erika Musterfrau",
            authorMail = "erika@example.com",
            authorTime = 1672531199, // 2022-12-31
            summary = "Fix critical concurrency bug in cache synchronization module"
        )

        val formatted = blame.formatText(
            template = "{author}, {date} • {hash} • {message}",
            useRelativeDate = false,
            dateFormat = "yyyy-MM-dd",
            maxMessageLength = 30
        )

        assertTrue(formatted.contains("Erika Musterfrau"))
        assertTrue(formatted.contains("abcdef12"))
        assertTrue(formatted.contains("Fix critical concurrency bu..."))
    }

    @Test
    fun `test uncommitted blame text`() {
        val blame = BlameInfo.UNCOMMITTED
        val formatted = blame.formatText(
            template = "{author}, {date} • {hash} • {message}",
            useRelativeDate = true,
            dateFormat = "yyyy-MM-dd",
            maxMessageLength = 60
        )

        assertEquals("Not committed yet (Uncommitted changes)", formatted)
    }
}
