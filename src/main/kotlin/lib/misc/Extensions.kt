package lib.misc

import com.google.gson.JsonElement
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText

//inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, T::class.java)
//
//inline fun <reified T> fromJson(json: JsonElement): T = gson.fromJson(json, T::class.java)

inline fun <reified T> String.fromJson(): T = gson.fromJson(this, T::class.java)

inline fun <reified T> JsonElement.fromJson(): T = gson.fromJson(this, T::class.java)

suspend inline fun <reified T> HttpResponse.fromJson(): T = readText().fromJson()

fun <T> T.toJson(): String = gson.toJson(this)

fun <T> T.toJsonTree(): JsonElement = gson.toJsonTree(this)