package com.adgainai.springbooturlfullpath.gutter.curl

import com.adgainai.springbooturlfullpath.settings.CurlAnnotationMappingSettings

object AnnotationBodyWrapperConfig {

    /** 始终从 ApplicationSettings 读取 */
    val annotationToField: Map<String, String>
        get() = CurlAnnotationMappingSettings.instance.mappings
}
