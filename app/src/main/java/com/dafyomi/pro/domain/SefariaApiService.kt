package com.dafyomi.pro.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            val heText = extractTextFromField(jsonResponse, "\"he\":")
            val enText = extractTextFromField(jsonResponse, "\"text\":")
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
     * Extracts text from a field that contains nested arrays.
     */
    private fun extractTextFromField(jsonResponse: String, fieldMarker: String): String? {
        val fieldIndex = jsonResponse.indexOf(fieldMarker)
        if (fieldIndex == -1) return null

        // Find the opening bracket after the colon
        val bracketStart = jsonResponse.indexOf("[", fieldIndex)
        if (bracketStart == -1) return null

        // Find matching closing bracket
        var depth = 1
        var endIndex = bracketStart + 1
        while (endIndex < jsonResponse.length && depth > 0) {
            when (jsonResponse[endIndex]) {
                '[' -> depth++
                ']' -> depth--
            }
            endIndex++
        }

        if (depth != 0) return null

        val arrayContent = jsonResponse.substring(bracketStart, endIndex)
        return parseNestedArray(arrayContent)
    }

    /**
     * Parses nested array of strings (like [["a","b"], ["c","d"]])
     * into a single readable string.
     */
    private fun parseNestedArray(content: String): String? {
        val allLines = mutableListOf<String>()
        var i = 0

        while (i < content.length) {
            // Skip whitespace and brackets
            while (i < content.length && (content[i] == ' ' || content[i] == '\n' || content[i] == '[' || content[i] == ']' || content[i] == ',')) {
                i++
            }
            if (i >= content.length) break

            if (content[i] == '"') {
                i++ // skip opening quote
                val line = StringBuilder()

                while (i < content.length) {
                    val c = content[i]
                    when {
                        c == '\\' && i + 1 < content.length -> {
                            val next = content[i + 1]
                            when (next) {
                                'n' -> { line.append('\n'); i += 2 }
                                '"' -> { line.append('"'); i += 2 }
                                '\\' -> { line.append('\\'); i += 2 }
                                else -> { line.append(c); i++ }
                            }
                        }
                        c == '"' -> break
                        else -> { line.append(c); i++ }
                    }
                }

                if (line.isNotEmpty()) {
                    val trimmed = line.toString().trim()
                    // Remove HTML tags like <big><strong> and </strong></big>
                    val clean = trimmed.replace(Regex("<[^>]*>"), "")
                    allLines.add(clean)
                }
                i++ // skip closing quote
            } else {
                i++
            }
        }

        return if (allLines.isNotEmpty()) {
            allLines.joinToString(" ").replace("\n", " ").trim()
        } else null
    }
}

data class DafTextResult(
    val hebrew: String?,   // Hebrew text
    val english: String?   // English translation
)
