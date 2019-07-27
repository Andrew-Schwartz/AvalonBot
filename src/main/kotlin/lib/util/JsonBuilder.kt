package lib.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class J {
    var json = JsonObject()

    fun <V> add(k: String, v: V?): Unit = with(json) {
        when (v) {
            null -> add(k, null)
            is Char -> addProperty(k, v)
            is Number -> addProperty(k, v)
            is String -> addProperty(k, v)
            is Boolean -> addProperty(k, v)
            is JsonElement -> add(k, v)
            else -> {
                add(k, v.toJsonTree())
            }
        }
    }

    fun build(): JsonObject = json

    //    inline operator fun <reified V> invoke(vararg pairs: Pair<String, V?>): JsonObject = JsonObject().apply {
//        for (pair in pairs) {
//            val (k, v) = pair
//            when (v) {
//                null -> add(k, null)
//                is Char -> addProperty(k, v)
//                is Number -> addProperty(k, v)
//                is String -> addProperty(k, v)
//                is Boolean -> addProperty(k, v)
//                is JsonElement -> add(k, v)
//                else -> {
//                    add(k, v.toJsonTree())
//                }
//            }
//        }
//    }
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

        inline operator fun <reified V> invoke(k: String, v: V?): JsonObject = JsonObject().apply {
            when (v) {
                null -> add(k, null)
                is Char -> addProperty(k, v)
                is Number -> addProperty(k, v)
                is String -> addProperty(k, v)
                is Boolean -> addProperty(k, v)
                is JsonElement -> add(k, v)
                else -> {
                    add(k, v.toJsonTree())
                }
            }
        }

    }
}

fun j(λ: J.() -> Unit): JsonObject = J().apply(λ).build()

//fun test(): String {
//    //language=JSON
//    """
//        {
//          "content": "Message content here",
//          "embed": {
//            "title": "Embed title",
//            "fields": [
//              {
//                "name": "field name",
//                "value": "field value",
//                "inline": false
//              }
//            ]
//          }
//        }
//    """.trimIndent()
//
//    """
//        {
//        "content":"message content here",
//          "embed":{
//            "title":"embed title",
//            "fields":[
//              {
//              "name":"field name",
//              }
//            ]
//          }
//        }
//    """.trimIndent()
//
//    return j {
////        add("content", "message content here")
////        add("embed", j {
////            add("title", "embed title")
////            add("fields", J[
////                    j {
////                        add("name", "field name")
////                    }
////            ])
////        })
//    }.toJson()
//
////    J(
////            "content" to "Message content here",
////            "embed" to J(
////                    "title" to "Embed title",
////                    "fields" to J[
////                            J(
////                                    "name" to "field name"
////                            ),
////                            J()
////                    ]
////            )
////    )
//}