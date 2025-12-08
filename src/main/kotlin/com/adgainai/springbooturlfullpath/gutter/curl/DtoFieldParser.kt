package com.adgainai.springbooturlfullpath.gutter.curl

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField

data class DtoField(
    val jsonKey: String,
    val type: String
)

object DtoFieldParser {

    /** 解析 DTO 的字段，生成 JSON key → type 列表 */
    fun parseDtoFields(psiClass: PsiClass): List<DtoField> {

        return psiClass.allFields.map { field ->

            val jsonKey = parseJsonKey(field)
            val type = field.type.presentableText

            DtoField(
                jsonKey = jsonKey,
                type = type
            )
        }
    }

    /**
     * 解析字段实际 JSON key：
     * 1. 若字段注解在 AnnotationBodyWrapperConfig 中，按注解映射
     * 2. 若注解有 value 属性（如 @JsonProperty("name")），按注解 value
     * 3. 否则按字段名
     */
    private fun parseJsonKey(field: PsiField): String {
        // ① 支持用户配置映射（注解 → 字段名）
        val wrapperMap = AnnotationBodyWrapperConfig.annotationToField

        for (ann in field.annotations) {
            val qn = ann.qualifiedName ?: continue
            val short = qn.substringAfterLast(".")

            // 用户自定义注解映射
            wrapperMap[short]?.let { return it }
            wrapperMap[qn]?.let { return it }

            // ② 兼容 @JsonProperty("xxx")
            if (qn == "com.fasterxml.jackson.annotation.JsonProperty"
                || short == "JsonProperty"
            ) {
                parseAnnotationStringValue(ann)?.let { return it }
            }
        }

        // ③ 默认字段名
        return field.name
    }

    /**
     * 从注解里取字符串参数：
     * - @JsonProperty("xxx")
     * - @A("x")
     */
    private fun parseAnnotationStringValue(ann: PsiAnnotation): String? {
        val attr = ann.findAttributeValue("value") ?: return null
        return attr.text.removeSurrounding("\"")
    }
}
