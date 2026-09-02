package com.sambabyte.gitblame.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.sambabyte.gitblame.model.BlameFileResult
import com.sambabyte.gitblame.model.BlameInfo
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class GitBlameService(private val project: Project) {

    private val LOG = Logger.getInstance(GitBlameService::class.java)

    // Cache: File Path -> BlameFileResult
    private val cache = ConcurrentHashMap<String, BlameFileResult>()
    // Set of currently executing file paths to avoid duplicate concurrent git processes
    private val inFlight = ConcurrentHashMap.newKeySet<String>()

    fun getBlame(virtualFile: VirtualFile, lineIndex: Int): BlameInfo? {
        if (!virtualFile.isInLocalFileSystem || project.isDisposed) {
            return null
        }

        val path = virtualFile.path
        val cached = cache[path]

        if (cached != null && cached.modificationStamp == virtualFile.modificationStamp) {
            return cached.getLineBlame(lineIndex)
        }

        // Trigger background fetch if not already in flight
        triggerAsyncFetch(virtualFile)

        // Return old cache if available while new one is loading
        return cached?.getLineBlame(lineIndex)
    }

    fun invalidate(virtualFile: VirtualFile) {
        cache.remove(virtualFile.path)
    }

    fun invalidateAll() {
        cache.clear()
        refreshAllEditors()
    }

    private fun triggerAsyncFetch(virtualFile: VirtualFile) {
        val path = virtualFile.path
        if (!inFlight.add(path)) {
            return
        }

        val modStamp = virtualFile.modificationStamp
        AppExecutorUtil.getAppExecutorService().execute {
            try {
                val blameResult = executeGitBlame(virtualFile, modStamp)
                cache[path] = blameResult

                // Request repaint on UI thread
                ApplicationManager.getApplication().invokeLater({
                    if (!project.isDisposed) {
                        refreshFileEditors(virtualFile)
                    }
                }, project.disposed)
            } catch (e: Exception) {
                LOG.debug("Failed to fetch git blame for ${virtualFile.path}: ${e.message}")
            } finally {
                inFlight.remove(path)
            }
        }
    }

    private fun executeGitBlame(virtualFile: VirtualFile, modStamp: Long): BlameFileResult {
        val ioFile = File(virtualFile.path)
        if (!ioFile.exists()) {
            return BlameFileResult(virtualFile.path, modStamp, emptyMap(), "File does not exist")
        }

        val repoRoot = findGitRepositoryRoot(ioFile.parentFile)
            ?: return BlameFileResult(virtualFile.path, modStamp, emptyMap(), "Not in a git repository")

        val relativePath = ioFile.relativeTo(repoRoot).path.replace('\\', '/')

        return try {
            val processBuilder = ProcessBuilder(
                "git",
                "blame",
                "--line-porcelain",
                "--",
                relativePath
            )
            processBuilder.directory(repoRoot)
            processBuilder.redirectErrorStream(true)

            val process = processBuilder.start()
            val outputLines = StringBuilder()

            BufferedReader(InputStreamReader(process.inputStream, Charsets.UTF_8)).use { reader ->
                var l: String?
                while (reader.readLine().also { l = it } != null) {
                    outputLines.append(l).append("\n")
                }
            }

            val completed = process.waitFor(10, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return BlameFileResult(virtualFile.path, modStamp, emptyMap(), "Git blame timed out")
            }

            if (process.exitValue() != 0) {
                return BlameFileResult(virtualFile.path, modStamp, emptyMap(), "Git blame exited with code ${process.exitValue()}")
            }

            val parsedLines = GitBlameParser.parse(outputLines.toString())
            BlameFileResult(virtualFile.path, modStamp, parsedLines)
        } catch (e: Exception) {
            LOG.warn("Error running git blame on ${virtualFile.path}", e)
            BlameFileResult(virtualFile.path, modStamp, emptyMap(), e.message)
        }
    }

    private fun findGitRepositoryRoot(dir: File?): File? {
        var current = dir
        while (current != null && current.exists()) {
            val gitDir = File(current, ".git")
            if (gitDir.exists()) {
                return current
            }
            current = current.parentFile
        }
        return null
    }

    private fun refreshFileEditors(virtualFile: VirtualFile) {
        val editors = EditorFactory.getInstance().allEditors
        for (editor in editors) {
            if (editor.isDisposed) continue
            val doc = editor.document
            val editorFile = ApplicationManager.getApplication().runReadAction(Computable {
                com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getFile(doc)
            })
            if (editorFile == virtualFile) {
                editor.contentComponent.repaint()
            }
        }
    }

    private fun refreshAllEditors() {
        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed) {
                EditorFactory.getInstance().allEditors.forEach { it.contentComponent.repaint() }
            }
        }
    }

    companion object {
        fun getInstance(project: Project): GitBlameService {
            return project.getService(GitBlameService::class.java)
        }
    }
}
