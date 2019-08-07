package lib.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class J {
    var json = JsonObject()

    infix fun <V> String.to(v: V?): Unit = with(json) {
        when (v) {
            null -> add(this@to, null)
            is Char -> addProperty(this@to, v)
            is Number -> addProperty(this@to, v)
            is String -> addProperty(this@to, v)
            is Boolean -> addProperty(this@to, v)
            is JsonElement -> add(this@to, v)
            else -> {
                add(this@to, v.toJsonTree())
            }
        }
    }

    fun build(): JsonObject = json

    companion object {
        operator fun get(vararg vals: Any?): JsonArray = JsonArray().apply {
            for (v in vals) {
                when (v) {
                    is Char -> this.add(v)
                    is Number -> this.add(v)
                    is String -> this.add(v)
                    is Boolean -> this.add(v)
                    is JsonElement -> this.add(v)
                    else -> add(v.toJsonTree())
                }
            }
        }
    }
}

fun j(λ: J.() -> Unit): JsonObject = J().apply(λ).build()