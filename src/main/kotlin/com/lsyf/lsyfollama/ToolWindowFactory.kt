package com.lsyf.lsyfollama

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.dsl.builder.panel
import com.sun.java.accessibility.util.AWTEventMonitor.addActionListener
import com.intellij.openapi.ui.Messages
import com.intellij.ui.content.ContentFactory


class ToolWindowFactory : ToolWindowFactory {
    override
    fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = panel {
            row {
                label("User Input:")
                textField().applyToComponent {
                    toolTipText = "Enter text here"
                }

            }
            row {
                button("Submit") {
                    addActionListener {
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showInfoMessage("这是新的开始！", "Hello");
                        }



//                                Thread { // 后台线程
//                                    val data = fetchData() // 耗时操作
//                                    ApplicationManager.getApplication().invokeLater {
//                                        updateUI(data) // 切回 EDT 更新
//                                    }
//                                }.start()



                    }
                }
            }
        }
        toolWindow.contentManager.addContent(
            ContentFactory.getInstance().createContent(panel, "", false))



    }
}