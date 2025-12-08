package com.adgainai.springbooturlfullpath.gutter.curl

import com.adgainai.springbooturlfullpath.settings.CurlAnnotationMappingSettings
import com.intellij.psi.PsiMethod

/**
 * 单个参数信息
 */
data class CurlParam(
    val name: String,            // 参数名：例如 "obj"
    val typeText: String,        // 参数类型：例如 "Obj"
    val isRequestBody: Boolean,  // 是否是 RequestBody（或 wrapper 注解）
    val wrapperKey: String?      // 若为 wrapper 注解，则对应 JSON 字段名
)

/**
 * 方法全部参数
 */
data class CurlParams(
    val params: List<CurlParam>
)

/**
 * 参数解析器：解析 RequestBody 和注解包装关系
 */
object ParamParser {

    fun parse(method: PsiMethod): CurlParams {
        val settings = CurlAnnotationMappingSettings.instance   // 应用级别配置
        val wrapperMap = settings.mappings                      // annotation → wrapperKey

        val params = method.parameterList.parameters.map { p ->

            val paramName = p.name ?: "param"
            val type = p.type.canonicalText

            // 是否是 @RequestBody
            val isRequestBody = p.annotations.any {
                it.qualifiedName?.endsWith("RequestBody") == true
            }

            // 查找 wrapperKey（来自配置）
            val wrapperKey = p.annotations.mapNotNull { ann ->
                val qn = ann.qualifiedName ?: return@mapNotNull null
                val short = qn.substringAfterLast(".")

                // 配置支持短名 or 完整名
                wrapperMap[short] ?: wrapperMap[qn]
            }.firstOrNull()

            CurlParam(
                name = paramName,
                typeText = type,
                isRequestBody = isRequestBody || wrapperKey != null,   // wrapper 也视为 body
                wrapperKey = wrapperKey
            )
        }

        return CurlParams(params)
    }
}
