package com.xflip.arglassesdemo.entity

import java.io.Serializable

class MessageEvent : Serializable {
    var msgId = 0
    var msg: String? = null
    var data: Any? = null
}