package lib.misc

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText

val gson: Gson = GsonBuilder().run {
    serializeNulls()
    create()
}

inline fun <reified T> String.fromJson(): T = gson.fromJson(this, T::class.java)

inline fun <reified T> JsonElement.fromJson(): T = gson.fromJson(this, T::class.java)

suspend inline fun <reified T> HttpResponse.fromJson(): T = readText().fromJson()

fun <T> T.toJson(): String = gson.toJson(this)

fun <T> T.toJsonTree(): JsonElement = gson.toJsonTree(this)