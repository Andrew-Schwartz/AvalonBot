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

/**
 * Builder for Discord's Embeds.
 */
data class RichEmbed internal constructor(
        var title: String? = null,
        var description: String? = null,
        private var timestamp: Timestamp? = null,
        var color: Color? = null,
        var url: String? = null,
        var author: User? = null,
        private var image: EmbedImage? = null,
        private var thumbnail: EmbedThumbnail? = null,
        private var footer: EmbedFooter? = null
) {
    private val fields: ArrayList<EmbedField> = ArrayList()

    private val files: MutableMap<String, InputStream> = mutableMapOf()

    companion object {
        private val empty = RichEmbed()
    }

    /**
     * Utility method if this embed is completely empty, ie in equal to `RichEmbed()`
     */
    val isEmpty: Boolean
        get() = this == empty

    /**
     * The text of the footer
     */
    var footerText: String?
        get() = footer?.text
        set(value) {
            value?.let {
                // todo figure out iconUrl and proxyIconUrl?
                footer = EmbedFooter(it, "", "")
            }
        }

    /**
     * Sets the timestamp. By default, uses the timestamp of when this method is called
     */
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
        addFile(file, name)

        image = EmbedImage(url = "attachment://$name")
    }

    fun image(url: String) {
        image = EmbedImage(url)
    }

    /**
     * uploads [file] as an attachment but does not actually attach it,
     * to do that create an image with url "attachment://$filename".
     *
     * The preferred way to do this is by calling [image] with the same parameters
     */
    fun addFile(file: File, name: String = file.name) {
        files[name] = file.inputStream()
    }

    fun footer(text: String, iconUrl: String = "", proxyIconUrl: String = "") {
        footer = EmbedFooter(text, iconUrl, proxyIconUrl)
    }

    override fun toString(): String {
        return super.toString() + "fields=$fields"
    }

    private fun String.trimTo(maxLen: Int, end: String = "..."): String {
        return when {
            length > maxLen -> take(maxLen - end.length) + end
            else -> this
        }
    }

    private fun ensureLimits() {
        val totalChars = (title?.length ?: 0) +
                (description?.length ?: 0) +
                (fields.sumBy { it.name.length + it.value.length }) +
                (footer?.text?.length ?: 0) +
                (author?.username?.length ?: 0)
        if (totalChars > 6000)
            throw EmbedLimitException("Embeds cannot have more than 6000 characters in total")
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

//        if (footer == null && footerText != null)
//            footer(footerText!!)

        ensureLimits()

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

    /**
     * Make it nice and easy to build a [RichEmbed]
     */
    suspend operator fun invoke(λ: suspend RichEmbed.() -> Unit): RichEmbed = apply { λ() }
}

suspend fun embed(builder: suspend RichEmbed.() -> Unit): RichEmbed = RichEmbed().apply { builder() }
