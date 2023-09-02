package com.github.dinbtechit.ngrx.action.cli.services

import com.github.dinbtechit.ngrx.action.cli.models.SchemaJson
import com.github.dinbtechit.ngrx.action.cli.models.SchematicInfo
import com.github.dinbtechit.ngrx.action.cli.models.SchematicsCollection
import com.github.dinbtechit.ngrx.action.cli.store.CLIState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Service(Service.Level.PROJECT)
class NgrxCliService(val project: Project) {
    private val state = project.service<CLIState>().store.state

    private fun getSchematics(): Map<String, SchematicInfo> {
        val schematicCollectionFile = state.module?.virtualFile?.children?.firstOrNull { it.name == "collection.json" }
        if(schematicCollectionFile != null) {
            val schematicsCollection = fromJsonFileToSchematics(schematicCollectionFile)
            if (schematicsCollection != null) {
                return schematicsCollection.schematics
            }
        }
        return mapOf()
    }

    private fun getSchematicsFoldersAndDesc(hasProperties: Boolean = false): Map<VirtualFile, SchemaJson?> {
        val srcDir = state.module?.virtualFile?.children?.first { it.name == "src" }
        if (srcDir?.children != null) {
            val cliFolder = srcDir.children.filter { it.isDirectory }.map { it ->
                val schemaJsonFile = it.children.firstOrNull { it.name == "schema.json" }
                var pair: Pair<VirtualFile, SchemaJson?>? = null
                if (schemaJsonFile != null) {
                    val schemaJson =  fromJsonFileToSchema(schemaJsonFile)
                    pair = if (hasProperties) {
                        if (!schemaJson?.properties.isNullOrEmpty()) {
                            Pair(it, schemaJson)
                        } else {
                            null
                        }
                    } else {
                        Pair(it, schemaJson)
                    }
                }
                pair
            }.filterNotNull()
                .toMap()

            return cliFolder
        }
        return mapOf() // Or return an empty list
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
    private fun fromJsonFileToSchema(schemaJsonFile: VirtualFile): SchemaJson? {
        val jsonFile = File(schemaJsonFile.path)
        if (jsonFile.exists()) {
            try {
                val content = jsonFile.readText()
                if (content.isNotBlank()) {
                    val json = Json {
                        ignoreUnknownKeys = true

                    }
                    return json.decodeFromString<SchemaJson>(content)
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
        return getSchematics()
    }
}
