package com.adgainai.springbooturlfullpath

import com.google.common.collect.Lists
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiUtil
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotation
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.idea.base.analysis.api.utils.getSymbolContainingMemberDeclarations
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent


class KotlinMappingUrlProvider : CodeVisionProvider<Unit> {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Top
    override val id: String
        get() = "KotlinMappingUrlProvider"

    override val name: String
        get() = "Get Mapping URL For Kotlin"
    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = Lists.newArrayList()

    override fun computeCodeVision(editor: Editor, data: Unit): CodeVisionState {

        val project = editor.project ?: return CodeVisionState.Ready(emptyList())
        var entries = emptyList<Pair<TextRange, CodeVisionEntry>>()
        val settings = MyPluginProjectSettings.getInstance(project)

        // 使用 DumbService 等待索引就绪
        DumbService.getInstance(project).runReadActionInSmartMode {

            val psiFile = PsiUtil.getPsiFile(project, editor.virtualFile)

            if (psiFile.language.id == "kotlin") {
                // 处理 Kotlin 文件
                val ktFile = psiFile as KtFile

                analyze(ktFile) {
                    ktFile.declarations.forEach { declaration ->

                        val classSymbol = declaration.symbol

                        val requestMapping =
                            classSymbol.annotations.find { it ->
                                it.classId?.relativeClassName?.asString()?.contains("RequestMapping") ?: false
                            }

                        val classUrlPart = getAnnUrlForKt(requestMapping)

                        val prefix = settings.prefix

                        // 获取类中的所有函数
                        entries =
                            classSymbol.getSymbolContainingMemberDeclarations()?.memberScope?.callables?.asSequence()
                                ?.mapNotNull { it.getGetMappingUrl(classUrlPart, prefix) }
                                ?.toList() ?: emptyList()

                    }
                }
            }

        }


        return CodeVisionState.Ready(entries)
    }

    private fun KaCallableSymbol.getGetMappingUrl(
        computeUrl: List<String>,
        prefix: String
    ): Pair<TextRange, CodeVisionEntry>? {
        val supportedMappings = setOf("GetMapping", "PostMapping", "RequestMapping")

        val getMapping = annotations.find {
            supportedMappings.contains(
                it.classId?.relativeClassName?.asString()
            )
        } ?: return null

        val textRange = getMapping.psi?.textRange ?: return  null// 确保使用的是 com.intellij.openapi.util.TextRange

        val newTextRange: com.intellij.openapi.util.TextRange = com.intellij.openapi.util.TextRange(
            textRange.startOffset,
            textRange.endOffset
        )

        val url = getAnnUrlForKt(getMapping)


        val fullPathList = PathUtils.buildFullPaths(computeUrl, url, prefix)

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

        return Pair(newTextRange, secondCodeVision)

    }

    private fun KtNamedFunction.getGetMappingUrl(
        computeUrl: List<String>,
        prefix: String
    ): Pair<TextRange, CodeVisionEntry>? {
        val supportedMappings = setOf("GetMapping", "PostMapping", "RequestMapping")

        val getMapping = annotationEntries.find {
            it.shortName?.asString() in supportedMappings
        } ?: return null

        val textRange = getMapping.textRange  // 确保使用的是 com.intellij.openapi.util.TextRange

        val newTextRange: com.intellij.openapi.util.TextRange = com.intellij.openapi.util.TextRange(
            textRange.startOffset,
            textRange.endOffset
        )

        val url = getAnnUrl(getMapping)


        val fullPathList = PathUtils.buildFullPaths(computeUrl, url, prefix)

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

        return Pair(newTextRange, secondCodeVision)

    }

    private fun getAnnUrl(getMapping: KtAnnotationEntry?): List<String> {
        val arguments = getMapping?.valueArguments ?: return emptyList()

        // 先拿到所有 text
        val texts: List<String> = arguments.mapNotNull { arg ->
            arg.getArgumentExpression()?.text
        }

        // 再统一处理
        return PathUtils.doGetMappingUrls(texts)                   // 去重
    }

    private fun getAnnUrlForKt(getMapping: KaAnnotation?): List<String> {
        val arguments = getMapping?.arguments ?: return emptyList()

        // 先拿到所有 text
        val texts: List<String> = arguments.mapNotNull { arg ->
            arg.expression.sourcePsi?.text
        }

        // 再统一处理
        return PathUtils.doGetMappingUrls(texts)                   // 去重
    }


    private fun getClickHandler(text: String): (MouseEvent?, Editor) -> Unit {
        val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->

            val copyPasteManager = CopyPasteManager.getInstance()
            val stringSelection = StringSelection(text)
            copyPasteManager.setContents(stringSelection)
        }
        return clickHandler
    }


    override fun isAvailableFor(project: com.intellij.openapi.project.Project): Boolean {
        // 根据需要调整条件，以确定此提供者是否适用于特定项目
        return true
    }

    override fun precomputeOnUiThread(editor: Editor) {
//        TODO("Not yet implemented")
    }


}



