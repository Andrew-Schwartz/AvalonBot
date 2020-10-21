package lib.rest

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.util.*

/**
 * API v6 base address (no trailing `/`)
 */
const val api = "https://discordapp.com/api/v6"

/**
 * The client that runs it all
 *
 * Why is this here in this file all alone?
 *
 * Who knows
 *
 * I decided it should be I guess
 */
@KtorExperimentalAPI
val client = HttpClient(CIO) {
    install(WebSockets)
}