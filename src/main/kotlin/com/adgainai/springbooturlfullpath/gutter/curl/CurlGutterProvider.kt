package com.adgainai.springbooturlfullpath.gutter.curl

import com.adgainai.springbooturlfullpath.InlayHintsIcons
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent

class CurlGutterProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {

        // ★ 只处理注解
        val ann = element as? PsiAnnotation ?: return null

        // ★ 判断是否是 Spring Mapping 注解
        val qn = ann.qualifiedName ?: return null
        if (!qn.startsWith("org.springframework.web.bind.annotation.")) return null

        val short = qn.substringAfterLast(".")
        if (short !in setOf(
                "GetMapping",
                "PostMapping",
                "PutMapping",
                "DeleteMapping",
                "PatchMapping",
                "RequestMapping"
            )
        ) return null

        // ★ 注解对应的方法
        val method = ann.parentOfType<PsiMethod>() ?: return null

        val mapping = SpringMappingParser.parse(method) ?: return null
        val params = ParamParser.parse(method)

        // ★ 图标 anchor 改到 注解 上
        return LineMarkerInfo(
            /* anchor element */
            ann,
            ann.textRange,
            InlayHintsIcons.curl,
            { "Generate curl" },
            CurlClickHandler(element.project,mapping, params),
            Alignment.LEFT
        )
    }

    private class CurlClickHandler(
        val p: Project,
        val mapping: SpringMapping,
        val params: CurlParams
    ) : GutterIconNavigationHandler<PsiElement> {

        override fun navigate(e: MouseEvent?, elt: PsiElement?) {

            val port = elt?.let { SpringBootPortResolver.resolve(it.project) }

            val text = CurlGenerator.generate(
                project = p,
                baseUrl = "http://localhost:$port",
                mapping = mapping,
                params = params
            )
            CopyPasteManager.getInstance().setContents(StringSelection(text))
        }
    }
}
