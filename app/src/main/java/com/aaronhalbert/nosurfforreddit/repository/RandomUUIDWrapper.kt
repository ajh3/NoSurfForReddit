package com.aaronhalbert.nosurfforreddit.repository

import java.util.*

/* Wrap this static call to allow mocking during tests. */
class RandomUUIDWrapper {
    fun randomUUID(): String {
        return UUID.randomUUID().toString()
    }
}
