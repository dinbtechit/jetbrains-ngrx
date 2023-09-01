package com.github.dinbtechit.ngrx.action.notification

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class WhatsNewAction: DumbAwareAction("What's New?") {

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open("https://github.com/dinbtechit/jetbrains-ngrx/blob/main/CHANGELOG.md")
    }

}
