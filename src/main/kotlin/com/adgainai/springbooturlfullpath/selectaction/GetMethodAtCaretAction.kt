package com.adgainai.springbooturlfullpath.selectaction

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class GetMethodAtCaretAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // 获取当前编辑器
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        if (editor == null || psiFile == null) {
            return
        }

        // 获取光标位置
        val caretModel: CaretModel = editor.caretModel
        val offset = caretModel.offset

        // 获取光标所在的 Psi 元素
        val elementAtCaret: PsiElement? = psiFile.findElementAt(offset)

        // 获取方法（如果有的话）
        val method = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod::class.java)

        if (method != null) {
            // 打印方法名，或者执行其他操作
            println("方法名：${method.name}")

            // 获取方法的范围
            val methodRange: TextRange = method.textRange

            // 获取编辑器的 SelectionModel
            val selectionModel: SelectionModel = editor.selectionModel

            // 设置选区为方法的范围
            selectionModel.setSelection(methodRange.startOffset, methodRange.endOffset)

        } else {
            println("当前光标不在方法中")
        }
    }
}