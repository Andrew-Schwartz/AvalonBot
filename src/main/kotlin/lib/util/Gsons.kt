@file:Suppress("NOTHING_TO_INLINE")

package lib.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.ktor.client.statement.*
import lib.model.ActivityType
import lib.model.ActivityTypeAdapter
import kotlin.reflect.KClass

val gson: Gson = GsonBuilder().run {
    registerTypeAdapter(ActivityType::class.java, ActivityTypeAdapter())
    //    serializeNulls()
    create()
}

inline fun <T : Any> String.fromJson(kclass: KClass<T>): T = gson.fromJson(this, kclass.java)

inline fun <T : Any> JsonElement.fromJson(kclass: KClass<T>): T = gson.fromJson(this, kclass.java)

inline fun <reified T : Any> String.fromJson(): T = fromJson(T::class)

inline fun <reified T : Any> JsonElement.fromJson(): T = fromJson(T::class)

suspend inline fun <reified T : Any> HttpResponse.fromJson(): T = readText().fromJson()

inline fun <T> T.toJson(): String = gson.toJson(this)

inline fun <T> T?.toJsonTree(): JsonElement = gson.toJsonTree(this)
