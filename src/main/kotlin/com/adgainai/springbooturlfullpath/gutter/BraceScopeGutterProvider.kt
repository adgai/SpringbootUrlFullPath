package com.adgainai.springbooturlfullpath.gutter

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.TextAnnotationGutterProvider
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.JBColor
import java.awt.Color

class BraceScopeGutterProvider(private val editor: Editor) :
    TextAnnotationGutterProvider, Disposable {


    private val markedLines = hashSetOf<Int>()

    private val color = JBColor(Color(120, 150, 255), Color(100, 130, 240))

    /** 更新 gutter 标记（竖线）*/
    fun updateScope(startOffset: Int, endOffset: Int) {
        markedLines.clear()
        val doc = editor.document
        val start = doc.getLineNumber(startOffset)
        val end = doc.getLineNumber(endOffset)

        for (line in (start + 1) until end) {
            markedLines.add(line)
        }

        refreshGutter()

    }

    /** 清除标记 */
    fun clear() {
        markedLines.clear()
        refreshGutter()
    }

    private fun refreshGutter() {
        // 优先用 EditorEx 的 gutterComponentEx
        val ex = editor as? EditorEx
        ex?.gutterComponentEx?.revalidateMarkup()
            ?: editor.contentComponent.repaint() // 兜底刷新一下整块
    }

    override fun getLineText(p0: Int, p1: Editor?): String? {
        return " "
    }

    override fun getToolTip(
        p0: Int,
        p1: Editor?
    ): @NlsContexts.Tooltip String? {
        return ""
    }

    override fun getStyle(
        p0: Int,
        p1: Editor?
    ): EditorFontType? {
        return EditorFontType.PLAIN
    }

    override fun getColor(
        p0: Int,
        p1: Editor?
    ): ColorKey? {
        return null
    }

    /** 背景颜色（这里可以自定义高亮前 N 行） */
    override fun getBgColor(line: Int, editor: Editor?): Color? {
        return if (markedLines.contains(line))
            JBColor(
                Color(167, 192, 128)
                ,
                Color(40, 50, 60)
            )
        else
            null
    }

    override fun getPopupActions(
        p0: Int,
        p1: Editor?
    ): List<AnAction?>? {
        return  emptyList()
    }


    override fun gutterClosed() {

    }

    override fun dispose() {

    }

}
