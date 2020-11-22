package lib.rest.http.httpRequests

import com.google.gson.JsonElement
import common.util.loop
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import lib.dsl.Bot
import lib.model.ChannelId
import lib.model.IntoId
import lib.rest.api
import lib.rest.client
import lib.rest.http.BucketKey
import lib.rest.http.DefaultRateLimiter
import lib.rest.http.RateLimit
import lib.util.log
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun request(
        endpoint: String,
        method: HttpMethod,
        body: Any,
        typingChannel: IntoId<ChannelId>? = null,
): HttpResponse {
    return loop {
        val key = BucketKey.ofEndpoint(endpoint)
        DefaultRateLimiter.rateLimit(key, typingChannel?.intoId()?.channel())
        val response = client.request<HttpResponse>(api + endpoint) {
            Bot.headers.forEach { (k, v) -> header(k, v) }
            header("X-RateLimit-Precision", "millisecond")
            this.method = method
            this.body = body
        }
        RateLimit.update(response, key)

        if (response.status == HttpStatusCode.TooManyRequests) {
            log("429 on '$endpoint'. Retrying...")
            delay(1000)
            null
        } else {
            response
        }
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteRequest(url: String): HttpResponse = request(url, HttpMethod.Delete, EmptyContent)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getRequest(url: String): HttpResponse = request(url, HttpMethod.Get, EmptyContent)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun patchRequest(url: String, jsonBody: String): HttpResponse =
        request(url, HttpMethod.Patch, TextContent(jsonBody, ContentType.Application.Json))

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend inline fun patchRequest(url: String, json: JsonElement) = patchRequest(url, json.toJson())

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun postRequest(
        url: String,
        jsonBody: String = "",
        typingChannel: IntoId<ChannelId>? = null,
): HttpResponse = request(url, HttpMethod.Post, TextContent(jsonBody, ContentType.Application.Json), typingChannel)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend inline fun postRequest(
        url: String,
        json: JsonElement,
        typingChannel: IntoId<ChannelId>? = null,
): HttpResponse = postRequest(url, json.toJson(), typingChannel)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun postFormDataRequest(
        url: String,
        typingChannel: IntoId<ChannelId>? = null,
        formData: FormBuilder.() -> Unit,
): HttpResponse = request(url, HttpMethod.Post, MultiPartFormDataContent(formData { formData() }), typingChannel)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun putRequest(url: String, jsonBody: String = "") {
    request(url, HttpMethod.Put, TextContent(jsonBody, ContentType.Application.Json))
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend inline fun putRequest(url: String, json: JsonElement) = putRequest(url, json.toJson())