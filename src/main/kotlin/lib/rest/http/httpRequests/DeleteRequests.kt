package lib.rest.http.httpRequests

import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Snowflake
import lib.rest.api
import lib.rest.client

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend fun Bot.deleteRequest(url: String): HttpResponse {
    return client.delete(api + url) {
        authHeaders.forEach { (k, v) ->
            header(k, v)
        }
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.deletePin(channelId: Snowflake, messageId: Snowflake) {
    deleteRequest("/channels/$channelId/pins/$messageId")
}