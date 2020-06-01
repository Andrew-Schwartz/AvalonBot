package lib.rest.http

import io.ktor.client.statement.HttpResponse

data class RateLimit(
        var limit: Int? = null,
        var remaining: Int? = null,
        var resetAfter: Double? = null,
        var bucket: String? = null
) {
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
                it.resetAfter = this["X-RateLimit-Reset-After"]?.toDouble() ?: it.resetAfter
                it.bucket = bucket
            }
            println("$routeKey -> $bucket -> ${buckets[bucket]!!}")
        }

        fun route(routeKey: String): RateLimit {
            val bucket = routeBucketMap.getOrDefault(routeKey, GLOBAL)
            return buckets[bucket]!!
        }
    }
}