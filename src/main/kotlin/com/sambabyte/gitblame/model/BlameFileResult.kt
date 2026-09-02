package com.sambabyte.gitblame.model

/**
 * Result of git blame for a whole file.
 * Line numbers are 0-indexed for IntelliJ Editor compatibility.
 */
data class BlameFileResult(
    val filePath: String,
    val modificationStamp: Long,
    val lines: Map<Int, BlameInfo> = emptyMap(),
    val error: String? = null
) {
    fun getLineBlame(zeroBasedLineIndex: Int): BlameInfo? {
        return lines[zeroBasedLineIndex]
    }

    val isSuccessful: Boolean
        get() = error == null && lines.isNotEmpty()
}
