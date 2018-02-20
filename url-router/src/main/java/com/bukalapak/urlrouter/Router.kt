package com.bukalapak.urlrouter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import java.util.*
import java.util.regex.Pattern

typealias PreProcessor = (Result) -> String?
typealias Processor = (Result) -> Unit
typealias Preparation = (Processor, Result) -> Unit

/**
 * Created by mrhabibi on 5/8/17.
 * This is the main class, where the routing happens
 */

class Router {

    /**
     * Store expressions of map()
     */
    private val processors = mutableListOf<Pair<String, Processor>>()

    /**
     * Store expressions of preMap()
     */
    private val preProcessors = mutableListOf<Pair<String, PreProcessor>>()

    /**
     * Setter for invocation after launching preMap() and before map()
     *
     * @param preparation Invocation
     */
    var preparation: Preparation? = null

    /**
     * Setter for default regex used for variable without specific regex
     * Example: ❮variable_name❯
     *
     * @param defaultVariableRegex Default regex for variable
     */
    var defaultVariableRegex = "[^\\/]+"

    /**
     * Path and query processor for single expression in  single processor
     *
     * @param expression Expression
     * @param processor  Invocation
     */
    fun map(expression: String, processor: Processor) {
        processors.add(Pair(expression, processor))
    }

    /**
     * Path and query processor for multiple expressions in single processor
     *
     * @param expressions Expressions
     * @param processor   Invocation
     */
    fun map(expressions: List<String>, processor: Processor) {
        processors.addAll(expressions.map { Pair(it, processor) })
    }

    /**
     * Scheme and host processor for single expression in single processor
     *
     * @param expression Expression
     * @param preProcessor  Invocation
     */
    fun preMap(expression: String, preProcessor: PreProcessor) {
        preProcessors.add(Pair(expression, preProcessor))
    }

    /**
     * Schemes and host processor for multiple expressions with prefixes and postfixes in single processor
     *
     * @param prefixes    Prefixes
     * @param expressions Expressions
     * @param postfixes   Postfixes
     * @param processor   Processor
     */
    fun preMap(prefixes: List<String>, expressions: List<String>, postfixes: List<String>, processor: PreProcessor) {
        prefixes.forEach { prefix ->
            expressions.forEach { expression ->
                postfixes.forEach { postfix ->
                    preProcessors.add(Pair(nullToEmpty(prefix) + expression + nullToEmpty(postfix), processor))
                }
            }
        }
    }

    /**
     * Main method to do the routing
     *
     * @param context Passed context
     * @param url     URL
     * @param args    Optional arguments
     * @return Has routing path
     */
    fun route(context: Context, url: String, args: Bundle?): Boolean {
        var urlWithoutQuery = url

        // remove the query
        if (urlWithoutQuery.contains("?")) {
            urlWithoutQuery = urlWithoutQuery.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }

        // preMap is optional, if there is no preMap, it will directly go to map expressions
        if (preProcessors.size > 0) {

            // Do sorting first
            Collections.sort<Pair<String, PreProcessor>>(preProcessors) { value1, value2 -> sort(value1.first, value2.first) }

            for (preProcessorPair in preProcessors) {
                val variableMap = CastMap()
                val queryMap = CastMap()

                // Check the preMap matching
                val match = parse(url, urlWithoutQuery, preProcessorPair.first, variableMap, queryMap)

                if (match) {
                    val result = Result(context, url, variableMap, queryMap, args)
                    val processedUrl = preProcessorPair.second.invoke(result)

                    // processedUrl == null means that the preMap won't be continued to map
                    if (processedUrl != null) {
                        return routeUrl(context, url, processedUrl, args)
                    } else {
                        Log.i(TAG, "Routing url " + url + " using " + preProcessorPair.first)
                        return true
                    }
                }
            }
            Log.e(TAG, "No route for url " + url)
            return false
        } else {
            return routeUrl(context, url, urlWithoutQuery, args)
        }
    }

