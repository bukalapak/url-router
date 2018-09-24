package com.bukalapak.urlrouter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import java.util.*
import java.util.regex.Pattern

typealias PreProcessor = (Result) -> Unit
typealias Processor = (Result) -> Unit
typealias GlobalInterceptor = (Interceptor, Processor, Result) -> Unit
typealias Interceptor = (Processor, Result) -> Unit

/**
 * Created by mrhabibi on 5/8/17.
 * This is the main class, where the routing happens
 */

class Router {

    /**
     * Store expressions of map()
     */
    private var processors = listOf<Expression<Processor>>()

    /**
     * Store expressions of preMap()
     */
    private var preProcessors = listOf<Expression<PreProcessor>>()

    /**
     * Setter for invocation after launching preMap() and before map()
     *
     * @param preparation Invocation
     */
    var globalInterceptor: GlobalInterceptor = DEFAULT_GLOBAL_INTERCEPTOR

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
     * @param parentExpression  Scheme and host expression
     */
    private fun pathMap(expression: String, processor: Processor, parentExpression: String? = null) {
        assertExpression(expression)
        processors = processors.plus(Expression(expression, processor, 1, parentExpression))
    }

    /**
     * Path and query processor for multiple expressions in single processor
     *
     * @param expressions Expressions
     * @param processor   Invocation
     * @param parentExpression  Scheme and host expression
     */
    private fun pathMap(expressions: List<String>, processor: Processor, parentExpression: String? = null) {
        assertExpression(expressions)
        processors = processors.plus(expressions.map { Expression(it, processor, expressions.size, parentExpression) })
    }

    /**
     *
     */
    fun map(routeMapBuilder: RouterMap.Builder) {
        val routerMap = routeMapBuilder.build()

        assertExpression(routerMap.expressions)
        val count = if (routerMap.prefixes.isEmpty()) 1 else routerMap.prefixes.size *
                routerMap.expressions.size *
                if (routerMap.postfixes.isEmpty()) 1 else routerMap.postfixes.size

        val pattern = mutableListOf<String>()
        val prefixes = if (!routerMap.prefixes.isEmpty()) routerMap.prefixes else listOf("")
        val postfixes = if (!routerMap.postfixes.isEmpty()) routerMap.postfixes else listOf("")
        prefixes.forEach { prefix ->
            routerMap.expressions.forEach { expression ->
                postfixes.forEach { postfix ->
                    pattern.add(prefix.nullToEmpty() + expression + postfix.nullToEmpty())
                    preProcessors = preProcessors.plus(Expression(
                            prefix.nullToEmpty() +
                                    expression +
                                    postfix.nullToEmpty(),
                            routerMap.preProcessor, count))
                }
            }
        }
        pattern.forEach { pattern ->
            routerMap.path.forEach {
                pathMap(it.expression, it.processor, pattern)
            }
        }
    }

    /**
     *
     */
    fun map(routeMapBuilder: RouterMap.Builder.() -> Unit) {
        val builder = RouterMap.Builder()
        builder.routeMapBuilder()
        val routerMap = builder.build()

        assertExpression(routerMap.expressions)
        val count = if (routerMap.prefixes.isEmpty()) 1 else routerMap.prefixes.size *
                routerMap.expressions.size *
                if (routerMap.postfixes.isEmpty()) 1 else routerMap.postfixes.size

        val pattern = mutableListOf<String>()
        val prefixes = if (!routerMap.prefixes.isEmpty()) routerMap.prefixes else listOf("")
        val postfixes = if (!routerMap.postfixes.isEmpty()) routerMap.postfixes else listOf("")
        prefixes.forEach { prefix ->
            routerMap.expressions.forEach { expression ->
                postfixes.forEach { postfix ->
                    pattern.add(prefix.nullToEmpty() + expression + postfix.nullToEmpty())
                    preProcessors = preProcessors.plus(Expression(
                            prefix.nullToEmpty() +
                                    expression +
                                    postfix.nullToEmpty(),
                            routerMap.preProcessor, count))
                }
            }
        }
        pattern.forEach { pattern ->
            routerMap.path.forEach {
                pathMap(it.expression, it.processor, pattern)
            }
        }
    }

    private fun assertExpression(expression: String) {
        assertExpression(listOf(expression))
    }

    private fun assertExpression(expressions: List<String>) {
        if (expressions.isEmpty()) throw IllegalArgumentException("List of expressions is empty")
        if (expressions.any { it.isBlank() }) throw IllegalArgumentException("One of expression is blank")
    }

