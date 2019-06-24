package lib.dsl

import lib.exceptions.EmbedLimitException
import lib.model.*
import java.io.File
import java.io.InputStream
import java.time.OffsetDateTime

data class RichEmbed internal constructor(
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
    private val fields: ArrayList<EmbedField> = ArrayList()

    val files: MutableMap<String, InputStream> = mutableMapOf()

    fun timestamp() {
        timestamp = OffsetDateTime.now().timestamp()
    }

    fun addField(name: String, value: String, inline: Boolean = false) {
        fields += EmbedField(
                name = name,
                value = value,
                inline = inline
        )
    }

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
        val files = files.takeUnless { it.isEmpty() }

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
                fields = fields,
                files = files
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

    operator fun invoke(λ: RichEmbed.() -> Unit): RichEmbed = apply { λ() }
}

//suspend fun (suspend RichEmbed.() -> Unit).build(): Embed = RichEmbed().apply { this@build() }.build()

suspend fun embed(builder: suspend RichEmbed.() -> Unit): RichEmbed = RichEmbed().apply { builder() }
