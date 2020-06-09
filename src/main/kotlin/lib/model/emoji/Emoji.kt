package lib.model.emoji

import com.google.gson.annotations.SerializedName
import lib.model.EmojiId
import lib.model.IntoId
import lib.model.RoleId
import lib.model.user.User

@Suppress("ArrayInDataClass")
data class Emoji(
        val id: EmojiId?,
        val name: String,
        val roles: Array<RoleId>?,
        val user: User,
        @SerializedName("require_colons") val requireColons: Boolean?,
        val managed: Boolean?,
        val animated: Boolean?
) : IntoId<EmojiId?> {
    override fun intoId(): EmojiId? = id
}