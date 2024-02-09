package com.adgainai.springbootapifullpath

import com.google.common.collect.Lists.newArrayList
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.squareup.wire.internal.newMutableList
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent

class GetMappingUrlProvider : CodeVisionProvider<Unit> {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = TODO("Not yet implemented")
    override val id: String
        get() = "GetMappingUrlProvider"

    override val name: String
        get() = "GetMapping URL"
    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = newMutableList()

//    override fun computeCodeVision(editor: Editor, data: Unit): CodeVisionState {
//
//        var entries: List<Pair<TextRange, CodeVisionEntry>>? = null;
//
//        ApplicationManager.getApplication().runReadAction {
//            // 现在这行代码在读取操作的上下文中安全执行
//            val psiFile = PsiUtil.getPsiFile(editor.project!!, editor.virtualFile)
//
//            // 在这里继续处理 psiFile
//            // 例如，执行对 psiFile 的读取操作和分析
////            val psiFile = PsiUtil.getPsiFile(editor.project!!, editor.virtualFile)
//            val methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java)
//            val classAnnotation = PsiTreeUtil.findChildrenOfType(psiFile, PsiAnnotation::class.java)
//
//
//            val requestMapping =
//                classAnnotation.find { it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping" }
//            if (requestMapping == null) {
//                entries = newArrayList()
//            } else {
//                val computeUrl = requestMapping?.computeUrl() ?: ""
//                entries = methods.asSequence()
//                    .mapNotNull { it.getGetMappingUrl(computeUrl) }
//                    .toList()
//            }
//
//        }
//
//
//
//        return CodeVisionState.Ready(entries!!)
//    }
//

    override fun computeCodeVision(editor: Editor, data: Unit): CodeVisionState {
        val project = editor.project ?: return CodeVisionState.Ready(emptyList())
        var entries = emptyList<Pair<TextRange, CodeVisionEntry>>()

        // 使用 DumbService 等待索引就绪
        DumbService.getInstance(project).runReadActionInSmartMode {
            val psiFile = PsiUtil.getPsiFile(project, editor.virtualFile)
            val methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java)
            val classAnnotation = PsiTreeUtil.findChildrenOfType(psiFile, PsiAnnotation::class.java)

            val requestMapping =
                classAnnotation.find { it.qualifiedName == "org.springframework.web.bind.annotation.RequestMapping" }
            val computeUrl = requestMapping?.computeUrl() ?: ""

            entries = methods.asSequence()
                .mapNotNull { it.getGetMappingUrl(computeUrl) }
                .toList()
        }

        return CodeVisionState.Ready(entries)
    }


    private fun PsiMethod.getGetMappingUrl(computeUrl: String): Pair<TextRange, CodeVisionEntry>? {
        val getMapping = annotations.find {
            it.qualifiedName == "org.springframework.web.bind.annotation.GetMapping"
                    || it.qualifiedName == "org.springframework.web.bind.annotation.PostMapping"
        }
            ?: return null

        val textRange = getMapping.textRange
        val url = getMapping.computeUrl() ?: return null

        val text = computeUrl.replace("\"", "") + url.replace("\"", "")

        val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->
            // 处理点击事件
            // 例如：显示一个对话框
            if (event != null) {

//                JOptionPane.showMessageDialog(null, text)
            }
            println("--------------------------")

            val copyPasteManager = CopyPasteManager.getInstance()
            val stringSelection = StringSelection(text)
            copyPasteManager.setContents(stringSelection)
        }

        val codeVisionEntry = ClickableTextCodeVisionEntry(
            text = text,
            providerId = id,
            onClick = clickHandler,  // 点击处理器
            icon = InlayHintsIcons.web, // 可以设置图标，如果需要的话
            tooltip = "Click here for more information", // 鼠标悬停时的描述
        )

        return Pair(textRange, codeVisionEntry)
//        return codeVisionEntry

//        return CodeVisionEntry.TextOnly(url, id)
    }

//    public fun CodeVisionEntry.TextOnly(url: String, id: String): CodeVisionEntry {
//        val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->
//            // 处理点击事件
//            // 例如：显示一个对话框
//            if (event != null) {
//
//                JOptionPane.showMessageDialog(null, url)
//            }
//            println("--------------------------")
//
//            val copyPasteManager = CopyPasteManager.getInstance()
//            val stringSelection = StringSelection(url)
//            copyPasteManager.setContents(stringSelection)
//        }
//        val codeVisionEntry = ClickableTextCodeVisionEntry(
//                text = url,
//                providerId = id,
//                onClick = clickHandler,  // 点击处理器
//                icon =Web, // 可以设置图标，如果需要的话
//                tooltip = "Click here for more information", // 鼠标悬停时的描述
//        )
//        return codeVisionEntry
//    }

    private fun PsiAnnotation.computeUrl(): String {
        // 这里的逻辑应该解析GetMapping的参数以及类级别的RequestMapping等
        // 简化为直接返回注解的值
        return findAttributeValue("value")?.text ?: ""
    }

    override fun isAvailableFor(project: com.intellij.openapi.project.Project): Boolean {
        // 根据需要调整条件，以确定此提供者是否适用于特定项目
        return true
    }

    override fun precomputeOnUiThread(editor: Editor) {
//        TODO("Not yet implemented")
    }

//    override fun precomputeOnUiThread(editor: Editor) {
//        TODO("Not yet implemented")
//    }

    // 其他必要的实现 ...
}

//val Web = load("/icon/web.svg", -130155959, 2)
//
//private fun load(path: String, cacheKey: Int, flags: Int): Icon {
//    return IconManager.getInstance().loadRasterizedIcon(path, AllIcons::class.java.classLoader, cacheKey, flags)
//}
fun runWhenIndexReady(project: Project, runnable: Runnable) {
    DumbService.getInstance(project).runWhenSmart(runnable)
}