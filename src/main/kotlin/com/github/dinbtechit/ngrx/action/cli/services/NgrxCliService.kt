package com.github.dinbtechit.ngrx.action.cli.services

import com.github.dinbtechit.ngrx.action.cli.models.SchematicParameters
import com.github.dinbtechit.ngrx.action.cli.models.SchematicDetails
import com.github.dinbtechit.ngrx.action.cli.models.SchematicInfo
import com.github.dinbtechit.ngrx.action.cli.models.SchematicsCollection
import com.github.dinbtechit.ngrx.action.cli.store.CLIState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Service(Service.Level.PROJECT)
class NgrxCliService(val project: Project) {


    private fun getSchematics(): Map<String, SchematicInfo> {
        val state = project.service<CLIState>().store.state
        val schematicCollectionFile = state.module?.virtualFile?.children?.firstOrNull { it.name == "collection.json" }
        if (schematicCollectionFile != null) {
            val schematicsCollection = fromJsonFileToSchematics(schematicCollectionFile)
            if (schematicsCollection != null) {
                return schematicsCollection.schematics
            }
        }
        return mapOf()
    }

    private fun fromJsonFileToSchematics(collectionJsonFile: VirtualFile): SchematicsCollection? {
        val jsonFile = File(collectionJsonFile.path)
        if (jsonFile.exists()) {
            try {
                val content = jsonFile.readText()
                if (content.isNotBlank()) {
                    val json = Json {
                        ignoreUnknownKeys = true

                    }
                    return json.decodeFromString<SchematicsCollection>(content)
                }
            } catch (e: Exception) {
                thisLogger().error("unable to serialize - ${jsonFile.path}", e)
                return null
            }
        }
        thisLogger().error("File does not exist - $jsonFile")
        return null
    }

    /**
     *
     * Expensive method
     */
    private fun fromJsonFileToSchema(schemaJsonFile: VirtualFile): SchematicDetails? {
        val jsonFile = File(schemaJsonFile.path)
        if (jsonFile.exists()) {
            try {
                val content = jsonFile.readText()
                if (content.isNotBlank()) {
                    val json = Json {
                        ignoreUnknownKeys = true

                    }
                    return json.decodeFromString<SchematicDetails>(content)
                }
            } catch (e: Exception) {
                thisLogger().error("unable to serialize - ${jsonFile.path}", e)
                return null
            }
        }
        thisLogger().error("File does not exist - $jsonFile")
        return null
    }


    fun getTypeOptions(): Map<String, SchematicInfo> {
        return getSchematics().filter { it.key != "ng-add" && it.key != "ngrx-push-migration" }
    }

    fun getSchematicsParameters(schematicType: String): Map<String, SchematicParameters> {
        val parameters = getSchematicsDetails(schematicType)
            ?.properties
            ?.filter { it.key != "name" }
            ?.map { "--${it.key}" to it.value }
            ?.toMap()

        if (parameters != null) {
            return parameters
        }
        return mapOf()
    }

    fun getSchematicsDetails(schematicType: String): SchematicDetails? {
        try {
            val state = project.service<CLIState>().store.state

            val schematicInfo = state.types.filter { it.key == schematicType }.first()
            val schemaFilePath = schematicInfo.value.schema?.replace("./", "")
            if (!schemaFilePath.isNullOrBlank()) {
                val schemaFile = LocalFileSystem.getInstance()
                    .findFileByPath("${state.module?.absolutePath}/$schemaFilePath")
                if (schemaFile != null) {
                    return fromJsonFileToSchema(schemaFile)
                }
            }
        } catch (e: Exception) {
            thisLogger().error("Unable to get getSchematicsParameters for $schematicType", e)
        }
        return null
    }
}
