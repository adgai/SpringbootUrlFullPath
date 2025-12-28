package com.adgainai.springbooturlfullpath.autofile

/**
 * @author: codeman
 * @date: 2025/12/28 23:03
 **/
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

fun openOrCreateRootFile(manager: FileEditorManager, fileName: String) {
    val project: Project = manager.project
    val basePath = project.basePath ?: return

    val root = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return

    WriteCommandAction.runWriteCommandAction(project) {
        val target = root.findChild(fileName)
            ?: root.createChildData(project, fileName).apply {
                setBinaryContent(defaultContent(project).toByteArray(Charsets.UTF_8))
            }

        manager.openFile(target, true)
    }
}

private fun defaultContent(project: Project): String = """
    # ${project.name}

    该文件由 Codeman 插件自动创建并打开：
    - 仅在编辑区没有打开任何文件时触发
    - 你可以自由删除/修改

""".trimIndent()
