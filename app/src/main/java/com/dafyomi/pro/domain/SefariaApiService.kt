package com.dafyomi.pro.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sefaria API Service - Fetches actual Talmud text from Sefaria.org
 *
 * Uses v2 API which returns both Hebrew and English text.
 * Free, no API key required.
 * License: CC-BY for translations, Public Domain for Hebrew text
 */
class SefariaApiService {

    companion object {
        private const val BASE_URL = "https://www.sefaria.org/api/texts"
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }

    /**
     * Fetches both Hebrew and English text for a given masechet and daf.
     */
    suspend fun getDafText(masechetEnglish: String, dafNumber: Int): DafTextResult? {
        return withContext(Dispatchers.IO) {
            try {
                val ref = "${masechetEnglish}.${dafNumber}"
                // Use v2 API with lang=en to get both Hebrew and English
                val url = URL("$BASE_URL/$ref?lang=en")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    parseV2Response(response)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Parses v2 API response which has:
     * - "text" field with English text
     * - "he" field with Hebrew text
     */
    private fun parseV2Response(jsonResponse: String): DafTextResult? {
        return try {
            val parsed = json.parseToJsonElement(jsonResponse)
            val heText = extractTextFromField(parsed, "he")
            val enText = extractTextFromField(parsed, "text")
            if (heText != null || enText != null) {
                DafTextResult(hebrew = heText, english = enText)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extracts text from a field, handling both nested arrays and flat arrays.
     */
    private fun extractTextFromField(root: JsonElement, fieldName: String): String? {
        val field = (root as? JsonObject)?.get(fieldName) ?: return null
        return extractTextFromElement(field)
    }

    /**
     * Extracts text from a JsonElement, handling nested arrays and strings.
     */
    private fun extractTextFromElement(element: JsonElement): String? {
        return when (element) {
            is JsonPrimitive -> element.content
            is JsonArray -> {
                val allLines = mutableListOf<String>()
                for (item in element) {
                    val extracted = extractTextFromElement(item)
                    if (extracted != null) {
                        allLines.add(extracted)
                    }
                }
                if (allLines.isNotEmpty()) {
                    allLines.joinToString(" ").replace("\n", " ").trim()
                } else null
            }
            is JsonObject -> {
                // For objects, try to get text content
                val textField = element["text"]
                if (textField != null) {
                    extractTextFromElement(textField)
                } else null
            }
            else -> null
        }
    }
}

/**
 * Result containing Hebrew and English text for a daf.
 */
@Serializable
data class DafTextResult(
    val hebrew: String? = null,
    val english: String? = null
)