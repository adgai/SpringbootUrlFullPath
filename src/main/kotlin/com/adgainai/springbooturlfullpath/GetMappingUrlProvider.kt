package com.adgainai.springbooturlfullpath

import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.squareup.wire.internal.newMutableList
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent

class GetMappingUrlProvider : CodeVisionProvider<Unit> {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Top
    override val id: String
        get() = "GetMappingUrlProvider"

    override val name: String
        get() = "GetMapping URL"
    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = newMutableList()

    override fun computeCodeVision(editor: Editor, data: Unit): CodeVisionState {
        val project = editor.project ?: return CodeVisionState.Ready(emptyList())
        var entries = emptyList<Pair<TextRange, CodeVisionEntry>>()
        val settings = MyPluginProjectSettings.getInstance(project)
        // 使用 DumbService 等待索引就绪
        DumbService.getInstance(project).runReadActionInSmartMode {
            val psiFile = PsiUtil.getPsiFile(project, editor.virtualFile)
            val methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java)
            val psiclasss = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
            if (CollectionUtils.isNotEmpty(psiclasss)){
                val firstPsiClass = psiclasss.first()
                val classAnnotation = firstPsiClass.annotations

                val requestMapping =
                    classAnnotation.find { it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping" }

                val classUrlPart = requestMapping?.computeUrl() ?: ""

                val prefix = settings.prefix

                entries = methods.asSequence()
                    .mapNotNull { it.getGetMappingUrl(classUrlPart,prefix) }
                    .toList()
            }

        }


        return CodeVisionState.Ready(entries)
    }


    private fun PsiMethod.getGetMappingUrl(computeUrl: String, prefix: String): Pair<TextRange, CodeVisionEntry>? {
        val getMapping = annotations.find {
            it.qualifiedName == "org.springframework.web.bind.annotation.GetMapping"
                    || it.qualifiedName == "org.springframework.web.bind.annotation.PostMapping"
                    || it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping"
        }
            ?: return null

        val textRange = getMapping.textRange
        val url = getMapping.computeUrl() ?: return null

        var text = computeUrl.replace("\"", "") + url.replace("\"", "")

        text = if (StringUtils.isBlank(prefix)) text else prefix + text

        val clickHandler: (MouseEvent?, Editor) -> Unit = getClickHandler(text)

        val codeVisionEntry = ClickableTextCodeVisionEntry(
            text = text,
            providerId = id,
            onClick = clickHandler,  // 点击处理器
            icon = InlayHintsIcons.web, // 可以设置图标，如果需要的话
            tooltip = "Click here for more information", // 鼠标悬停时的描述
        )

        return Pair(textRange, codeVisionEntry)

    }

    private fun getClickHandler(text: String): (MouseEvent?, Editor) -> Unit {
        val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->

            val copyPasteManager = CopyPasteManager.getInstance()
            val stringSelection = StringSelection(text)
            copyPasteManager.setContents(stringSelection)
        }
        return clickHandler
    }

    private fun PsiAnnotation.computeUrl(): String {
        // 这里的逻辑应该解析GetMapping的参数以及类级别的RequestMapping等
        // 简化为直接返回注解的值
        val path = findAttributeValue("path")?.text
        val value = findAttributeValue("value")?.text
        if (StringUtils.isBlank(value) || value == "{}") {
            return path.toString();
        } else {
            return value.toString()
        }

    }

    override fun isAvailableFor(project: com.intellij.openapi.project.Project): Boolean {
        // 根据需要调整条件，以确定此提供者是否适用于特定项目
        return true
    }

    override fun precomputeOnUiThread(editor: Editor) {
//        TODO("Not yet implemented")
    }

}


fun runWhenIndexReady(project: Project, runnable: Runnable) {
    DumbService.getInstance(project).runWhenSmart(runnable)
}