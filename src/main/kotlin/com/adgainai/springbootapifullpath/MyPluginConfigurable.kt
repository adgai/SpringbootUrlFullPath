package com.example.myinlayhints

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class MyPluginConfigurable(private val project: Project) : Configurable {
    private val settings = MyPluginProjectSettings.getInstance(project)
    private val tokenField = JBTextField(settings.token).apply {
        emptyText.text = "请输入 apollo 的token"
    }
    private val urlField = JBTextField(settings.url).apply {
        emptyText.text = "请输入 Apollo 的URL"
    }
    private val serviceNameField = JBTextField(settings.serviceName).apply {
        emptyText.text = "请输入服务名称"
    }
    private val methodSignatureField = JBTextField(settings.methodSignature).apply {
        emptyText.text = "请输入方法签名多个逗号拼接，默认为：configUtils.getBool,configUtils.getBoolean"
    }

    override fun getDisplayName(): String = "My Plugin Configuration"

    override fun createComponent(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Token: "), tokenField, 4, false)
            .addLabeledComponent(JBLabel("MethodSignatureField: "), methodSignatureField, 4, false)
            .addLabeledComponent(JBLabel("URL: "), urlField, 4, false)
            .addLabeledComponent(JBLabel("ServiceName: "), serviceNameField, 4, false)
            .panel
        return panel
    }

    override fun isModified(): Boolean {
        return tokenField.text != settings.token || urlField.text != settings.url || serviceNameField.text != settings.serviceName || methodSignatureField.text !=settings.methodSignature
    }

    override fun apply() {
        settings.token = tokenField.text
        settings.url = urlField.text
        settings.serviceName = serviceNameField.text
        settings.methodSignature = methodSignatureField.text
    }

    override fun reset() {
        tokenField.text = settings.token
        urlField.text = settings.url
        serviceNameField.text = settings.serviceName
        methodSignatureField.text = settings.methodSignature
    }

    override fun getHelpTopic(): String? = null
}
