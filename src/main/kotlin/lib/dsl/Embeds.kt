package lib.dsl

import lib.misc.get
import lib.model.Embed
import lib.model.Timestamp
import lib.model.timestamp
import java.time.OffsetDateTime

fun embed(λ: CreateEmbed.() -> Unit): Embed {
    return CreateEmbed().apply(λ).build()
}

class CreateEmbed {
    var title: String? = null
    var description: String? = null
    var timestamp: Timestamp? = null
    var color: Int? = null

    fun timestamp() {
        timestamp = OffsetDateTime.now().timestamp()
    }

    fun color(color: Int) {
        this.color = color
    }

    fun color(color: String) {
        val r = color[1..3].toInt(16)
        val g = color[3..5].toInt(16)
        val b = color[5..7].toInt(16)
    }

    fun build(): Embed = Embed(
            title = title,
            type = "rich",
            description = description,
            timestamp = timestamp,
            color = color,
            url = null,
            footer = null,
            image = null,
            thumbnail = null,
            video = null,
            provider = null,
            author = null,
            fields = null
    )
}