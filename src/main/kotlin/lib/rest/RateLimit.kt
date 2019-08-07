package lib.rest

import io.ktor.client.response.HttpResponse
import io.ktor.http.Headers
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import lib.dsl.Bot
import lib.model.parseRfc1123

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.rateLimit() = with(rateLimitInfo) {
    if (remaining == 0) {
        val seconds = resetTime!! - currentTime!!
        println("rate limited for $seconds seconds")
        delay(seconds * 1000)
    }
}

data class RateLimitInfo(var limit: Int?, var remaining: Int?, var resetTime: Long?, var currentTime: Long?) {
    fun copyNonNullFrom(new: RateLimitInfo) {
        limit = new.limit ?: limit
        remaining = new.remaining ?: remaining
        resetTime = new.resetTime ?: resetTime
        currentTime = new.currentTime ?: currentTime
    }
}

fun Headers.getRateLimitInfo(): RateLimitInfo = RateLimitInfo(
        this["X-RateLimit-Limit"]?.toInt(),
        this["X-RateLimit-Remaining"]?.toInt(),
        this["X-RateLimit-Reset"]?.toLong(),
        parseRfc1123(this["Date"]!!)
)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.updateRateLimitInfo(response: HttpResponse) = rateLimitInfo.copyNonNullFrom(response.headers.getRateLimitInfo())