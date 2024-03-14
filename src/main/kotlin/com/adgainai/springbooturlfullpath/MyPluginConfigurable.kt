package com.adgainai.springbooturlfullpath

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class MyPluginConfigurable(private val project: Project) : Configurable {
    private val settings = MyPluginProjectSettings.getInstance(project)
    private val prefixField = JBTextField(settings.prefix).apply {
        emptyText.text = "请输入 prefix, 如果多个用英文逗号拼接。方法上将会展示原始url，拼接前缀的url"
    }



    override fun getDisplayName(): String = "My Plugin Configuration"

    override fun createComponent(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("prefix: "), prefixField, 4, false)
                     .panel
        return panel
    }

    override fun isModified(): Boolean {
        return prefixField.text != settings.prefix
    }

    override fun apply() {
        settings.prefix = prefixField.text

    }

    override fun reset() {
        prefixField.text = settings.prefix

    }

    override fun getHelpTopic(): String? = null
}
