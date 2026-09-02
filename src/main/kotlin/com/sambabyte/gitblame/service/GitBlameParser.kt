package com.sambabyte.gitblame.service

import com.sambabyte.gitblame.model.BlameInfo
import java.io.BufferedReader
import java.io.StringReader

/**
 * Parser for `git blame --line-porcelain` output.
 */
object GitBlameParser {

    fun parse(porcelainOutput: String): Map<Int, BlameInfo> {
        val reader = BufferedReader(StringReader(porcelainOutput))
        return parse(reader)
    }

    fun parse(reader: BufferedReader): Map<Int, BlameInfo> {
        val result = HashMap<Int, BlameInfo>()

        var line: String? = reader.readLine()
        while (line != null) {
            val headerParts = line.trim().split(" ")
            if (headerParts.size < 3) {
                line = reader.readLine()
                continue
            }

            val commitHash = headerParts[0]
            val finalLineNo = headerParts[2].toIntOrNull() ?: 1
            // Convert to 0-based index for IntelliJ Editor
            val zeroBasedLine = finalLineNo - 1

            var author = ""
            var authorMail = ""
            var authorTime = 0L
            var authorTz = ""
            var committer = ""
            var committerMail = ""
            var committerTime = 0L
            var summary = ""
            var isNotCommitted = commitHash.all { it == '0' } || author == "Not Committed Yet"

            // Read the properties until the tab-prefixed code line
            line = reader.readLine()
            while (line != null && !line.startsWith("\t")) {
                val spaceIdx = line.indexOf(' ')
                if (spaceIdx > 0) {
                    val key = line.substring(0, spaceIdx)
                    val value = line.substring(spaceIdx + 1)
                    when (key) {
                        "author" -> {
                            author = value
                            if (value.equals("Not Committed Yet", ignoreCase = true)) {
                                isNotCommitted = true
                            }
                        }
                        "author-mail" -> authorMail = value.removeSurrounding("<", ">")
                        "author-time" -> authorTime = value.toLongOrNull() ?: 0L
                        "author-tz" -> authorTz = value
                        "committer" -> committer = value
                        "committer-mail" -> committerMail = value.removeSurrounding("<", ">")
                        "committer-time" -> committerTime = value.toLongOrNull() ?: 0L
                        "summary" -> summary = value
                    }
                }
                line = reader.readLine()
            }

            if (isNotCommitted || commitHash.all { it == '0' }) {
                result[zeroBasedLine] = BlameInfo.UNCOMMITTED
            } else {
                result[zeroBasedLine] = BlameInfo(
                    commitHash = commitHash,
                    author = author,
                    authorMail = authorMail,
                    authorTime = authorTime,
                    authorTimeZone = authorTz,
                    committer = committer,
                    committerMail = committerMail,
                    committerTime = committerTime,
                    summary = summary,
                    isNotCommitted = false
                )
            }

            // Move to next line
            line = reader.readLine()
        }

        return result
    }
}
