package lib.rest.http.httpRequests

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.rest.model.BotGateway
import lib.util.fromJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun gateway(): String = getRequest("/gateway/bot").fromJson<BotGateway>().url.removePrefix("wss://")