package com.adgainai.springbooturlfullpath.resizewindow


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowEx


open class ResizeWindowAction : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        TODO("Not yet implemented")
    }


    companion object {
        private const val STEP = 100 // 每次调整步长

        fun getActiveWindow(project: Project): String {
            val toolWindowManager = ToolWindowManager.getInstance(project)

            // 1. 获取激活的 ToolWindow
            val activeToolWindowId = toolWindowManager.activeToolWindowId
            if (activeToolWindowId != null) {
                return activeToolWindowId
            }

            // 2. 判断是否是 Editor
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor != null) {
                return "Editor"
            }

            return ""
        }

        fun resizeWindow(project: Project, direction: ToolWindowAnchor) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val activeId = getActiveWindow(project)

            if (activeId == "Editor" || activeId.isEmpty()) {


                // 暂不处理 Editor
                val openToolWindows = toolWindowManager.toolWindowIds.mapNotNull { id ->
                    toolWindowManager.getToolWindow(id)?.takeIf { it.isVisible }
                }

                /// 构造 Map<Anchor, ToolWindow>，每个方向只取第一个
                val toolWindowByAnchor: Map<ToolWindowAnchor, ToolWindow> = openToolWindows
                    .groupBy { it.anchor }           // 按方向分组
                    .mapValues { it.value.first() }  // 每组取第一个 ToolWindow


                val dwindow = toolWindowByAnchor.get(direction)
                if (dwindow != null) {
                    when (direction) {
                        ToolWindowAnchor.LEFT -> (dwindow as ToolWindowEx).stretchWidth(-STEP)
                        ToolWindowAnchor.RIGHT -> (dwindow as ToolWindowEx).stretchWidth(-STEP)
//                        ToolWindowAnchor.TOP -> (dwindow as ToolWindowEx).stretchHeight(-STEP)
                        ToolWindowAnchor.BOTTOM -> (dwindow as ToolWindowEx).stretchHeight(-STEP)
                    }
                } else {
                    val oppositeDirection = direction.opposite()
                    val dwindow = toolWindowByAnchor.get(oppositeDirection)
                    dwindow?.let { it ->
                        when (direction) {
                            ToolWindowAnchor.LEFT -> (it as ToolWindowEx).stretchWidth(+STEP)
                            ToolWindowAnchor.RIGHT -> (it as ToolWindowEx).stretchWidth(+STEP)
                            ToolWindowAnchor.TOP -> (it as ToolWindowEx).stretchHeight(+STEP)
                            ToolWindowAnchor.BOTTOM -> (it as ToolWindowEx).stretchHeight(+STEP)
                        }
                    }


                }

                return
            }

            val window = toolWindowManager.getToolWindow(activeId) ?: return
            val anchor = window.anchor

            when (anchor) {
                ToolWindowAnchor.LEFT -> {
                    when (direction) {
                        ToolWindowAnchor.LEFT -> (window as ToolWindowEx).stretchWidth(-STEP)
                        ToolWindowAnchor.RIGHT -> (window as ToolWindowEx).stretchWidth(STEP)
                    }
                }

                ToolWindowAnchor.RIGHT -> {
                    when (direction) {
                        ToolWindowAnchor.LEFT -> (window as ToolWindowEx).stretchWidth(+STEP)
                        ToolWindowAnchor.RIGHT -> (window as ToolWindowEx).stretchWidth(-STEP)
                    }
                }

                ToolWindowAnchor.BOTTOM -> {
                    when (direction) {
                        ToolWindowAnchor.TOP -> (window as ToolWindowEx).stretchHeight(+STEP)
                        ToolWindowAnchor.BOTTOM -> (window as ToolWindowEx).stretchHeight(-STEP)
                    }
                }

                ToolWindowAnchor.TOP -> {
                    when (direction) {
                        ToolWindowAnchor.TOP -> (window as ToolWindowEx).stretchHeight(-STEP)
                        ToolWindowAnchor.BOTTOM -> (window as ToolWindowEx).stretchHeight(+STEP)
                    }
                }
            }
        }
    }


    // 左右上下子动作
    class Left : ResizeWindowAction() {
        override fun actionPerformed(event: AnActionEvent) {
            event.project?.let { resizeWindow(it, ToolWindowAnchor.LEFT) }
        }
    }

    class Right : ResizeWindowAction() {
        override fun actionPerformed(event: AnActionEvent) {
            event.project?.let { resizeWindow(it, ToolWindowAnchor.RIGHT) }
        }
    }

    class Up : ResizeWindowAction() {


        override fun actionPerformed(event: AnActionEvent) {
            event.project?.let { resizeWindow(it, ToolWindowAnchor.TOP) }
        }
    }

    class Down : ResizeWindowAction() {
        override fun actionPerformed(event: AnActionEvent) {
            event.project?.let { resizeWindow(it, ToolWindowAnchor.BOTTOM) }
        }
    }
}

private fun ToolWindowAnchor.opposite(): ToolWindowAnchor {
    when (this) {
        ToolWindowAnchor.LEFT -> return ToolWindowAnchor.RIGHT
        ToolWindowAnchor.RIGHT -> return ToolWindowAnchor.LEFT
        ToolWindowAnchor.TOP -> return ToolWindowAnchor.BOTTOM
        ToolWindowAnchor.BOTTOM -> return ToolWindowAnchor.TOP
        else -> return this // 理论上不会走到这里
    }
}
