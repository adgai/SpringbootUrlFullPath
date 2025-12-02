package com.adgainai.springbooturlfullpath.gutter.curl

import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.asJava.toLightElements

data class CurlParams(
    val pathParams: Map<String, String>,
    val queryParams: Map<String, String>,
    val headers: Map<String, String>,
    val bodyJson: String?
)

object ParamParser {

    fun parse(method: PsiMethod): CurlParams {

        val pathParams = mutableMapOf<String, String>()
        val queryParams = mutableMapOf<String, String>()
        val headers = mutableMapOf<String, String>()
        var bodyJson: String? = null

        for (p in method.parameterList.parameters) {

            val anns = p.annotations
            val annNames = anns.map { it.qualifiedName?.substringAfterLast(".") }

            when {

                // ---------------------- @PathVariable ----------------------
                "PathVariable" in annNames -> {
                    val name = p.name ?: continue
                    pathParams[name] = "{${name}}"
                }

                // ---------------------- @RequestParam ----------------------
                "RequestParam" in annNames -> {
                    val name = p.name ?: continue
                    queryParams[name] = "<${name}>"
                }

                // ---------------------- @RequestHeader（增强版） ----------------------
                "RequestHeader" in annNames -> {
                    val ann = anns.first { it.qualifiedName!!.endsWith("RequestHeader") }

                    val name = extractHeaderName(ann, p)
                    val value = extractHeaderDefaultValue(ann, name)

                    headers[name] = value
                }

                // ---------------------- @RequestBody ----------------------
                "RequestBody" in annNames -> {
                    bodyJson = buildJsonForModel(p.type)
                }
            }
        }

        return CurlParams(
            pathParams = pathParams,
            queryParams = queryParams,
            headers = headers,
            bodyJson = bodyJson
        )
    }

    // =================================================================
    //            RequestHeader(name="", defaultValue="")
    // =================================================================

    private fun extractHeaderName(ann: PsiAnnotation, p: PsiParameter): String {
        val raw = ann.findDeclaredAttributeValue("value")
            ?: ann.findDeclaredAttributeValue("name")
            ?: return p.name!!

        return raw.text.removeSurrounding("\"")
    }

    private fun extractHeaderDefaultValue(ann: PsiAnnotation, name: String): String {
        val defVal = ann.findDeclaredAttributeValue("defaultValue")?.text
        if (defVal != null && defVal != "\"\"") {
            return defVal.removeSurrounding("\"")
        }

        return "<$name>"
    }

    // =================================================================
    //                   JSON Body 构造器（增强版）
    //     支持：Lombok / Kotlin data class / Jackson / 嵌套对象
    // =================================================================

    fun buildJsonForModel(type: PsiType): String = buildJson(type, mutableSetOf())

    private val terminalTypes = setOf(
        "java.lang.String",
        "java.lang.Integer", "int",
        "java.lang.Long", "long",
        "java.lang.Double", "double",
        "java.lang.Float", "float",
        "java.lang.Short", "short",
        "java.lang.Byte", "byte",
        "java.lang.Boolean", "boolean",
        "java.math.BigDecimal",
        "java.math.BigInteger",
        "java.util.UUID",
        "java.util.Date",
        "java.sql.Timestamp",
        "java.time.LocalDate",
        "java.time.LocalDateTime",
        "java.time.LocalTime",
        "java.time.Instant"
    )

