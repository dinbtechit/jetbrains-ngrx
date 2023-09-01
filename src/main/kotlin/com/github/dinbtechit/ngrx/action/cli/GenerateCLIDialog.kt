package com.github.dinbtechit.ngrx.action.cli

import com.github.dinbtechit.ngrx.NgrxBundle
import com.github.dinbtechit.ngrx.action.cli.store.Action
import com.github.dinbtechit.ngrx.action.cli.util.NgxsGeneratorFileUtil
import com.github.dinbtechit.ngrx.common.ui.TextIconField
import com.github.dinbtechit.ngrx.action.cli.store.CLIState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent


class GenerateCLIDialog(private val project: Project, e: AnActionEvent) : DialogWrapper(project, true) {
    private var autoCompleteField = TextFieldWithAutoCompletion(
        project,
        CLIOptionsCompletionProvider(CLIOptionsCompletionProvider.options.keys.toList()), false,
        null
    ).apply {
        setPlaceholder("--options")
    }

    private val ngxsStoreService = project.service<CLIState>()
    private val store = ngxsStoreService.store

    private val virtualFile: VirtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
    private val directory = when {
        virtualFile.isDirectory -> virtualFile // If it's directory, use it
        else -> virtualFile.parent // Otherwise, get its parent directory
    }

    private val pathField = TextIconField(AllIcons.Actions.GeneratedFolder)

    private val state = ngxsStoreService.store.getState()

    init {
        title = "NgRx CLI/Schematics Generate"
        autoCompleteField.text = state.parameter
        autoCompleteField.isEnabled = state.module != null
        pathField.apply {
            val relativePath = NgxsGeneratorFileUtil.getRelativePath(project, directory)
            text = when (relativePath) {
                "" -> project.guessProjectDir()?.path
                else -> relativePath
            }
            isEnabled = true
            isEditable = false
        }

        autoCompleteField.document.addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                ApplicationManager.getApplication().invokeLater {


                }
            }
        })
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            group(NgrxBundle.message("generateInPath")) {
                row {
                    cell(pathField).align(Align.FILL)
                }
            }
            separator()
            row(NgrxBundle.message("parameters")) {}.topGap(TopGap.SMALL)
            row {
                cell(autoCompleteField).focused().align(
                    Align.FILL
                ).apply {
                    comment("(name --options)")
                }
            }
            window.minimumSize = Dimension(500, super.getPreferredSize().height)
        }
    }

    override fun doValidate(): ValidationInfo? {
        val parameters = autoCompleteField.text
        var invalidFileName = false
        if (parameters.isNotBlank()) {
            invalidFileName = true
        }
        return if (parameters.isBlank() || autoCompleteField.text.isBlank()) {
            ValidationInfo(NgrxBundle.message("parameterBlankErrorMessage"), autoCompleteField)
        } else null
    }


    override fun doOKAction() {
        store.dispatch(
            Action.GenerateCLIAction(
                options = autoCompleteField.text,
                filePath = directory.path,
                project = project,
                workingDir = directory,
                module = state.module!!
            )
        )
        super.doOKAction()
    }

}
