//package com.adgainai.springbooturlfullpath.selectaction
//
//
//import com.intellij.openapi.actionSystem.AnAction
//import com.intellij.openapi.actionSystem.AnActionEvent
//import com.intellij.openapi.actionSystem.CommonDataKeys
//import com.intellij.openapi.editor.Editor
//import com.intellij.openapi.editor.ScrollType
//import com.intellij.openapi.editor.SelectionModel
//import com.intellij.openapi.util.TextRange
//import com.intellij.psi.PsiClass
//import com.intellij.psi.util.PsiTreeUtil
//import com.intellij.psi.util.PsiUtilBase
//
//class SelectWholeClassAction : AnAction() {
//
//    override fun actionPerformed(e: AnActionEvent) {
//        val project = e.project ?: return
//        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
//        val psiFile = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
//
//        // 获取当前光标所在的类
//        val elementAtCaret = PsiUtilBase.getElementAtCaret(editor, project)
//        val psiClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java)
//
//        psiClass?.let { selectClassInEditor(editor, it) }
//    }
//
//    private fun selectClassInEditor(editor: Editor, psiClass: PsiClass) {
//        // 获取类的文本范围
//        val range = psiClass.textRange
//
//        // 在编辑器中设置选择范围
//        editor.selectionModel.setSelection(range.startOffset, range.endOffset)
//
//        // 可选: 滚动到选择的位置
//        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
//
//    }
//
//}