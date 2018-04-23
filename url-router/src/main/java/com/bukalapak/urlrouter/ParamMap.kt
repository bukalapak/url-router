package com.bukalapak.urlrouter

import java.util.*

/**
 * Created by mrhabibi on 5/26/17.
 * Extension class of HashMap as container class for parsed variables and queries
 */

open class ParamMap : HashMap<String, List<String>>() {

    @Deprecated("Don't use default's for preventing null value")
    override fun get(key: String): List<String>? = super.get(key)

    fun put(key: String, value: String): List<String>? {
        return super.put(key, Arrays.asList(value))
    }

    fun getString(key: String): String = if (containsKey(key)) {
        super.get(key)?.get(0) ?: ""
    } else ""

    fun getLong(key: String): Long = if (containsKey(key)) {
        try {
            super.get(key)?.get(0)?.toLong() ?: 0L
        } catch (ex: Exception) {
            0L
        }
    } else 0L

    fun getInt(key: String): Int = if (containsKey(key)) {
        try {
            super.get(key)?.get(0)?.toInt() ?: 0
        } catch (ex: Exception) {
            0
        }
    } else 0

    fun getFloat(key: String): Float = if (containsKey(key)) {
        try {
            super.get(key)?.get(0)?.toFloat() ?: 0.0F
        } catch (ex: Exception) {
            0.0F
        }
    } else 0.0F

    fun getDouble(key: String): Double = if (containsKey(key)) {
        try {
            super.get(key)?.get(0)?.toDouble() ?: 0.0
        } catch (ex: Exception) {
            0.0
        }
    } else 0.0

    fun getBoolean(key: String): Boolean = if (containsKey(key)) {
        val value = super.get(key)?.get(0)
        !value.equals("false", true) || !value.equals("0", true)
    } else false
}

class ParamsMap : ParamMap() {

    fun getStringList(key: String): List<String> = if (containsKey(key)) {
        super.get(key) ?: emptyList()
    } else emptyList()

    fun getLongList(key: String): List<Long> = if (containsKey(key)) {
        try {
            super.get(key)?.map { it.toLong() } ?: emptyList()
        } catch (ex: Exception) {
            emptyList<Long>()
        }
    } else emptyList()

    fun getIntList(key: String): List<Int> = if (containsKey(key)) {
        try {
            super.get(key)?.map { it.toInt() } ?: emptyList()
        } catch (ex: Exception) {
            emptyList<Int>()
        }
    } else emptyList()

    fun getFloatList(key: String): List<Float> = if (containsKey(key)) {
        try {
            super.get(key)?.map { it.toFloat() } ?: emptyList()
        } catch (ex: Exception) {
            emptyList<Float>()
        }
    } else emptyList()

    fun getDoubleList(key: String): List<Double> = if (containsKey(key)) {
        try {
            super.get(key)?.map { it.toDouble() } ?: emptyList()
        } catch (ex: Exception) {
            emptyList<Double>()
        }
    } else emptyList()

    fun getBooleanList(key: String): List<Boolean> = if (containsKey(key)) {
        super.get(key)?.map { !it.equals("false", true) || !it.equals("0", true) } ?: emptyList()
    } else emptyList()
}