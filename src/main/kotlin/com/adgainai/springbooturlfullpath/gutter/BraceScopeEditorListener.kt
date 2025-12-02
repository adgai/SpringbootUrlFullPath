package com.adgainai.springbooturlfullpath.gutter;

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.util.Disposer

/**
 * @date 2025/11/13 10:43
 */
class BraceScopeEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor

        val provider = BraceScopeGutterProvider(editor)
        val disposable = editor.gutter.registerTextAnnotation(provider)

        val caretListener = BraceScopeCaretListener(editor, provider)
        editor.caretModel.addCaretListener(caretListener)

//        Disposer.register(editor.disposable, provider)
//        Disposer.register(editor.disposable, disposable)
//        Disposer.register(editor.disposable) {
//            editor.caretModel.removeCaretListener(caretListener)
//        }
    }
}
