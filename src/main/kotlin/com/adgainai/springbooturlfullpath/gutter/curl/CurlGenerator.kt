package com.adgainai.springbooturlfullpath.gutter.curl

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope

object CurlGenerator {

    fun generate(
        project: Project,
        baseUrl: String?,
        mapping: SpringMapping,
        params: CurlParams
    ): String {

        val url = buildUrl(baseUrl, mapping, params)
        val method = mapping.httpMethod.uppercase()

        val needBody = method in listOf("POST", "PUT", "PATCH")

        val body = if (needBody) buildRequestBody(project, params) else ""

        return if (needBody) {
            """
        curl -X $method "$url" \
          -H "Content-Type: application/json" \
          -d '$body'
        """.trimIndent()
        } else {
            """curl -X $method "$url""""
        }
    }

    /**
     * 组合 URL（支持 pathVariable 和 requestParam）
     */
    private fun buildUrl(
        baseUrl: String?,
        mapping: SpringMapping,
        params: CurlParams
    ): String {

        val base = baseUrl ?: "http://localhost:8080"

        val classPrefix = mapping.classPaths.firstOrNull() ?: ""
        val methodPath = mapping.methodPaths.firstOrNull() ?: ""

        var url = base + classPrefix + methodPath

        // pathVariable
        params.params.filter { it.isRequestBody.not() }.forEach { p ->
            url = url.replace("{${p.name}}", "1")
        }

        // requestParam
        val queryParams = params.params.filter {
            !it.isRequestBody && !url.contains("{${it.name}}")
        }
        if (queryParams.isNotEmpty()) {
            val q = queryParams.joinToString("&") { "${it.name}=test" }
            url += "?$q"
        }

        return url
    }

    /**
     * 构建 request body JSON
     */
    /**
     * 构建 request body JSON：逐个处理所有 isRequestBody 参数
     */
    private fun buildRequestBody(project: Project, params: CurlParams): String {

        val bodyFields = mutableListOf<String>()

        params.params.forEach { p ->

            if (!p.isRequestBody) return@forEach

            // 提取类名
            val className = (p.typeText)
            val psiClass = findPsiClass(project, className) ?: return@forEach

            // 构建该对象的 JSON
            val innerJson = buildJsonFromDto(psiClass)

            // 有 wrapperKey，用 wrapperKey 作为字段名
            val key = p.wrapperKey ?: p.name  // 无 wrapperKey 则用参数名

            val field = """
        "$key": $innerJson
        """.trimIndent()

            bodyFields += field
        }

        // 可能一个都没有（防御）
        if (bodyFields.isEmpty()) return "{}"

        // 多个字段统一封装到 JSON root
        return "{\n${bodyFields.joinToString(",\n")}\n}"
    }


    /**
     * 将 DTO PSI 转成 JSON 字段（你已有 DtoFieldParser）
     */
    private fun buildJsonFromDto(dtoClass: PsiClass): String {
        val fields = DtoFieldParser.parseDtoFields(dtoClass)

        val kv = fields.joinToString(",\n") { f ->
            "  \"${f.jsonKey}\": ${defaultValue(f.type)}"
        }

        return "{\n$kv\n}"
    }

    private fun defaultValue(type: String): String = when (type) {
        "String" -> "\"\""
        "Integer", "Long", "int", "long" -> "0"
        "Boolean", "boolean" -> "false"
        else -> "null"
    }

    private fun findPsiClass(project: Project, typeName: String): PsiClass? {
        val facade = JavaPsiFacade.getInstance(project)
        return facade.findClass(typeName, GlobalSearchScope.allScope(project))
    }
}
