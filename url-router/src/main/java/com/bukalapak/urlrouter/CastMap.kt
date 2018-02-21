package com.bukalapak.urlrouter

import java.util.*

/**
 * Created by mrhabibi on 5/26/17.
 * Extension class of HashMap as container class for parsed variables and queries
 */

class CastMap : HashMap<String, String>() {

    @Deprecated("Don't use default's for preventing null value")
    override fun get(key: String): String? = super.get(key)

    fun getString(key: String): String = if (containsKey(key)) {
        super.get(key) ?: ""
    } else ""

    fun getLong(key: String): Long = if (containsKey(key)) {
        try {
            super.get(key)?.toLong() ?: 0L
        } catch (ex: Exception) {
            0L
        }
    } else 0L

    fun getInt(key: String): Int = if (containsKey(key)) {
        try {
            super.get(key)?.toInt() ?: 0
        } catch (ex: Exception) {
            0
        }
    } else 0

    fun getFloat(key: String): Float = if (containsKey(key)) {
        try {
            super.get(key)?.toFloat() ?: 0.0F
        } catch (ex: Exception) {
            0.0F
        }
    } else 0.0F

    fun getDouble(key: String): Double = if (containsKey(key)) {
        try {
            super.get(key)?.toDouble() ?: 0.0
        } catch (ex: Exception) {
            0.0
        }
    } else 0.0

    fun getBoolean(key: String): Boolean = if (containsKey(key)) {
        super.get(key).equals("true", ignoreCase = true) || super.get(key).equals("1", ignoreCase = true)
    } else false

}
