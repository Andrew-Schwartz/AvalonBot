package lib.rest

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.util.*

const val api = "https://discordapp.com/api/v6"

@KtorExperimentalAPI
val client = HttpClient(CIO) {
    install(WebSockets)
}