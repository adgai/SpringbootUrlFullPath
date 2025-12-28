package com.adgainai.springbooturlfullpath.autofile

/**
 * @author: codeman
 * @date: 2025/12/28 22:39
 **/

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

class DefaultFileEditorListener(
    private val project: Project,
    private val connection: com.intellij.util.messages.MessageBusConnection
) : FileEditorManagerListener {


    override fun fileOpened(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) {
        // 有文件被打开，说明无需兜底
        dispose()
    }

    override fun fileClosed(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) {
        tryOpenIfNeeded()
    }

    private fun tryOpenIfNeeded() {

        ApplicationManager.getApplication().invokeLater {
            val manager = FileEditorManager.getInstance(project)

            if (manager.openFiles.isEmpty()) {
                openDefaultFile(manager)
                dispose()
            }
        }
    }

    private fun openDefaultFile(manager: FileEditorManager) {
        val project = manager.project
        val basePath = project.basePath ?: return

        val vfs = LocalFileSystem.getInstance()
        val root = vfs.findFileByPath(basePath) ?: return

        val fileName = "README.md"

        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            val target = root.findChild(fileName)
                ?: root.createChildData(this, fileName).apply {
                    setBinaryContent(defaultContent(project).toByteArray())
                }

            manager.openFile(target, true)
        }
    }

    private fun defaultContent(project: Project): String = """
    # ${project.name}

    欢迎使用 Codeman 插件 👋

    - 本文件由 Codeman 插件自动创建
    - 你可以放心删除或修改
    - 仅在「编辑区无文件」时自动打开

    Created at: ${java.time.LocalDateTime.now()}
""".trimIndent()

    private fun dispose() {
        connection.disconnect()
    }
}
