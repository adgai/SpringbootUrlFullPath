//package com.adgainai.springbooturlfullpath
//
//import com.google.common.collect.Lists
//import com.intellij.codeInsight.codeVision.*
//import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
//import com.intellij.openapi.editor.Editor
//import com.intellij.openapi.ide.CopyPasteManager
//import com.intellij.openapi.project.DumbService
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.util.TextRange
//import com.intellij.psi.PsiAnnotation
//import com.intellij.psi.PsiClass
//import com.intellij.psi.PsiMethod
//import com.intellij.psi.util.PsiTreeUtil
//import com.intellij.psi.util.PsiUtil
//import org.apache.commons.collections.CollectionUtils
//import org.apache.commons.lang3.StringUtils
//import java.awt.datatransfer.StringSelection
//import java.awt.event.MouseEvent
//import java.util.Arrays
//
//class GetMappingUrlProviderV1 : CodeVisionProvider<Unit> {
//    override val defaultAnchor: CodeVisionAnchorKind
//        get() = CodeVisionAnchorKind.Top
//    override val id: String
//        get() = "GetMappingUrlProvider11111111111111"
//
//    override val name: String
//        get() = "GetMapping URL"
//    override val relativeOrderings: List<CodeVisionRelativeOrdering>
//        get() = Lists.newArrayList()
//
//    override fun computeCodeVision(editor: Editor, data: Unit): CodeVisionState {
//        val project = editor.project ?: return CodeVisionState.Ready(emptyList())
//        var entries = emptyList<Pair<TextRange, CodeVisionEntry>>()
//        val settings = MyPluginProjectSettings.getInstance(project)
//        // 使用 DumbService 等待索引就绪
//        DumbService.getInstance(project).runReadActionInSmartMode {
//            val psiFile = PsiUtil.getPsiFile(project, editor.virtualFile)
//            val methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java)
//            val psiclasss = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
//            if (CollectionUtils.isNotEmpty(psiclasss)) {
//                val firstPsiClass = psiclasss.first()
//                val classAnnotation = firstPsiClass.annotations
//
//                val requestMapping =
//                    classAnnotation.find { it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping" }
//
//                val classUrlPart = requestMapping?.computeUrl() ?: emptyList()
//
//                val prefix = settings.prefix
//
//                entries = methods
//                    .filter { it.isValid }  // 确保PSI元素有效
//                    .flatMap { method ->
//                        method.getGetMappingUrl(classUrlPart, prefix) ?: emptyList()
//                    }
//            }
//
//        }
//
//
//        return CodeVisionState.Ready(entries)
//    }
//
//    fun String.parseListFromBrackets(): List<String> {
//        return takeIf { contains("{") }
//            ?.replace("[{}\"]".toRegex(), "")
//            ?.split(",")
//            ?.filter { it.isNotBlank() }
//            ?: Arrays.asList(this)
//    }
//
//
//    private fun PsiMethod.getGetMappingUrl(
//        computeUrl: List<String>,
//        prefix: String
//    ): List<Pair<TextRange, CodeVisionEntry>>? {
//        val getMapping = annotations.find {
//            it.qualifiedName == "org.springframework.web.bind.annotation.GetMapping"
//                    || it.qualifiedName == "org.springframework.web.bind.annotation.PostMapping"
//                    || it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping"
//        }
//            ?: return null
//
//        val textRange = getMapping.textRange
//        val url = getMapping.computeUrl() ?: emptyList()
//
//        val fullPathList: List<String> = buildFullPaths(computeUrl, url, prefix)
//
//        val text = fullPathList.joinToString("\u21B5")
//
//        val map = fullPathList.mapIndexed { index, path ->
//            val clickHandler: (MouseEvent?, Editor) -> Unit = getClickHandler(path)
//
//            val entry = ClickableTextCodeVisionEntry(
//                text = path,  // 使用实际的path而不是外部的text变量
//                providerId = id,  // 确保唯一性
//                onClick = clickHandler,
//                icon = InlayHintsIcons.web,
//                tooltip = "Click to copy: $path"  // 显示完整路径
//            )
//
//            // 计算偏移量，避免重叠
//            val offset = index * 1  // 每行增加2个字符的偏移
//            TextRange(textRange.startOffset + offset, textRange.endOffset + offset) to entry
//        }
//
//        return map
//
//    }
//
//    fun buildFullPaths(classPaths: List<String>, methodPaths: List<String>, prefix: String = ""): List<String> {
//        return buildList {
//            classPaths.forEach { classPath ->
//                methodPaths.forEach { methodPath ->
//                    add(buildString {
//                        if (prefix.isNotBlank()) append(prefix)
//                        append(classPath.cleanPath())
//                        append(methodPath.cleanPath())
//                    })
//                }
//            }
//        }
//    }
//
//    private fun String.cleanPath(): String {
//        return replace("\"", "").let { path ->
//            when {
//                path.isEmpty() -> ""
//                path.startsWith("/") -> path
//                else -> "/$path"
//            }
//        }
//    }
//
//    private fun getClickHandler(text: String): (MouseEvent?, Editor) -> Unit {
//        val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->
//
//            val copyPasteManager = CopyPasteManager.getInstance()
//            val stringSelection = StringSelection(text)
//            copyPasteManager.setContents(stringSelection)
//        }
//        return clickHandler
//    }
//
//    private fun PsiAnnotation.computeUrl(): List<String> {
//        // 这里的逻辑应该解析GetMapping的参数以及类级别的RequestMapping等
//        // 简化为直接返回注解的值
//        val path = findAttributeValue("path")?.text
//        val value = findAttributeValue("value")?.text
//        if (StringUtils.isBlank(value) || value == "{}") {
//            return listOf(path.toString());
//        } else {
//            return value?.parseListFromBrackets() ?: emptyList()
//        }
//
//    }
//
//    override fun isAvailableFor(project: Project): Boolean {
//        // 根据需要调整条件，以确定此提供者是否适用于特定项目
//        return true
//    }
//
//    override fun precomputeOnUiThread(editor: Editor) {
////        TODO("Not yet implemented")
//    }
//
//}