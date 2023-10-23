package com.ammar.wallflow.extensions

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Pattern
import org.jsoup.parser.Parser

val Any.TAG: String
    get() {
        return if (!javaClass.isAnonymousClass) {
            val name = javaClass.simpleName
            if (name.length <= 23) name else name.substring(0, 23) // first 23 chars
        } else {
            val name = javaClass.name
            if (name.length <= 23) {
                name
            } else {
                name.substring(
                    name.length - 23,
                    name.length,
                ) // last 23 chars
            }
        }
    }

fun String.trimAll() = this.trim { it <= ' ' }

fun String.urlEncoded(): String = URLEncoder.encode(this, "UTF-8")

fun String.urlDecoded(): String = URLDecoder.decode(this, "UTF-8")

fun String.quoteIfSpaced() = if (this.contains(" ")) "\"$this\"" else this

fun String.getFileNameFromUrl() = substring(
    lastIndexOf('/') + 1,
    length,
)

fun String.capitalise() = this
    .split(" ")
    .joinToString(" ") {
        it.replaceFirstChar { c -> c.uppercaseChar() }
    }

fun String.fromQueryString() = this
    .split("&")
    .map { it.split(Pattern.compile("="), 2) }
    .associate {
        Pair(
            it[0].urlDecoded(),
            if (it.size > 1) it[1].urlDecoded() else null,
        )
    }

fun String.htmlUnescaped(): String = Parser.unescapeEntities(this, false)
