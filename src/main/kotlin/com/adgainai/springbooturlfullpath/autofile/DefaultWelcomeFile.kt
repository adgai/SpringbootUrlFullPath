package com.adgainai.springbooturlfullpath.autofile

/**
 * @author: codeman
 * @date: 2025/12/28 22:07
 **/

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileSystem
import java.io.InputStream
import java.io.OutputStream

class DefaultWelcomeFile(
    private val project: Project
) : VirtualFile() {

    override fun getName() = "Codeman Welcome"

    override fun getFileSystem(): VirtualFileSystem =
        DefaultWelcomeFileSystem

    override fun getPath() = "codeman://welcome"

    override fun isWritable() = false
    override fun isDirectory() = false
    override fun isValid() = true
    override fun getParent(): VirtualFile? {
        TODO("Not yet implemented")
    }

    override fun getChildren(): Array<out VirtualFile?>? {
        TODO("Not yet implemented")
    }

    override fun getOutputStream(p0: Any?, p1: Long, p2: Long): OutputStream {
        TODO("Not yet implemented")
    }

    override fun getInputStream(): InputStream =
        welcomeContent().byteInputStream()

    override fun contentsToByteArray(): ByteArray =
        welcomeContent().toByteArray()

    override fun getLength(): Long =
        welcomeContent().length.toLong()

    override fun getTimeStamp() = 0L

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {}

    private fun welcomeContent(): String = """
        🚀 Codeman Plugin

        欢迎使用 Codeman 插件！

        ✔ 自动打开默认文件
        ✔ 可扩展为说明 / 使用文档
        ✔ 可集成你的 SpringBoot / URL 插件

        Project: ${project.name}
        
        Tips:
        - 你可以把这里换成 HTML / Markdown
        - 也可以根据项目类型动态生成内容
    """.trimIndent()
}

/** 一个最小可用的 FileSystem */
object DefaultWelcomeFileSystem : VirtualFileSystem() {
    override fun getProtocol() = "codeman"
    override fun findFileByPath(path: String) = null
    override fun refresh(asynchronous: Boolean) {}
    override fun refreshAndFindFileByPath(path: String) = null
    override fun addVirtualFileListener(p0: VirtualFileListener) {
        TODO("Not yet implemented")
    }

    override fun removeVirtualFileListener(p0: VirtualFileListener) {
        TODO("Not yet implemented")
    }

    override fun deleteFile(p0: Any?, p1: VirtualFile) {
        TODO("Not yet implemented")
    }

    override fun moveFile(
        p0: Any?,
        p1: VirtualFile,
        p2: VirtualFile
    ) {
        TODO("Not yet implemented")
    }

    override fun renameFile(p0: Any?, p1: VirtualFile, p2: String) {
        TODO("Not yet implemented")
    }

    override fun createChildFile(
        p0: Any?,
        p1: VirtualFile,
        p2: String
    ): VirtualFile {
        TODO("Not yet implemented")
    }

    override fun createChildDirectory(
        p0: Any?,
        p1: VirtualFile,
        p2: String
    ): VirtualFile {
        TODO("Not yet implemented")
    }

    override fun copyFile(
        p0: Any?,
        p1: VirtualFile,
        p2: VirtualFile,
        p3: String
    ): VirtualFile {
        TODO("Not yet implemented")
    }

    override fun isReadOnly(): Boolean {
        TODO("Not yet implemented")
    }
}
