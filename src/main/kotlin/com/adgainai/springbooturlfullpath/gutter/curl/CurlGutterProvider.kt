package com.adgainai.springbooturlfullpath.gutter.curl


import com.adgainai.springbooturlfullpath.InlayHintsIcons
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent

class CurlGutterProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {

        if (element !is PsiMethod) return null

        val mapping = SpringMappingParser.parse(element) ?: return null

        val params = ParamParser.parse(element)

        return LineMarkerInfo(
            element,
            element.textRange,
            InlayHintsIcons.curl,
            { "Generate curl" },
            CurlClickHandler(mapping, params),
            Alignment.LEFT
        )
    }

    private class CurlClickHandler(
        val mapping: SpringMapping,
        val params: CurlParams
    ) : GutterIconNavigationHandler<PsiElement> {

        override fun navigate(e: MouseEvent?, elt: PsiElement?) {
            val text = CurlGenerator.generate(
                baseUrl = "http://localhost:8080",
                mapping = mapping,
                params = params
            )
            CopyPasteManager.getInstance().setContents(StringSelection(text))
        }
    }
}
