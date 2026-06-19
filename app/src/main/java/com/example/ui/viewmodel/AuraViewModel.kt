package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AuraDatabase
import com.example.data.database.JournalEntry
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AuraDatabase.getDatabase(application)
    private val repository = AuraRepository(database.journalDao())

    val entries: StateFlow<List<JournalEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentAura = MutableStateFlow<Pair<String, String>?>(null)
    val currentAura: StateFlow<Pair<String, String>?> = _currentAura
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    fun addEntry(title: String, content: String) {
        viewModelScope.launch {
            repository.insertEntry(title, content)
            analyzeAura() // Automatically analyze overall aura on new entry
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteEntry(id)
            analyzeAura()
        }
    }

    fun analyzeAura() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = repository.analyzeMoods()
            _currentAura.value = result
            _isAnalyzing.value = false
        }
    }
}
