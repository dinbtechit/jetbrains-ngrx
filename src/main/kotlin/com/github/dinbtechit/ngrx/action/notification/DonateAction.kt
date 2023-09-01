package com.github.dinbtechit.ngrx.action.notification

import com.github.dinbtechit.ngrx.NgrxIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class DonateAction: DumbAwareAction("Donate ($2)", "", NgrxIcons.Donate) {

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open("https://www.buymeacoffee.com/dinbtechit")
    }

}
