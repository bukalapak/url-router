package com.bukalapak.urlrouter

import java.util.*

/**
 * Created by mrhabibi on 5/26/17.
 * Extension class of HashMap as container class for parsed variables and queries
 */

class CastMap : HashMap<String, String>() {

    fun getLong(key: String): Long = if (containsKey(key)) {
        try {
            get(key)?.toLong() ?: 0L
        } catch (ex: Exception) {
            0L
        }
    } else 0L

    fun getInt(key: String): Int = if (containsKey(key)) {
        try {
            get(key)?.toInt() ?: 0
        } catch (ex: Exception) {
            0
        }
    } else 0

    fun getBool(key: String): Boolean = if (containsKey(key)) {
        get(key).equals("true", ignoreCase = true) || get(key).equals("1", ignoreCase = true)
    } else false

}
