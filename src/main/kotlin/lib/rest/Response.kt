package lib.rest

import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText

data class Response(val status: Int, val content: String)

suspend fun HttpResponse.toResponse(): Response = Response(status.value, readText())