package lib.dsl

import lib.exceptions.EmbedLimitException
import lib.model.Color
import lib.model.Timestamp
import lib.model.channel.*
import lib.model.timestamp
import lib.model.user.User
import java.io.File
import java.io.InputStream
import java.time.OffsetDateTime
import kotlin.collections.set

data class RichEmbed internal constructor(
        var title: String? = null,
        var description: String? = null,
        var timestamp: Timestamp? = null,
        var color: Color? = null,
        var url: String? = null,
        var author: User? = null,
        var image: EmbedImage? = null,
        var thumbnail: EmbedThumbnail? = null,
        var footer: EmbedFooter? = null
) {
    companion object {
        val empty = RichEmbed()
    }

    val isEmpty: Boolean
        get() = this == empty

    val isNotEmpty: Boolean
        get() = this != empty

    private val fields: ArrayList<EmbedField> = ArrayList()

    private val files: MutableMap<String, InputStream> = mutableMapOf()

    fun timestamp(time: OffsetDateTime = OffsetDateTime.now()) {
        timestamp = time.timestamp()
    }

    fun addField(name: String, value: String, inline: Boolean = false) {
        fields += EmbedField(
                name = name.trimTo(256),
                value = value.trimTo(256),
                inline = inline
        )
    }

    fun addBlankField(inline: Boolean = false) = addField("\u200B", "\u200B", inline)

    fun image(file: File, name: String = file.name) {
        files[name] = file.inputStream()

        image = EmbedImage(url = "attachment://$name")
    }

    /**
     * uploads [file] as an attachment but does not actually attach it,
     * to do that create an image with url "attachment://$filename".
     * The preferred way to do this is by calling [image] with the same parameters
     */
    fun addFile(file: File, name: String = file.name) {
        files[name] = file.inputStream()
    }

    fun image(url: String) {
        image = EmbedImage(url)
    }

    fun footer(text: String, iconUrl: String = "", proxyIconUrl: String = "") {
        footer = EmbedFooter(text, iconUrl, proxyIconUrl)
    }

    fun build(): Embed {
        val author = author?.let {
            EmbedAuthor(
                    name = it.username.trimTo(256),
                    url = null,
                    iconUrl = null,
                    proxyIconUrl = null
            )
        }

        val fields = fields.takeUnless { it.isEmpty() }?.toTypedArray()
        val files = files.takeUnless { it.isEmpty() }

        return Embed(
                title = title?.trimTo(256),
                type = "rich",
                description = description?.trimTo(2048),
                timestamp = timestamp,
                color = color?.value?.toInt(),
                url = url,
                footer = footer,
                image = image,
                thumbnail = null,
                video = null,
                provider = null,
                author = author,
                fields = fields?.take(25)?.toTypedArray(),
                files = files
        )
    }

    private fun String.trimTo(maxLen: Int, end: String = "..."): String {
        return when {
            length > maxLen -> take(maxLen - end.length) + end
            else -> this
        }
    }

    private fun ensureLimits() {
//        if ((title?.length ?: 0) > 256)
//            throw EmbedLimitException("Embed titles cannot be more than 256 characters")
//
//        if ((description?.length ?: 0) > 2048)
//            throw EmbedLimitException("Embed descriptions cannot be more than 2048 characters")
//
//        if (fields.size > 25)
//            throw EmbedLimitException("Embed cannot have more than 25 fields")
//
//        if (fields.any { it.name.length > 256 })
//            throw EmbedLimitException("Field names cannot be more than 256 characters")
//
//        if (fields.any { it.value.length > 256 })
//            throw EmbedLimitException("Field values cannot be more than 256 characters")
//
//        if ((footer?.text?.length ?: 0) > 2048)
//            throw EmbedLimitException("Footer text cannot be more than 2048 characters")

//        if ((author?.username?.length ?: 0) > 256)
//            throw EmbedLimitException("Author name cannot be more than 256 characters")

        val totalChars = (title?.length ?: 0) +
                (description?.length ?: 0) +
                (fields.sumBy { it.name.length + it.value.length }) +
                (footer?.text?.length ?: 0) +
                (author?.username?.length ?: 0)
        if (totalChars > 6000)
            throw EmbedLimitException("Embeds cannot have more than 6000 characters in total")
    }

    operator fun invoke(λ: RichEmbed.() -> Unit): RichEmbed = apply { λ() }
}

suspend fun embed(builder: suspend RichEmbed.() -> Unit): RichEmbed = RichEmbed().apply { builder() }
