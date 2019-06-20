package lib.dsl

import lib.exceptions.EmbedLimitException
import lib.model.*
import java.time.OffsetDateTime

data class RichEmbed(
        var title: String? = null,
        var description: String? = null,
        var timestamp: Timestamp? = null,
        var color: Color? = null,
        var author: User? = null,
        var image: EmbedImage? = null
) {
    companion object {
        val empty = RichEmbed()
    }

    private val footer: EmbedFooter? = null
    private val fields: ArrayList<EmbedField> = arrayListOf()

    fun timestamp() {
        timestamp = OffsetDateTime.now().timestamp()
    }

    fun field(name: String, value: String, inline: Boolean = true) {
        fields += EmbedField(
                name = name,
                value = value,
                inline = inline
        )
    }

    // works when URL is for cdn.discordapp.com, ie
    // https://cdn.discordapp.com/attachments/492122962451759124/590027285512454144/Food_Cart_OTK.jpg
    //                                        channel id         somewhat near message id
    fun image(url: String) {
//        val attachment =
        image = EmbedImage(url = url)
    }

    fun build(): Embed {
        ensureLimits()

        val author = author?.let {
            EmbedAuthor(
                    name = it.username,
                    url = null,
                    iconUrl = null,
                    proxyIconUrl = null
            )
        }

        val fields = fields.takeUnless { it.isEmpty() }?.toTypedArray()

        return Embed(
                title = title,
                type = "rich",
                description = description,
                timestamp = timestamp,
                color = color?.value?.toInt(),
                url = null,
                footer = null,
                image = image,
                thumbnail = null,
                video = null,
                provider = null,
                author = author,
                fields = fields
        )
    }

    private fun ensureLimits() {
        if ((title?.length ?: 0) > 256)
            throw EmbedLimitException("Embed titles cannot be more than 256 characters")

        if ((description?.length ?: 0) > 2048)
            throw EmbedLimitException("Embed descriptions cannot be more than 2048 characters")

        if (fields.size > 25)
            throw EmbedLimitException("Embed cannot have more than 25 fields")

        if (fields.any { it.name.length > 256 })
            throw EmbedLimitException("Field names cannot be more than 256 characters")

        if (fields.any { it.value.length > 256 })
            throw EmbedLimitException("Field values cannot be more than 256 characters")

        if ((footer?.text?.length ?: 0) > 2048)
            throw EmbedLimitException("Footer text cannot be more than 2048 characters")

        if ((author?.username?.length ?: 0) > 256)
            throw EmbedLimitException("Author name cannot be more than 256 characters")

        val totalChars = (title?.length ?: 0) +
                (description?.length ?: 0) +
                (fields.sumBy { it.name.length + it.value.length }) +
                (footer?.text?.length ?: 0) +
                (author?.username?.length ?: 0)
        if (totalChars > 6000)
            throw EmbedLimitException("Embeds cannot have more than 6000 characters in total")
    }

//    operator fun invoke(λ: RichEmbed.() -> Unit): RichEmbed {
//        λ()
//        return this
//    }
}

//suspend fun (suspend RichEmbed.() -> Unit).build(): Embed = RichEmbed().apply { this@build() }.build()

suspend fun embed(builder: suspend RichEmbed.() -> Unit): Embed = RichEmbed().apply { builder() }.build()
