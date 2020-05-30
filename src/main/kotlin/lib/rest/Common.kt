package lib.rest

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.util.KtorExperimentalAPI

const val api = "https://discordapp.com/api/v6"

@KtorExperimentalAPI
val client = HttpClient(CIO) {
    install(WebSockets) /*{
        timeout = java.time.Duration.ofSeconds(-1)
    }
*/
}