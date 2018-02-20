package com.bukalapak.urlrouter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
    private val processors = mutableListOf<Expression<Processor>>()

    /**
     * Store expressions of preMap()
     */
    private val preProcessors = mutableListOf<Expression<PreProcessor>>()

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
    var defaultVariableRegex = DEFAULT_VARIABLE_REGEX

    /**
     * Path and query processor for single expression in  single processor
     *
     * @param expression Expression
     * @param processor  Invocation
     */
    fun map(expression: String, processor: Processor) {
        processors.add(Expression(expression, processor))
    }

    /**
     * Path and query processor for multiple expressions in single processor
     *
     * @param expressions Expressions
     * @param processor   Invocation
     */
    fun map(expressions: List<String>, processor: Processor) {
        processors.addAll(expressions.map { Expression(it, processor) })
    }

    /**
     * Scheme and host processor for single expression in single processor
     *
     * @param expression Expression
     * @param preProcessor  Invocation
     */
    fun preMap(expression: String, preProcessor: PreProcessor) {
        preProcessors.add(Expression(expression, preProcessor))
    }

    /**
     * Schemes and host processor for multiple expressions with prefixes and postfixes in single processor
     *
     * @param prefixes    Prefixes
     * @param expressions Expressions
     * @param postfixes   Postfixes
     * @param processor   Processor
     */
    fun preMap(prefixes: List<String> = emptyList(),
               expressions: List<String>,
               postfixes: List<String> = emptyList(),
               processor: PreProcessor) {

        prefixes.forEach { prefix ->
            expressions.forEach { expression ->
                postfixes.forEach { postfix ->
                    preProcessors.add(Expression(prefix.nullToEmpty() + expression + postfix.nullToEmpty(), processor))
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

        // remove the query
        val urlWithoutQuery = url.split('?')[0]

        // preMap is optional, if there is no preMap, it will directly go to map expressions
        if (preProcessors.isNotEmpty()) {

            // Do sorting first
            preProcessors.sortedWith(generateComparator())

            preProcessors.forEach {
                val variables = CastMap()
                val queries = CastMap()

                // Check the preMap matching
                val match = parse(url, urlWithoutQuery, it.pattern, variables, queries)

                if (match) {
                    val result = Result(context, url, variables, queries, args)
                    val processedUrl = it.processor.invoke(result)

                    // processedUrl == null means that the preMap won't be continued to map
                    return if (processedUrl != null) routeUrl(context, url, processedUrl, args)
                    else {
                        Log.i(TAG, "Routing url " + url + " using " + it.pattern)
                        true
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
        processors.sortWith(generateComparator())

        processors.forEach {
            val variables = CastMap()
            val queries = CastMap()

            // Check the map matching
            val match = parse(url, processedUrl, it.pattern, variables, queries)

            if (match) {
                val result = Result(context, url, variables, queries, args)

                // Do preparation if it's set
                preparation?.invoke(it.processor, result) ?: it.processor.invoke(result)

                Log.i(TAG, "Routing url " + url + " using " + it.pattern)
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
    private fun <P> generateComparator(): Comparator<Expression<P>> = Comparator({ expression1, expression2 ->

        // Replace all variable into * first
        val expr1 = expression1.pattern.replace(VARIABLE_REGEX.toRegex(), "*")
        val expr2 = expression2.pattern.replace(VARIABLE_REGEX.toRegex(), "*")

        // Return which is the longest expression
        expr2.length - expr1.length
    })

    /**
     * Parsing the expression
     *
     * @param url          Full URL
     * @param processedUrl Processed sub URL
     * @param expression   Expression
     * @param variables  Parsed variables
     * @param queries     Parsed queries
     * @return Is matched
     */
    private fun parse(url: String,
                      processedUrl: String,
                      expression: String,
                      variables: CastMap,
                      queries: CastMap): Boolean {

        val varNames = mutableListOf<String>()
        val varMatcher = Pattern.compile(VARIABLE_REGEX).matcher(expression)

        // Collect variable names from expression
        while (varMatcher.find()) {
            varNames.add(varMatcher.group(1))
        }

        // Sanitize expression into pure regex form
        var bodyRegex = expression
                .replace("\\.".toRegex(), "\\\\.") // literally dot
                .replace("\\*".toRegex(), ".+") // wildcard
                .replace("<\\w+>".toRegex(), "($defaultVariableRegex)") // variable with no specific regex

        val varRegexMatcher = Pattern.compile(VARIABLE_REGEX).matcher(bodyRegex)

        // Sanitize variable in expression with specific regex
        while (varRegexMatcher.find()) {
            bodyRegex = bodyRegex.replaceFirst(VARIABLE_REGEX.toRegex(), "(${varRegexMatcher.group(3)})")
        }

        // Remove last slash in expression and URL
        bodyRegex = bodyRegex.removeSuffix("/")
        val cleanUrl = processedUrl.removeSuffix("/")

        val bodyMatcher = Pattern.compile(bodyRegex).matcher(cleanUrl)

        // If URL matches expression
        return if (bodyMatcher.matches()) {

            // Put parsed variable value into the container
            varNames.forEachIndexed { index, name ->
                variables[name] = bodyMatcher.group(index + 1)
            }

            val uri = Uri.parse(url)

            // Parse the queries
            if (!uri.query.isNullOrEmpty()) {
                try {
                    val names = uri.queryParameterNames // It crashes in very long query value

                    // Put into the container
                    names.forEach {
                        queries[it] = uri.getQueryParameter(it)
                    }
                } catch (ignored: Exception) {
                    // Exception never been catched, internal bug from android (?)
                }

            }
            true
        } else false
    }

    /**
     * Null to empty string converter
     *
     * @param str String
     * @return Converted String
     */
    private fun String.nullToEmpty(): String = this ?: ""

    fun reset(onlyRoutes: Boolean = false) {
        preProcessors.clear()
        processors.clear()
        if (!onlyRoutes) {
            preparation = null
            defaultVariableRegex = DEFAULT_VARIABLE_REGEX
        }

    }

    companion object {

        private const val TAG = "UrlRouter"

        /**
         * Regex used for variable
         * Example: ❮variable_name:[a-z0-9]+❯
         */
        private const val VARIABLE_REGEX = "<(\\w+)(:([^>]+))?>"
        private const val DEFAULT_VARIABLE_REGEX = "[^\\/]+"

        private var router: Router? = null

        /**
         * Used for single instance
         *
         * @return Router instance
         */
        val INSTANCE: Router
            get() {
                val r = router ?: Router()
                router = r
                return r
            }
    }
}