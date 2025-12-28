package com.adgainai.springbooturlfullpath.autofile

/**
 * @author: codeman
 * @date: 2025/12/28 22:12
 **/
import com.intellij.openapi.fileTypes.FileType
import javax.swing.Icon

object DefaultWelcomeFileType : FileType {

    override fun getName() = "CodemanWelcome"
    override fun getDescription() = "Codeman Welcome Page"
    override fun getDefaultExtension() = ""
    override fun getIcon(): Icon? = null
    override fun isBinary() = false
    override fun isReadOnly() = true
}