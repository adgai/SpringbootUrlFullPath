package com.adgainai.springbooturlfullpath.selectaction

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.SelectionModel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.util.PsiTreeUtil

class GetMethodBodyContentAtCaretAction : AnAction() {
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

            // 获取方法体（PsiCodeBlock）
            val methodBody: PsiCodeBlock? = method.body

            if (methodBody != null) {
                // 获取方法体的文本范围
                val methodBodyRange = methodBody.textRange

                // 获取编辑器的 SelectionModel
                val selectionModel: SelectionModel = editor.selectionModel

                // 设置选区为方法体的范围
                selectionModel.setSelection(methodBodyRange.startOffset, methodBodyRange.endOffset)

                // 确保光标位于选区内
                editor.caretModel.moveToOffset(methodBodyRange.startOffset)
            } else {
                println("方法没有方法体，无法选中")
            }
        } else {
            println("当前光标不在方法中")
        }
    }
}