    /**
     * Main method to do the routing
     *
     * @param context Passed context
     * @param url     URL
     * @param args    Optional arguments
     * @return Has routing path
     */
    fun route(context: Context,
              url: String,
              interceptor: Interceptor? = null,
              args: Bundle? = null): Boolean {

        // remove the query
        val urlPathOnly = url.split('#').first().split('?').first()

        // preMap is optional, if there is no preMap, it will directly go to map expressions
        if (preProcessors.isNotEmpty()) {

            // Do sorting first
            preProcessors = preProcessors.sortedWith(generateComparator())

            preProcessors.forEach {
                val rawResult = RawResult()

                // Check the preMap matching
                val match = parse(url, urlPathOnly, it.pattern, rawResult)

                if (match) {
                    val result = rawResult.cook(context, url, args)
                    it.processor.invoke(result)

                    val processedUrl = Uri.parse(getUrlWithScheme(url)).path

                    // processedUrl == null means that the preMap won't be continued to map
                    return if (processedUrl != null) {
                        routeUrl(context, url, processedUrl, interceptor, args, it.pattern)
                    } else {
                        Log.i(TAG, "Routing url " + url + " using " + it.pattern)
                        true
                    }
                }
            }
            Log.e(TAG, "No route for url " + url)
            return false
        } else {
            return routeUrl(context, url, urlPathOnly, interceptor, args)
        }
    }

    /**
     * Private method to do map execution after preMap matching
     *
     * @param context      Passed context
     * @param url          URL
     * @param processedUrl Processed sub URL from preMap
     * @param args         Optional arguments
     * @param parentPattern Scheme and host Expression
     * @return Has routing path
     */
    private fun routeUrl(context: Context,
                         url: String,
                         processedUrl: String,
                         interceptor: Interceptor?,
                         args: Bundle?,
                         parentPattern: String? = null): Boolean {

        var processorFiltered = processors.filter { it.parentPattern.equals(parentPattern) }

        // Do sorting first
        processorFiltered = processorFiltered.sortedWith(generateComparator())


        processorFiltered.forEach {
            val rawResult = RawResult()

            // Check the map matching
            val match = parse(url, processedUrl, it.pattern, rawResult)

            if (match) {
                val result = rawResult.cook(context, url, args)

                // Router won't execute routing if it's true
                val checkOnly = args?.getBoolean(ARG_CHECK_ONLY) == true

                if (!checkOnly) {

                    // Do global interception if it's set
                    globalInterceptor.invoke(interceptor
                            ?: DEFAULT_INTERCEPTOR, it.processor, result)
                }

                Log.i(TAG, "Routing url " + url + " using " + it.pattern)
                return true
            }
        }
        Log.e(TAG, "No route for url " + url)
        return false
    }

    /**
     * generate URL priority comparator
     */
    private fun <P> generateComparator(): Comparator<Expression<P>> = Comparator({ expression1, expression2 ->

        // Replace all variable into * first
        val pattern1 = expression1.pattern.replace(VARIABLE_REGEX.toRegex(), "*")
        val pattern2 = expression2.pattern.replace(VARIABLE_REGEX.toRegex(), "*")

        // Pattern length priority #1
        val patternLength = pattern2.length - pattern1.length

        // Member count priority #2
        val memberCount = expression1.memberCount - expression2.memberCount

        if (patternLength == 0) memberCount else patternLength
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
                      rawResult: RawResult): Boolean {

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
                rawResult.variables.put(name, bodyMatcher.group(index + 1))
            }

            val uri = Uri.parse(getUrlWithScheme(url))

            // Parse the fragment
            rawResult.fragment = uri.fragment

            // Parse the queries
            if (!uri.query.isNullOrEmpty()) {
                try {
                    val names = uri.queryParameterNames // It crashes in very long query value

                    // Put into the container
                    names.forEach {
                        rawResult.queries[it] = uri.getQueryParameters(it)
                    }
                } catch (ignored: Exception) {
                    // Exception never been catched, internal bug from android (?)
                }

            }
            true
        } else false
    }

    /**
     * add scheme https://
     */
    private fun getUrlWithScheme(url: String): String {
        return if (!Pattern.matches("\\w+://.+", url)) {
            "https://$url"
        } else {
            url
        }
    }

    /**
     * Null to empty string converter
     */
    private fun String?.nullToEmpty(): String = this ?: ""

    /**
     * Router resetter
     *
     * @param onlyRoutes Clear only map and premap if true, otherwise reset everything
     */
    fun reset(onlyRoutes: Boolean = false) {
        preProcessors = listOf()
        processors = listOf()
        if (!onlyRoutes) {
            globalInterceptor = DEFAULT_GLOBAL_INTERCEPTOR
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
        private val DEFAULT_GLOBAL_INTERCEPTOR: GlobalInterceptor = { interceptor, processor, result ->
            interceptor.invoke(processor, result)
        }
        private val DEFAULT_INTERCEPTOR: Interceptor = { processor, result -> processor.invoke(result) }

        const val ARG_CHECK_ONLY = "arg_check_only"

        private var router: Router? = null

        /**
         * Used for single instance
         *
         * @return Router instance
         */
        val INSTANCE: Router
            get() = router ?: let {
                val temp = Router()
                router = temp
                temp
            }
    }
}