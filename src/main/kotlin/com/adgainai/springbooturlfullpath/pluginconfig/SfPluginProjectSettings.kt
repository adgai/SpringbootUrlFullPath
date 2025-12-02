package com.adgainai.springbooturlfullpath.pluginconfig

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "im.codeman.SfgConfiguration ",
    storages = [Storage("CodeManSfgConfiguration.xml")]
)
@Service(Service.Level.APP)
class SfPluginProjectSettings : PersistentStateComponent<SfPluginProjectSettings> {
    var gutterHighlightCurrentBlock: Boolean = false

    companion object {
        val instance: SfPluginProjectSettings
            get() = service()
    }


    override fun getState(): SfPluginProjectSettings? {
        return this
    }

    override fun loadState(state: SfPluginProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
