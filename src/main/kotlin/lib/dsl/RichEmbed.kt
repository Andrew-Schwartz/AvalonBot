package lib.dsl

import lib.model.*
import java.time.OffsetDateTime

class RichEmbed {
    var title: String? = null
    var description: String? = null
    var timestamp: Timestamp? = null
    var color: Color? = null
    var author: User? = null

    fun timestamp() {
        timestamp = OffsetDateTime.now().timestamp()
    }

    fun build(): Embed = Embed(
            title = title,
            type = "rich",
            description = description,
            timestamp = timestamp,
            color = color?.value?.toInt(),
            url = null,
            footer = null,
            image = null,
            thumbnail = null,
            video = null,
            provider = null,
            author = EmbedAuthor(
                    name = author?.username,
                    url = null,
                    iconUrl = null,
                    proxyIconUrl = null
            ),
            fields = null
    )
}

suspend fun (suspend RichEmbed.() -> Unit).build(): Embed = RichEmbed().apply { this@build() }.build()