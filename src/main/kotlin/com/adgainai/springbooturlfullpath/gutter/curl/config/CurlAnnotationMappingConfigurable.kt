package com.adgainai.springbooturlfullpath.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

class CurlAnnotationMappingConfigurable : Configurable {

    private var panel: JPanel? = null
    private var tableModel: DefaultTableModel? = null
    private var table: JBTable? = null

    override fun getDisplayName(): String = "Curl Annotation Mapping"

    override fun createComponent(): JComponent {
        if (panel != null) return panel!!

        panel = JPanel(BorderLayout())

        tableModel = DefaultTableModel(arrayOf("Annotation", "Field Name"), 0)

        table = JBTable(tableModel).apply {
            setShowGrid(true)
            emptyText.text = "点击 + 添加注解映射"
        }

        // ★ 自定义 Add / Remove 操作
        val decorator = ToolbarDecorator.createDecorator(table!!)
            .setAddAction {
                // 添加空行
                tableModel!!.addRow(arrayOf("", ""))
            }
            .setRemoveAction {
                val row = table!!.selectedRow
                if (row >= 0) {
                    tableModel!!.removeRow(row)
                }
            }
            .setEditAction(null) // 不需要 Edit 按钮
            .disableUpDownActions() // 禁用上下移动

        val tablePanel = decorator.createPanel()

        panel!!.add(tablePanel, BorderLayout.CENTER)
        reset()

        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = CurlAnnotationMappingSettings.instance

        // table → map
        val uiData = mutableMapOf<String, String>()
        for (i in 0 until tableModel!!.rowCount) {
            val ann = tableModel!!.getValueAt(i, 0)?.toString()?.trim()
            val field = tableModel!!.getValueAt(i, 1)?.toString()?.trim()
            if (!ann.isNullOrEmpty() && !field.isNullOrEmpty()) {
                uiData[ann] = field
            }
        }

        return uiData != settings.mappings
    }

    override fun apply() {
        val settings = CurlAnnotationMappingSettings.instance

        val newMap = mutableMapOf<String, String>()
        for (i in 0 until tableModel!!.rowCount) {
            val ann = tableModel!!.getValueAt(i, 0)?.toString()?.trim()
            val field = tableModel!!.getValueAt(i, 1)?.toString()?.trim()
            if (!ann.isNullOrEmpty() && !field.isNullOrEmpty()) {
                newMap[ann] = field
            }
        }

        settings.mappings = newMap
    }

    override fun reset() {
        val settings = CurlAnnotationMappingSettings.instance

        tableModel!!.rowCount = 0
        settings.mappings.forEach { (ann, field) ->
            tableModel!!.addRow(arrayOf(ann, field))
        }
    }
}
