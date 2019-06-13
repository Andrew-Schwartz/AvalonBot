package lib.misc

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val gson: Gson = GsonBuilder().run {
    serializeNulls()
    create()
}
