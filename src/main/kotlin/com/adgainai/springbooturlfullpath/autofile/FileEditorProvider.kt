package com.adgainai.springbooturlfullpath.autofile

/**
 * @author: codeman
 * @date: 2025/12/28 22:14
 **/

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class DefaultWelcomeFileEditorProvider : FileEditorProvider {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file is DefaultWelcomeFile
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return TextEditorProvider.getInstance().createEditor(project, file)
    }

    override fun getEditorTypeId() = "codeman-welcome-editor"

    override fun getPolicy() = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
}
