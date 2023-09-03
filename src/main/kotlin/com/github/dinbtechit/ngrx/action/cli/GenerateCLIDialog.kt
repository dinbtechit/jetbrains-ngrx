package com.github.dinbtechit.ngrx.action.cli

import com.github.dinbtechit.ngrx.NgrxBundle
import com.github.dinbtechit.ngrx.action.cli.services.NgrxCliService
import com.github.dinbtechit.ngrx.action.cli.store.Action
import com.github.dinbtechit.ngrx.action.cli.store.CLIState
import com.github.dinbtechit.ngrx.action.cli.util.NgRxGeneratorFileUtil
import com.github.dinbtechit.ngrx.common.ui.TextIconField
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent


class GenerateCLIDialog(private val project: Project, e: AnActionEvent) : DialogWrapper(project, true) {


    private val ngxsStoreService = project.service<CLIState>()
    private val store = ngxsStoreService.store

    private val virtualFile: VirtualFile = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
    private val directory = when {
        virtualFile.isDirectory -> virtualFile // If it's directory, use it
        else -> virtualFile.parent // Otherwise, get its parent directory
    }
    private val ngrxCliService = project.service<NgrxCliService>()
    private val optionTypes = ngrxCliService.getTypeOptions()
    private val comboBoxModel = DefaultComboBoxModel(
        optionTypes.keys.toTypedArray()
    )
    private val schematicTypeComboBox = ComboBox(comboBoxModel).apply {
        setRenderer(GenerateTypeComboRenderer(project))
    }

    private var autoCompleteField = TextFieldWithAutoCompletion(
        project, CLIOptionsCompletionProvider(
            project, listOf()
        ), false, null
    ).apply {
        setPlaceholder("name --options")
    }

    private val pathField = TextIconField(AllIcons.Actions.GeneratedFolder)

    private val state = ngxsStoreService.store.getState()

    init {
        title = NgrxBundle.message("dialog.title")
        store.dispatch(Action.LoadTypesAction(cliTypeOptions = optionTypes))
        store.dispatch(
            Action.SelectSchematicType(
                selectedSchematicType = optionTypes.keys.first(),
                selectedSchematicParameters = ngrxCliService.getSchematicsParameters(optionTypes.keys.first())
            )
        )

        autoCompleteField.text = state.parameter
        autoCompleteField.isEnabled = state.module != null
        autoCompleteField.installProvider(
            CLIOptionsCompletionProvider(
                project,
                ngxsStoreService.store.getState().selectedSchematicParameters.keys.toList()
            )
        )

        pathField.apply {
            val relativePath = NgRxGeneratorFileUtil.getRelativePath(project, directory)
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

        schematicTypeComboBox.addItemListener {
            if (it?.stateChange == ItemEvent.SELECTED) {
                store.dispatch(
                    Action.SelectSchematicType(
                        selectedSchematicType = schematicTypeComboBox.item,
                        selectedSchematicParameters = ngrxCliService
                            .getSchematicsParameters(schematicTypeComboBox.item)
                    )
                )

                autoCompleteField.installProvider(
                    CLIOptionsCompletionProvider(
                        project,
                        ngxsStoreService.store.getState()
                            .selectedSchematicParameters.keys.toList()
                    )
                )
            }
        }

    ComboboxSpeedSearch(schematicTypeComboBox)
    init()
}

override fun createCenterPanel(): JComponent {
    return panel {
        group(NgrxBundle.message("dialog.generateInPath")) {
            row {
                cell(pathField).align(Align.FILL)
            }
        }
        separator()
        row(NgrxBundle.message("dialog.schematicType")) {}.topGap(TopGap.SMALL)
        row {
            cell(schematicTypeComboBox)
                .focused()
                .horizontalAlign(HorizontalAlign.FILL)
        }
        row(NgrxBundle.message("dialog.parameters")) {}.topGap(TopGap.SMALL)
        row {
            cell(autoCompleteField).align(
                Align.FILL
            ).apply {
                comment("(name --options)")
            }
        }
        window.minimumSize = Dimension(500, super.getPreferredSize().height)
    }
}

override fun doValidate(): ValidationInfo? {
    val fileName = autoCompleteField.text.split(" ")[0]
    var invalidFileName = false
    if (fileName.isNotBlank() && fileName.startsWith("-", ignoreCase = true)) {
        invalidFileName = true
    }
    return if (fileName.isBlank() || autoCompleteField.text.isBlank()) {
        ValidationInfo(NgrxBundle.message("dialog.parameterBlankErrorMessage"), autoCompleteField)
    }else if (invalidFileName) {
        ValidationInfo("$fileName in an invalid filename", autoCompleteField)
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
