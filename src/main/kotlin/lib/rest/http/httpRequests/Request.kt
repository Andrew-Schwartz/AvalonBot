package lib.rest.http.httpRequests

import common.util.loop
import common.util.now
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
import lib.rest.http.RateLimit

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun request(
        endpoint: String,
        method: HttpMethod,
        body: Any,
        typingChannel: IntoId<ChannelId>? = null
): HttpResponse {
    return loop {
        val key = BucketKey.ofEndpoint(endpoint)
        RateLimit[key].limit(typingChannel?.intoId()?.channel())
        val response = client.request<HttpResponse>(api + endpoint) {
            Bot.headers.forEach { (k, v) -> header(k, v) }
            header("X-RateLimit-Precision", "millisecond")
            this.method = method
            this.body = body
        }
        RateLimit.update(response, key)

        if (response.status == HttpStatusCode.TooManyRequests) {
            println("[${now()}] 429 on '$endpoint'. Retrying...")
            delay(1000)
            null
        } else {
            response
        }
    }
}