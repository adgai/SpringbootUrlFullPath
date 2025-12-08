package com.adgainai.springbooturlfullpath.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "CurlAnnotationMappingSettings",
    storages = [Storage("curl-annotation-mapping.xml")]
)
@Service(Service.Level.APP)  // ★ 应用级别（不是 Project）
class CurlAnnotationMappingSettings : PersistentStateComponent<CurlAnnotationMappingSettings> {

    var mappings: MutableMap<String, String> = mutableMapOf(
        "XXX" to "_body",
        "Biz" to "bizData"
    )

    companion object {
        val instance: CurlAnnotationMappingSettings
            get() = service()
    }

    override fun getState(): CurlAnnotationMappingSettings = this

    override fun loadState(state: CurlAnnotationMappingSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
