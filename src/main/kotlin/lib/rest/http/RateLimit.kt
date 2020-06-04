package lib.rest.http

import common.util.durationSince
import common.util.now
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.delay
import java.time.Instant
import kotlin.math.pow

data class RateLimit(
        var limit: Int? = null,
        var remaining: Int? = null,
        var reset: Instant? = null,
        var bucket: String? = null
) {
    suspend fun limit() {
        reset?.let { reset ->
            val duration = reset.durationSince(Instant.now())
            if (remaining == 0 && !duration.isNegative) {
                val delayTime = duration.seconds + duration.nano / 10.0.pow(9)
                println("rate limited for $delayTime seconds in bucket $bucket")
                delay((delayTime * 1000.0).toLong())
            }
        }
    }

    companion object {
        private const val GLOBAL = "GLOBAL"
        private val routeBucketMap: MutableMap<String, String> = mutableMapOf()
        private val buckets: MutableMap<String, RateLimit> = mutableMapOf(GLOBAL to RateLimit())

        fun update(response: HttpResponse, routeKey: String): Unit = with(response.headers) {
            val bucket = this["X-RateLimit-Bucket"] ?: GLOBAL
            routeBucketMap[routeKey] = bucket
            buckets.getOrPut(bucket) { RateLimit() }.let {
                it.limit = this["X-RateLimit-Limit"]?.toInt() ?: it.limit
                it.remaining = this["X-RateLimit-Remaining"]?.toInt() ?: it.remaining
                this["X-RateLimit-Reset-After"]?.toDouble()?.let { secs ->
                    it.reset = Instant.now().plusMillis((secs * 1000).toLong())
                }
                it.bucket = bucket
            }
            println("[${now()}] $routeKey -> $bucket -> ${buckets[bucket]!!}")
        }

        fun route(routeKey: String): RateLimit {
            val bucket = routeBucketMap.getOrDefault(routeKey, GLOBAL)
            return buckets[bucket]!!
        }
    }
}