    /**
     * Private method to do map execution after preMap matching
     *
     * @param context      Passed context
     * @param url          URL
     * @param processedUrl Processed sub URL from preMap
     * @param args         Optional arguments
     * @return Has routing path
     */
    private fun routeUrl(context: Context, url: String, processedUrl: String, args: Bundle?): Boolean {

        // Do sorting first
        Collections.sort<Pair<String, Processor>>(processors) { value1, value2 -> sort(value1.first, value2.first) }

        for (processorPair in processors) {
            val variableMap = CastMap()
            val queryMap = CastMap()

            // Check the map matching
            val match = parse(url, processedUrl, processorPair.first, variableMap, queryMap)

            if (match) {
                val result = Result(context, url, variableMap, queryMap, args)

                // Do preparation if it's set
                if (preparation != null) {
                    preparation!!.invoke(processorPair.second, result)
                } else {
                    processorPair.second.invoke(result)
                }
                Log.i(TAG, "Routing url " + url + " using " + processorPair.first)
                return true
            }
        }
        Log.e(TAG, "No route for url " + url)
        return false
    }

    /**
     * URL priority sorting
     *
     * @param expression1 url 1
     * @param expression2 url 2
     * @return Comparation
     */
    private fun sort(expression1: String, expression2: String): Int {

        // Replace all variable into * first
        val expr1 = expression1.replace(VARIABLE_REGEX.toRegex(), "*")
        val expr2 = expression2.replace(VARIABLE_REGEX.toRegex(), "*")

        // Return which is the longest expression
        return expr2.length - expr1.length
    }

    /**
     * Parsing the expression
     *
     * @param url          Full URL
     * @param processedUrl Processed sub URL
     * @param expression   Expression
     * @param variableMap  Parsed variables
     * @param queryMap     Parsed queries
     * @return Is matched
     */
    private fun parse(url: String, processedUrl: String, expression: String, variableMap: CastMap, queryMap: CastMap): Boolean {
        var processedUrl = processedUrl
        val variableNames = ArrayList<String>()

        val variableMatcher = Pattern.compile(VARIABLE_REGEX).matcher(expression)

        // Collect variable names from expression
        while (variableMatcher.find()) {
            variableNames.add(variableMatcher.group(1))
        }

        // Sanitize expression into pure regex form
        var bodyRegex = expression
                .replace("\\.".toRegex(), "\\\\.") // literally dot
                .replace("\\*".toRegex(), ".+") // wildcard
                .replace("<\\w+>".toRegex(), "($defaultVariableRegex)") // variable with no specific regex

        val variableRegexMatcher = Pattern.compile(VARIABLE_REGEX).matcher(bodyRegex)

        // Sanitize variable in expression with specific regex
        while (variableRegexMatcher.find()) {
            bodyRegex = bodyRegex.replaceFirst(VARIABLE_REGEX.toRegex(), "(" + variableRegexMatcher.group(3) + ")")
        }

        // Remove last slash in expression and URL
        if (bodyRegex.endsWith("/")) {
            bodyRegex = bodyRegex.substring(0, bodyRegex.length - 1)
        }
        if (processedUrl.endsWith("/")) {
            processedUrl = processedUrl.substring(0, processedUrl.length - 1)
        }

        val bodyMatcher = Pattern.compile(bodyRegex).matcher(processedUrl)

        // If URL matches expression
        if (bodyMatcher.matches()) {

            // Put parsed variable value into the container
            for (i in variableNames.indices) {
                val value = bodyMatcher.group(i + 1)
                variableMap[variableNames[i]] = value
            }

            val uri = Uri.parse(url)

            // Parse the queries
            if (nullToEmpty(uri.query) != "") {
                try {
                    val names = uri.queryParameterNames // It crashes in very long query value

                    // Put into the container
                    for (name in names) {
                        queryMap[name] = uri.getQueryParameter(name)
                    }
                } catch (ignored: Exception) {
                    // Exception never been catched, internal bug from android (?)
                }

            }
            return true
        }
        return false
    }

    /**
     * Null to empty string converter
     *
     * @param str String
     * @return Converted String
     */
    private fun nullToEmpty(str: String?): String = str ?: ""

    companion object {

        private const val TAG = "UrlRouter"

        /**
         * Regex used for variable
         * Example: ❮variable_name:[a-z0-9]+❯
         */
        private const val VARIABLE_REGEX = "<(\\w+)(:([^>]+))?>"

        private var router: Router? = null

        /**
         * Used for single instance
         *
         * @return Router instance
         */
        val instance: Router
            get() {
                val r = router ?: Router()
                router = r
                return r
            }
    }
}