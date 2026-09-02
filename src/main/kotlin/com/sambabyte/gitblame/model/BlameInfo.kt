package com.sambabyte.gitblame.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Represents git blame metadata for a specific line.
 */
data class BlameInfo(
    val commitHash: String,
    val author: String,
    val authorMail: String,
    val authorTime: Long, // unix timestamp in seconds
    val authorTimeZone: String = "",
    val committer: String = "",
    val committerMail: String = "",
    val committerTime: Long = 0L,
    val summary: String = "",
    val isNotCommitted: Boolean = false
) {
    val shortHash: String
        get() = if (commitHash.length > 8) commitHash.substring(0, 8) else commitHash

    /**
     * Formats relative date like "just now", "5 minutes ago", "3 hours ago", "2 days ago", "1 month ago", "2 years ago"
     */
    fun formatRelativeDate(): String {
        if (authorTime <= 0 || isNotCommitted) return "Not committed yet"
        val commitInstant = Instant.ofEpochSecond(authorTime)
        val now = Instant.now()
        
        val seconds = ChronoUnit.SECONDS.between(commitInstant, now)
        if (seconds < 0) return "in the future"
        if (seconds < 60) return "just now"
        
        val minutes = ChronoUnit.MINUTES.between(commitInstant, now)
        if (minutes < 60) return if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
        
        val hours = ChronoUnit.HOURS.between(commitInstant, now)
        if (hours < 24) return if (hours == 1L) "1 hour ago" else "$hours hours ago"
        
        val days = ChronoUnit.DAYS.between(commitInstant, now)
        if (days < 30) return if (days == 1L) "1 day ago" else "$days days ago"
        
        val months = days / 30
        if (months < 12) return if (months == 1L) "1 month ago" else "$months months ago"
        
        val years = days / 365
        return if (years == 1L) "1 year ago" else "$years years ago"
    }

    /**
     * Formats absolute date using the provided pattern.
     */
    fun formatAbsoluteDate(pattern: String = "yyyy-MM-dd"): String {
        if (authorTime <= 0 || isNotCommitted) return "Not committed yet"
        return try {
            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(authorTime), ZoneId.systemDefault())
            dateTime.format(DateTimeFormatter.ofPattern(pattern))
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    /**
     * Formats the blame line according to the user template.
     * Supported placeholders:
     * - {author}
     * - {date}
     * - {hash}
     * - {message} or {comment} or {summary}
     * - {email}
     */
    fun formatText(
        template: String,
        useRelativeDate: Boolean,
        dateFormat: String,
        maxMessageLength: Int
    ): String {
        if (isNotCommitted) {
            return "Not committed yet (Uncommitted changes)"
        }

        val dateStr = if (useRelativeDate) formatRelativeDate() else formatAbsoluteDate(dateFormat)
        var msg = summary.trim()
        if (maxMessageLength > 0 && msg.length > maxMessageLength) {
            msg = msg.take(maxMessageLength - 3) + "..."
        }

        return template
            .replace("{author}", author)
            .replace("{date}", dateStr)
            .replace("{hash}", shortHash)
            .replace("{message}", msg)
            .replace("{comment}", msg)
            .replace("{summary}", msg)
            .replace("{email}", authorMail)
    }

    companion object {
        val UNCOMMITTED = BlameInfo(
            commitHash = "0000000000000000000000000000000000000000",
            author = "You",
            authorMail = "",
            authorTime = 0L,
            summary = "Uncommitted changes",
            isNotCommitted = true
        )
    }
}
