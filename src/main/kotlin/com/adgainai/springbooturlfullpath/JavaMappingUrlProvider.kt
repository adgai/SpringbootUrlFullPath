package com.adgainai.springbooturlfullpath

import com.google.common.collect.Lists
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.apache.commons.collections.CollectionUtils
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent
import java.util.*

class JavaMappingUrlProvider : CodeVisionProvider<Unit> {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Top
    override val id: String
        get() = "JavaMappingUrlProvider"

    override val name: String
        get() = "Get Mapping URL For Java"
    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = Lists.newArrayList()

    override fun computeCodeVision(editor: Editor, data: Unit): CodeVisionState {
        val project = editor.project ?: return CodeVisionState.Ready(emptyList())
        var entries = emptyList<Pair<TextRange, CodeVisionEntry>>()
        val settings = MyPluginProjectSettings.getInstance(project)
        // 使用 DumbService 等待索引就绪
        DumbService.getInstance(project).runReadActionInSmartMode {
            val psiFile = PsiUtil.getPsiFile(project, editor.virtualFile)
            val methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java)
            val psiclasss = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
            if (CollectionUtils.isNotEmpty(psiclasss)) {
                val firstPsiClass = psiclasss.first()
                val classAnnotation = firstPsiClass.annotations

                val requestMapping =
                    classAnnotation.find { it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping" }

                val classUrlPart = requestMapping?.getAnnUrl() ?: emptyList()

                val prefix = settings.prefix

                entries = methods.asSequence()
                    .mapNotNull { it.getGetMappingUrl(classUrlPart, prefix) }
                    .toList()
            }

        }


        return CodeVisionState.Ready(entries)
    }

    fun String.parseListFromBrackets(): List<String> {
        return takeIf { contains("{") }
            ?.replace("[{}\"]".toRegex(), "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: Arrays.asList(this)
    }


    private fun PsiMethod.getGetMappingUrl(
        computeUrl: List<String>,
        prefix: String
    ): Pair<TextRange, CodeVisionEntry>? {
        val getMapping = annotations.find {
            it.qualifiedName == "org.springframework.web.bind.annotation.GetMapping"
                    || it.qualifiedName == "org.springframework.web.bind.annotation.PostMapping"
                    || it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping"
        }
            ?: return null

        val textRange = getMapping.textRange
        val url = getMapping.getAnnUrl() ?: emptyList()

        val fullPathList: List<String> = buildFullPaths(computeUrl, url, prefix)

        val text = fullPathList.joinToString("---")
        val textC = fullPathList.joinToString("\n")

        val clickHandler: (MouseEvent?, Editor) -> Unit = getClickHandler(textC)

        val secondCodeVision = ClickableTextCodeVisionEntry(
            text = text,
            providerId = id, // 唯一ID
            onClick = clickHandler,  // 点击处理器
            icon = InlayHintsIcons.web, // 可以设置图标，如果需要的话
            tooltip = "Click here for more information", // 鼠标悬停时的描述
        )

        return Pair(textRange, secondCodeVision)

    }

    fun buildFullPaths(classPaths: List<String>, methodPaths: List<String>, prefix: String = ""): List<String> {
        if (classPaths.isEmpty()) {
            return methodPaths.map { it -> it.cleanPath() }.toCollection(ArrayList())
        }
        return buildList {
            classPaths.forEach { classPath ->
                methodPaths.forEach { methodPath ->
                    add(buildString {
                        if (prefix.isNotBlank()) append(prefix)
                        append(classPath.cleanPath())
                        append(methodPath.cleanPath())
                    })
                }
            }
        }
    }

    private fun String.cleanPath(): String {
        return replace("\"", "").let { path ->
            when {
                path.isEmpty() -> ""
                path.startsWith("/") -> path
                else -> "/$path"
            }
        }
    }

    private fun getClickHandler(text: String): (MouseEvent?, Editor) -> Unit {
        val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->

            val copyPasteManager = CopyPasteManager.getInstance()
            val stringSelection = StringSelection(text)
            copyPasteManager.setContents(stringSelection)
        }
        return clickHandler
    }

    private fun PsiAnnotation.getAnnUrl(): List<String> {

        val path = findAttributeValue("path")?.text?.takeIf { it != "{}" } ?: ""
        val value = findAttributeValue("value")?.text?.takeIf { it != "{}" } ?: ""

        val urls = listOf(path, value).filter { it.isNotBlank() }

        return PathUtils.doGetMappingUrls(urls)

    }

    override fun isAvailableFor(project: com.intellij.openapi.project.Project): Boolean {
        // 根据需要调整条件，以确定此提供者是否适用于特定项目
        return true
    }

    override fun precomputeOnUiThread(editor: Editor) {
//        TODO("Not yet implemented")
    }

}