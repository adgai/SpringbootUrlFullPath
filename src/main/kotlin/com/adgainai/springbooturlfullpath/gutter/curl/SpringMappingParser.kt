package com.adgainai.springbooturlfullpath.gutter.curl

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod

data class SpringMapping(
    val httpMethod: String,
    val methodPaths: List<String>,
    val classPaths: List<String>,
)

object SpringMappingParser {

    private val methodMap = mapOf(
        "GetMapping" to "GET",
        "PostMapping" to "POST",
        "PutMapping" to "PUT",
        "DeleteMapping" to "DELETE",
        "PatchMapping" to "PATCH",
    )

    fun parse(method: PsiMethod): SpringMapping? {

        // ----- 方法级别 -----
        val ann = method.annotations.firstOrNull { a ->
            val short = a.qualifiedName?.substringAfterLast(".") ?: ""
            short in methodMap || short == "RequestMapping"
        } ?: return null

        val methodName = ann.qualifiedName?.substringAfterLast(".")!!
        val http = when (methodName) {
            "RequestMapping" -> getRequestMappingMethod(ann)
            else -> methodMap[methodName] ?: "GET"
        }

        val methodPaths = getPaths(ann)

        // ----- 类级别 -----
        val cls = method.containingClass
        val classAnn = cls?.annotations?.firstOrNull { it.qualifiedName?.endsWith("RequestMapping") == true }
        val classPaths = if (classAnn != null) getPaths(classAnn) else emptyList()

        return SpringMapping(
            httpMethod = http,
            methodPaths = methodPaths,
            classPaths = classPaths
        )
    }

    private fun getPaths(ann: PsiAnnotation): List<String> {
        val attr = ann.findDeclaredAttributeValue("value")
            ?: ann.findDeclaredAttributeValue("path")
            ?: return listOf("/")

        return attr.text
            .removePrefix("{").removeSuffix("}")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
    }

    private fun getRequestMappingMethod(ann: PsiAnnotation): String {
        val m = ann.findDeclaredAttributeValue("method")?.text ?: return "GET"
        return when {
            "POST" in m -> "POST"
            "PUT" in m -> "PUT"
            "DELETE" in m -> "DELETE"
            "PATCH" in m -> "PATCH"
            else -> "GET"
        }
    }
}
