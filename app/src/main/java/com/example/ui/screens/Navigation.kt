package com.example.ui.screens

import kotlinx.serialization.Serializable

@Serializable
object HomeDestination

@Serializable
data class EntryDestination(val entryId: Int = -1)
