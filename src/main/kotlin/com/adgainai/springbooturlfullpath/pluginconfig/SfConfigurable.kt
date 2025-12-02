package com.adgainai.springbooturlfullpath.pluginconfig

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class SfConfigurable() : Configurable {
    private val settings = SfPluginProjectSettings.instance
    private var mainPanel: JPanel? = null
    private var hls: JCheckBox? = null

    override fun getDisplayName(): String = "My Plugin Configuration"

    override fun createComponent(): JComponent {
        hls = JCheckBox("是否在gutter 高亮当前的block").apply {
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0) // 左边缩进20px
        }


        // 用 FormBuilder 构建主界面
        val formBuilder = FormBuilder.createFormBuilder()

        // 折叠设置
        formBuilder.addComponent(TitledSeparator("gutter highlight current block", mainPanel))
            .addComponent(hls!!)


        // 给整体面板加 padding
        mainPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(0, 40, 0, 0) // 整体左缩进40px
            add(formBuilder.panel, BorderLayout.CENTER)
        }

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        return hls!!.isSelected != settings.gutterHighlightCurrentBlock
    }

    override fun apply() {
        settings.gutterHighlightCurrentBlock = hls!!.isSelected

    }

    override fun reset() {
        hls!!.isSelected = settings.gutterHighlightCurrentBlock

    }

    override fun getHelpTopic(): String? = null
}
