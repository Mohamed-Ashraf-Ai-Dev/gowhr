package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.JournalDao
import com.example.data.database.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuraRepository(private val journalDao: JournalDao) {

    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun insertEntry(title: String, content: String) {
        val entry = JournalEntry(title = title, content = content)
        journalDao.insertEntry(entry)
    }

    suspend fun deleteEntry(id: Int) {
        journalDao.deleteEntryById(id)
    }

    private val apiKeys = listOf(
        "AIzaSyDbO_XVjWhE0Tl_RPJ3q0wfATk5ErGEdoQ",
        "AIzaSyCzHROTXqHfsKoE5njHvpbqfXKoh_TuVe8",
        "AIzaSyCAydZJD1VYorEIMcamUCQ_Ax8-7ITO7fk"
    )

    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun analyzeMoods(): Pair<String, String>? {
        val recentEntries = journalDao.getRecentEntriesList()
        if (recentEntries.isEmpty()) return null

        val recentText = recentEntries.take(5).joinToString("\\n---\\n") { "Title: ${it.title}\\nContent: ${it.content}" }
        
        val prompt = "Analyze the emotional tone of the following recent journal entries. " +
                "Summarize the user's overall 'Aura' (mood/mindset) in 1-2 lyrical, empathetic sentences. " +
                "Also provide a hex color code that represents this aura. " +
                "\\nEntries:\\n$recentText"

        val schema = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("aura_summary") {
                    put("type", "STRING")
                }
                putJsonObject("color_hex") {
                    put("type", "STRING")
                }
            }
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = schema,
                temperature = 0.7f
            )
        )

        for (key in apiKeys) {
            try {
                val response = RetrofitClient.service.generateContent(key, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonText != null) {
                    val json = jsonParser.parseToJsonElement(jsonText).jsonObject
                    val summary = json["aura_summary"]?.jsonPrimitive?.content ?: "Calm mind"
                    val hex = json["color_hex"]?.jsonPrimitive?.content ?: "#888888"
                    return Pair(summary, hex)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If it fails, continue to the next key.
            }
        }
        return null
    }
}
