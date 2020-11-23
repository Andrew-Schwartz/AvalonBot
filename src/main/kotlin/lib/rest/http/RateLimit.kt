package lib.rest.http

import common.util.A
import common.util.durationSince
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import lib.dsl.Bot
import lib.dsl.startTyping
import lib.model.ChannelId
import lib.model.IntoId
import lib.util.log
import java.time.Duration
import java.time.Instant
import kotlin.math.pow

data class RateLimit(
        var limit: Int? = null,
        var remaining: Int? = null,
        var reset: Instant? = null,
        var bucket: BucketKey? = null,
) {
    val mustLimit get() = limitMillis != 0L

    val limitMillis
        get(): Long {
            val duration: Duration = reset?.durationSince(Instant.now()) ?: Duration.ZERO
            return if (remaining == 0 && !duration.isNegative) {
                ((duration.seconds + duration.nano / 10.0.pow(9)) * 1000.0).toLong()
            } else {
                0L
            }
        }

    override fun toString(): String {
        val duration = reset?.durationSince(Instant.now())
                ?.takeIf { !it.isNegative }
                ?.let { it.seconds + it.nano / 10.0.pow(9) }
        return "RateLimit(limit=$limit, remaining=$remaining, reset=$duration, bucket=$bucket)"
    }

    companion object {
        private val buckets: MutableMap<BucketKey, RateLimit> = mutableMapOf()

        fun update(response: HttpResponse, key: BucketKey): Unit = with(response) {
            buckets.getOrPut(key) { RateLimit() }.apply {
                limit = headers["X-RateLimit-Limit"]?.toInt() ?: limit
                remaining = headers["X-RateLimit-Remaining"]?.toInt() ?: remaining
                headers["X-RateLimit-Reset-After"]?.toDouble()?.let { secs ->
                    reset = Instant.now().plusMillis((secs * 1000).toLong())
                }
            }
            log("$key -> ${buckets[key]}")
        }

        operator fun get(endpoint: String): RateLimit = RateLimit[BucketKey.ofEndpoint(endpoint)]

        operator fun get(key: BucketKey): RateLimit = buckets.getOrPut(key) { RateLimit(bucket = key) }
    }
}

inline class BucketKey(private val route: String) {
    companion object {
        fun ofEndpoint(endpoint: String): BucketKey {
            val route = endpoint
                    .substringBefore("?")
                    .split('/')
                    .windowed(2)
                    .joinToString("/") {
                        val (prev, part) = it
                        if (Regex("""\d{16,19}""").matches(part) && prev !in A["channels", "guilds"]) {
                            ":id"
                        } else {
                            part
                        }
                    }
            return BucketKey(route)
        }
    }

    override fun toString(): String = route
}

interface RateLimiter {
    suspend fun rateLimit(key: BucketKey, typingChannel: IntoId<ChannelId>? = null)
}

object DefaultRateLimiter : RateLimiter {
    @ExperimentalCoroutinesApi
    @KtorExperimentalAPI
    override suspend fun rateLimit(key: BucketKey, typingChannel: IntoId<ChannelId>?) {
        with(RateLimit[key]) {
            if (mustLimit) {
                log("rate limited for $limitMillis ms in bucket $bucket")
                if (typingChannel != null && !RateLimit["/channels/${typingChannel.intoId()}/typing"].mustLimit) {
                    typingChannel.startTyping()
                }
                delay(limitMillis)
            }
        }
    }
}

// todo (not sure this is really possible?)
object RateLimitManager : RateLimiter {
    //    val threads = mutableListOf<Thread>()
    val coroutines = mutableListOf<CoroutineScope>()
    val map = mutableMapOf<BucketKey, CoroutineScope>()

    // this probably doesn't work?
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override suspend fun rateLimit(key: BucketKey, typingChannel: IntoId<ChannelId>?) {
        val scope = map.getOrPut(key) { CoroutineScope(Bot.coroutineContext) }
        withContext(scope.coroutineContext) {
            DefaultRateLimiter.rateLimit(key, typingChannel)
        }
    }
}
