//package lib.rest
//
//import avalonBot.api
//import io.ktor.client.HttpClient
//import io.ktor.client.request.get
//import io.ktor.client.request.header
//import io.ktor.client.response.HttpResponse
//import io.ktor.util.KtorExperimentalAPI
//import lib.misc.fromJson
//import lib.model.User
//import lib.rest.model.BotGateway
//
//
//@KtorExperimentalAPI
//suspend fun HttpClient.getRequest(url: String, header: Pair<String, String>): HttpResponse {
//    return get("$api$url") { header(header.first, header.second) }
//}
//
//@KtorExperimentalAPI
//suspend fun HttpClient.getUser(id: String = "@me"): User = getRequest("/users/$id").fromJson()
//
//@KtorExperimentalAPI
//suspend fun HttpClient.getGateway(): BotGateway = getRequest("/gateway/bot").fromJson()
//
//@KtorExperimentalAPI
//suspend fun HttpClient.getGateway(): String = getGateway().url.removePrefix("wss://")
