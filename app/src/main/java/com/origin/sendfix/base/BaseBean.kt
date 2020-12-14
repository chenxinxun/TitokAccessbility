package com.origin.sendfix.base

open class BaseBean {
    var status = 0
    var content = ""
    override fun toString(): String {
        return "[status = $status, content = $content]"
    }
}