package common.util

import lib.model.channel.Message

typealias Action<T> = suspend T.() -> Unit

typealias Listener<T> = suspend T.() -> Unit

typealias MessageListener = Listener<Message>
