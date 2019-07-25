package lib.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText

val gson: Gson = GsonBuilder().run {
    //    serializeNulls()
    create()
}

inline fun <reified T> String.fromJson(): T = gson.fromJson(this, T::class.java)

inline fun <reified T> JsonElement.fromJson(): T = gson.fromJson(this, T::class.java)

suspend inline fun <reified T> HttpResponse.fromJson(): T = readText().fromJson()

fun <T> T.toJson(): String = gson.toJson(this)

fun <T> T.toJsonElement(): JsonElement = gson.toJsonTree(this)

/**
 * Json builder
 */
object J {
    inline operator fun <reified V> get(k: String, v: V): JsonObject = JsonObject().apply {
        when (v) {
            is Char -> addProperty(k, v)
            is Number -> addProperty(k, v)
            is String -> addProperty(k, v)
            is Boolean -> addProperty(k, v)
            is JsonElement -> add(k, v)
            else -> add(k, v.toJsonElement())
        }
    }
}