    private fun buildJson(type: PsiType, visited: MutableSet<String>): String {

        val canonical = type.canonicalText

        // -------------------------------------
        // 1) 终类型：直接返回默认文本
        // -------------------------------------
        if (canonical in terminalTypes) {
            return defaultValue(type)
        }

        // -------------------------------------
        // 2) 防止循环依赖
        // -------------------------------------
        if (canonical in visited) return "{}"
        visited.add(canonical)

        // -------------------------------------
        // 3) 数组 / List / Set
        // -------------------------------------

        if (type is PsiArrayType || isCollection(type)) {

            val cls: PsiClass? = when (type) {

                is PsiArrayType -> {
                    // 数组：String[] → String → PsiClass
                    val component = type.componentType
                    (component as? PsiClassType)?.resolve()
                }

                is PsiClassType -> {
                    // 泛型 List<T> → T → PsiClass
                    val generic = type.parameters.firstOrNull()
                    (generic as? PsiClassType)?.resolve()
                }

                else -> null
            }

            // 如果 T 不是类类型（例如 List<Int>），直接生成 []
            if (cls == null) {
                return "[ ]"
            }

            // 深层类型（泛型 T、数组元素类型）
            val deepType: PsiType = when (type) {
                is PsiArrayType -> type.componentType
                is PsiClassType -> type.parameters.firstOrNull() ?: type
                else -> type
            }

            val sample = buildJson(deepType, visited).prependIndent("  ")
            return "[\n$sample\n]"
        }

        // -------------------------------------
        // 4) 真实对象 DTO
        // -------------------------------------
        if (type is PsiClassType) {

            val cls = type.resolve() ?: return "{}"
            val fields = getAllFieldsSafe(cls)

            val sb = StringBuilder()
            sb.append("{\n")

            fields.forEachIndexed { idx, field ->

                val fieldName = extractJsonFieldName(field)
                val fieldType = field.type

                val jsonValue = buildJson(fieldType, visited).prependIndent("  ")

                sb.append("  \"$fieldName\": $jsonValue")
                if (idx != fields.lastIndex) sb.append(",")
                sb.append("\n")
            }

            sb.append("}")
            return sb.toString()
        }

        return "\"\""
    }

    // =================================================================
    //                         Jackson 支持
    // =================================================================
    private fun extractJsonFieldName(field: PsiField): String {
        val ann = field.annotations.firstOrNull {
            it.qualifiedName?.endsWith("JsonProperty") == true
        }
        if (ann != null) {
            val v = ann.findDeclaredAttributeValue("value")
            if (v != null && v.text != "\"\"") {
                return v.text.removeSurrounding("\"")
            }
        }
        return field.name
    }

    private fun isJacksonIgnored(field: PsiField): Boolean {
        return field.annotations.any {
            it.qualifiedName?.endsWith("JsonIgnore") == true
        }
    }

    // =================================================================
    //                         工具函数
    // =================================================================

    private fun isCollection(t: PsiType): Boolean =
        t.canonicalText.startsWith("java.util.List") ||
                t.canonicalText.startsWith("java.util.Set") ||
                t.canonicalText.startsWith("kotlin.collections.List") ||
                t.canonicalText.startsWith("kotlin.collections.Set")

    private fun extractDeepType(t: PsiType, context: PsiClass): PsiType {
        return when (t) {
            is PsiArrayType -> t.componentType

            is PsiClassType -> {
                // List<T> or Set<T>
                val param = t.parameters.firstOrNull()
                if (param != null) {
                    param
                } else {
                    // fallback to Object
                    PsiType.getJavaLangObject(context.manager, context.resolveScope)
                }
            }

            else -> t
        }
    }


    private fun defaultValue(type: PsiType): String {
        return when (type.canonicalText) {
            "java.lang.String" -> "\"\""
            "java.lang.Boolean", "boolean" -> "false"
            "java.lang.Integer", "int",
            "java.lang.Long", "long",
            "java.lang.Double", "double",
            "java.lang.Float", "float",
            "java.lang.Short", "short",
            "java.lang.Byte", "byte",
            "java.math.BigDecimal",
            "java.math.BigInteger" -> "0"

            "java.time.LocalDate" -> "\"2025-01-01\""
            "java.time.LocalDateTime" -> "\"2025-01-01T00:00:00\""
            "java.time.LocalTime" -> "\"00:00:00\""
            "java.time.Instant" -> "\"2025-01-01T00:00:00Z\""

            else -> "\"\""
        }
    }
    private fun getAllFieldsSafe(cls: PsiClass): List<PsiField> {

        val origin = cls.navigationElement

        // ---------------------------
        // Kotlin class (K2)
        // ---------------------------
        if (origin is org.jetbrains.kotlin.psi.KtClassOrObject) {

            val results = mutableListOf<PsiField>()

            // 1) 主构造参数（带 val/var 才是字段）
            origin.primaryConstructorParameters.forEach { param ->
                if (param.hasValOrVar()) {
                    val name = param.name ?: return@forEach
                    cls.findFieldByName(name, false)?.let { results += it }
                }
            }

            // 2) 类体中定义的 val/var 属性
            origin.getBody()?.properties?.forEach { prop ->
                val name = prop.name ?: return@forEach
                cls.findFieldByName(name, false)?.let { results += it }
            }

            return results
        }

        // ---------------------------
        // Java class
        // ---------------------------
        return cls.allFields.toList()
    }


}
