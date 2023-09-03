package com.github.dinbtechit.ngrx.action.cli.models

import kotlinx.serialization.Serializable

@Serializable
data class SchematicsCollection (
    val schematics: Map<String, SchematicInfo>
)

@Serializable
data class SchematicInfo(
    val aliases: List<String>? = null,
    val factory: String? = null,
    val schema: String? = null,
    val description: String? = null
)


@Serializable
data class SchematicDetails(
    val title: String?,
    val type: String?,
    val properties: Map<String, SchematicParameters>? = null,
    val required: List<String>? = null
)

@Serializable
data class SchematicParameters(
    val type: String? = null,
    val description: String? = null
)
