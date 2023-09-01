package com.github.dinbtechit.ngrx.action.notification


import com.github.dinbtechit.ngrx.NgrxIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class StarGithubRepoAction: DumbAwareAction("Star Repo", "", NgrxIcons.GitHub) {

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.open("https://github.com/dinbtechit/jetbrains-ngrx")
    }

}
