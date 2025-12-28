package com.adgainai.springbooturlfullpath.pluginconfig

import com.intellij.openapi.options.Configurable
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class SfConfigurable : Configurable {

    private val settings = SfPluginProjectSettings.instance

    private lateinit var hls: JCheckBox
    private lateinit var autoFile: JCheckBox
    private lateinit var mainPanel: JPanel

    override fun getDisplayName(): String = "My Plugin Configuration"

    override fun createComponent(): JComponent {
        hls = JCheckBox("是否在 gutter 高亮当前 block")
        autoFile = JCheckBox("是否在没有文件打开时自动打开 README.md")

        val form = FormBuilder.createFormBuilder()
            .addComponent(TitledSeparator("Gutter"))
            .addComponent(hls)
            .addComponent(TitledSeparator("Auto Open File"))
            .addComponent(autoFile)
            .panel

        mainPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(0, 40, 0, 0)
            add(form, BorderLayout.CENTER)
        }

        reset()
        return mainPanel
    }

    override fun isModified(): Boolean =
        hls.isSelected != settings.gutterHighlightCurrentBlock ||
                autoFile.isSelected != settings.autoFile

    override fun apply() {
        settings.gutterHighlightCurrentBlock = hls.isSelected
        settings.autoFile = autoFile.isSelected
    }

    override fun reset() {
        hls.isSelected = settings.gutterHighlightCurrentBlock
        autoFile.isSelected = settings.autoFile
    }
}
