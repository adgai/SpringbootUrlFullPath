package com.adgainai.springbooturlfullpath.gutter


import com.adgainai.springbooturlfullpath.pluginconfig.SfPluginProjectSettings
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil

class BraceScopeCaretListener(
    private val editor: Editor,
    private val provider: BraceScopeGutterProvider
) : CaretListener {

    override fun caretPositionChanged(event: CaretEvent) {
        val project = editor.project ?: return
        val psiFile = PsiDocumentManager.getInstance(project)
            .getPsiFile(editor.document) ?: return

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: run {
            provider.clear()
            return
        }


        val gutterHighlightCurrentBlock = SfPluginProjectSettings.instance.gutterHighlightCurrentBlock
        if (gutterHighlightCurrentBlock != true) {
            return
        }


        // 找到最近的 { ... } 块
        val block = PsiTreeUtil.getParentOfType(element, PsiCodeBlock::class.java)

        if (block == null) {
            provider.clear()
        } else {
            provider.updateScope(
                block.textRange.startOffset,
                block.textRange.endOffset
            )
        }
    }


}
