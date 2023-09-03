package com.github.dinbtechit.ngrx.action.cli.store

import com.github.dinbtechit.ngrx.action.cli.models.SchematicParameters
import com.github.dinbtechit.ngrx.action.cli.models.SchematicInfo
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class GenerateCLIState(
    val name: String = "",
    val types: Map<String, SchematicInfo> = mapOf(),
    val selectedSchematicType: String = "",
    val selectedSchematicParameters: Map<String, SchematicParameters> = mapOf(),
    val folderName: String = "",
    val parameter: String = "",
    val project: Project? = null,
    val workingDir: VirtualFile? = null,
    val module: CompletionModuleInfo? = null
)
