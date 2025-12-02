package com.adgainai.springbooturlfullpath.gutter.curl


object CurlGenerator {

    fun generate(
        baseUrl: String,
        mapping: SpringMapping,
        params: CurlParams
    ): String {

        val url = buildUrl(baseUrl, mapping, params)

        val http = mapping.httpMethod

        val bodyPart = if (params.bodyJson != null) {
            """
            -H "Content-Type: application/json" \
            -d '${params.bodyJson}'
            """.trimIndent()
        } else ""

        val headerPart = buildHeaders(params.headers)

        return """
curl -X $http "$url" \
$headerPart \
$bodyPart
""".trimIndent()
    }

    private fun buildUrl(
        baseUrl: String,
        mapping: SpringMapping,
        params: CurlParams
    ): String {

        val classPath = mapping.classPaths.firstOrNull() ?: ""
        val methodPath = mapping.methodPaths.firstOrNull() ?: ""

        var url = baseUrl + classPath + methodPath

        params.pathParams.forEach { (k, v) ->
            url = url.replace("{$k}", v)
        }

        if (params.queryParams.isNotEmpty()) {
            url += "?" + params.queryParams.entries.joinToString("&") {
                "${it.key}=${it.value}"
            }
        }

        return url
    }

    private fun buildHeaders(headers: Map<String, String>): String {
        if (headers.isEmpty()) return ""
        return headers.entries.joinToString(" \\\n") {
            """  -H "${it.key}: ${it.value}""""
        }
    }
}
