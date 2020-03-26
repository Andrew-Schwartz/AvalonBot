package lib.model.user

import com.google.gson.annotations.SerializedName

enum class Visibility {
    /**
     * invisible to everyone except the user themselves
     */
    @SerializedName("0")
    None,
    /**
     * visible to everyone
     */
    @SerializedName("1")
    Everyone
}