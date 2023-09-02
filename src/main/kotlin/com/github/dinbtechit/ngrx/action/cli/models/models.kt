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
data class SchemaJson(
    val `$schema`: String,
    val `$id`: String,
    val title: String?,
    val type: String?,
    val properties: Map<String, Property>? = null,
    val required: List<String>? = null
)

@Serializable
data class Property(
    val type: String? = null,
    val description: String? = null
)
