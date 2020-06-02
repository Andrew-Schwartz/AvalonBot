package lib.rest.http.httpRequests

import common.util.loop
import common.util.now
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.rest.api
import lib.rest.client
import lib.rest.http.RateLimit

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.request(
        routeKey: String,
        endpoint: String,
        method: HttpMethod,
        body: Any
): HttpResponse = loop {
    RateLimit.route(routeKey).limit()
    val response = client.request<HttpResponse>(api + endpoint) {
        authHeaders.forEach { (k, v) -> header(k, v) }
        header("X-RateLimit-Precision", "millisecond")
        this.method = method
        this.body = body
    }
    RateLimit.update(response, routeKey)
    if (response.status == HttpStatusCode.TooManyRequests) {
        println("[${now()}] 429 on '$endpoint'. Retrying...")
        null
    } else {
        response
    }
}