package com.adgainai.springbooturlfullpath

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseEvent

/**
 * @author: codeman
 * @date: 2025/8/31 0:19
 **/
class PathUtils {
    companion object {
        @JvmStatic
        fun buildFullPaths(
            classPaths: List<String>,
            methodPaths: List<String>,
            prefix: String = ""
        ): List<String> {
            if (classPaths.isEmpty()) {
                return methodPaths.map { it.cleanPath() }.toCollection(ArrayList())
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


        fun getClickHandler(text: String): (MouseEvent?, Editor) -> Unit {
            val clickHandler: (MouseEvent?, Editor) -> Unit = { event, editor ->

                val copyPasteManager = CopyPasteManager.getInstance()
                val stringSelection = StringSelection(text)
                copyPasteManager.setContents(stringSelection)
            }
            return clickHandler
        }


        fun doGetMappingUrls(texts: List<String>): List<String> {
            return texts.flatMap { text ->
                when {
                    text.startsWith("[") or text.startsWith("{") -> {
                        // 数组写法 ["/a","/b"]
                        text.removePrefix("[")
                            .removeSuffix("]")
                            .removePrefix("{")
                            .removeSuffix("}")
                            .split(",")
                            .map { it.trim().removePrefix("\"").removeSuffix("\"") }
                    }

                    text.startsWith("\"") -> listOf(text.removePrefix("\"").removeSuffix("\""))
                    else -> emptyList()
                }
            }
                .filter { it.isNotBlank() }   // 过滤空
                .distinct()
        }
    }
}
