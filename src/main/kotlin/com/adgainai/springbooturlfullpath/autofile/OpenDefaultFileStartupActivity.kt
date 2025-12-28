package com.adgainai.springbooturlfullpath.autofile

/**
 * @author: codeman
 * @date: 2025/12/28 22:07
 **/

import com.adgainai.springbooturlfullpath.pluginconfig.SfPluginProjectSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.SingleAlarm

class AutoOpenDefaultFileActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val autoFile = SfPluginProjectSettings.instance.autoFile
        if (!autoFile) return

        val opener = DefaultFileOpener(project)

        // ✅ 1) 启动后兜底：延迟检查，多次重试直到编辑器稳定
        opener.scheduleCheck()

        // ✅ 2) 编辑器变化时再兜底（打开/关闭/选中变化都会触发）
        val connection = project.messageBus.connect(project)
        connection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) {
                    opener.scheduleCheck()
                }

                override fun fileClosed(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) {
                    opener.scheduleCheck()
                }

            }
        )
    }
}

private class DefaultFileOpener(private val project: Project) {

    // 通过 SingleAlarm 把“检查打开”合并/去抖，避免连环触发
    private val alarm = SingleAlarm(
        Runnable { checkAndOpenIfEmpty() },
        /* delay ms */ 600,
        project
    )

    // 启动阶段 IDE 可能会多轮恢复/重建编辑器，这里做有限次数重试更稳
    private var remainingBootRetries = 10

    fun scheduleCheck() {
        alarm.cancelAndRequest()
    }

    private fun checkAndOpenIfEmpty() {
        if (project.isDisposed) return

        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater

            val settings = SfPluginProjectSettings.instance

            // 🚨 核心：用户没勾选，直接退出
            if (!settings.autoFile) {
                return@invokeLater
            }

            val manager = FileEditorManager.getInstance(project)

            if (manager.openFiles.isEmpty()) {
                openOrCreateRootFile(manager, "README.md")
                remainingBootRetries = 0
                return@invokeLater
            }

            if (remainingBootRetries > 0) {
                remainingBootRetries--
                alarm.request()
            }
        }
    }

}
