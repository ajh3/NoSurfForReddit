package com.aaronhalbert.nosurfforreddit.repository

import java.util.*

class RandomUUIDWrapper {
    fun randomUUID(): String {
        return UUID.randomUUID().toString()
    }
}
