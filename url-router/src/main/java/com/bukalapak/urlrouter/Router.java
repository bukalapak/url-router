package com.bukalapak.urlrouter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mrhabibi on 5/8/17.
 * This is the main class, where the routing happens
 */

public class Router {

    private final static String TAG = "UrlRouter";

    /**
     * Regex used for variable
     * Example: < variable_name:[a-z0-9]+ >
     */
    private final static String VARIABLE_REGEX = "<(\\w+)(:([^>]+))?>";

    private static Router router;

    /**
     * Used for single instance
     *
     * @return Router instance
     */
    public static Router getInstance() {
        if (router == null) {
            router = new Router();
        }
        return router;
    }

    /**
     * Store expressions of map()
     */
    private List<Pair<String, Processor>> mProcessors;

    /**
     * Store expressions of preMap()
     */
    private List<Pair<String, PreProcessor>> mPreProcessors;

    private Preparation mPreparation;
    private String mDefaultVariableRegex;

    public Router() {
        this.mProcessors = new ArrayList<>();
        this.mPreProcessors = new ArrayList<>();
        this.mDefaultVariableRegex = "[^\\/]+";
    }

    /**
     * Setter for default regex used for variable without specific regex
     * Example: < variable_name >
     *
     * @param defaultVariableRegex Default regex for variable
     */
    public void setDefaultVariableRegex(String defaultVariableRegex) {
        this.mDefaultVariableRegex = defaultVariableRegex;
    }

    /**
     * Setter for invocation after launching preMap() and before map()
     *
     * @param preparation Invocation
     */
    public void prepare(Preparation preparation) {
        this.mPreparation = preparation;
    }

    /**
     * Path and query processor for single expression in  single processor
     *
     * @param expression Expression
     * @param processor  Invocation
     */
    public void map(String expression, Processor processor) {
        mProcessors.add(new Pair<>(expression, processor));
    }

    /**
     * Path and query processor for multiple expressions in single processor
     *
     * @param expressions Expressions
     * @param processor   Invocation
     */
    public void map(List<String> expressions, Processor processor) {
        for (String expression : expressions) {
            mProcessors.add(new Pair<>(expression, processor));
        }
    }

    /**
     * Scheme and host processor for single expression in single processor
     *
     * @param expression Expression
     * @param processor  Invocation
     */
    public void preMap(String expression, PreProcessor processor) {
        mPreProcessors.add(new Pair<>(expression, processor));
    }

    /**
     * Schemes and host processor for multiple expressions with prefixes and postfixes in single processor
     *
     * @param prefixes    Prefixes
     * @param expressions Expressions
     * @param postfixes   Postfixes
     * @param processor   Processor
     */
    public void preMap(List<String> prefixes, List<String> expressions, List<String> postfixes, PreProcessor processor) {
        for (String prefix : prefixes) {
            for (String expression : expressions) {
                for (String postfix : postfixes) {
                    mPreProcessors.add(new Pair<>(nullToEmpty(prefix) + expression + nullToEmpty(postfix), processor));
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
    public boolean route(Context context, String url, Bundle args) {
        String urlWithoutQuery = url;

        // remove the query
        if (urlWithoutQuery.contains("?")) {
            urlWithoutQuery = urlWithoutQuery.split("\\?")[0];
        }

        // preMap is optional, if there is no preMap, it will directly go to map expressions
        if (mPreProcessors.size() > 0) {

            // Do sorting first
            Collections.sort(mPreProcessors, new Comparator<Pair<String, PreProcessor>>() {
                @Override
                public int compare(Pair<String, PreProcessor> value1, Pair<String, PreProcessor> value2) {
                    return sort(value1.first, value2.first);
                }
            });

            for (Pair<String, PreProcessor> preProcessorPair : mPreProcessors) {
                CastMap variableMap = new CastMap();
                CastMap queryMap = new CastMap();

                // Check the preMap matching
                boolean match = parse(url, urlWithoutQuery, preProcessorPair.first, variableMap, queryMap);

                if (match) {
                    Result result = new Result(url, variableMap, queryMap, args);
                    String processedUrl = preProcessorPair.second.proceed(context, result);

                    // processedUrl == null means that the preMap won't be continued to map
                    if (processedUrl != null) {
                        return routeUrl(context, url, processedUrl, args);
                    } else {
                        Log.i(TAG, "Routing url " + url + " using " + preProcessorPair.first);
                        return true;
                    }
                }
            }
            Log.e(TAG, "No route for url " + url);
            return false;
        } else {
            return routeUrl(context, url, urlWithoutQuery, args);
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
    private boolean routeUrl(Context context, String url, String processedUrl, Bundle args) {

        // Do sorting first
        Collections.sort(mProcessors, new Comparator<Pair<String, Processor>>() {
            @Override
            public int compare(Pair<String, Processor> value1, Pair<String, Processor> value2) {
                return sort(value1.first, value2.first);
            }
        });

        for (Pair<String, Processor> processorPair : mProcessors) {
            CastMap variableMap = new CastMap();
            CastMap queryMap = new CastMap();

            // Check the map matching
            boolean match = parse(url, processedUrl, processorPair.first, variableMap, queryMap);

            if (match) {
                Result result = new Result(url, variableMap, queryMap, args);

                // Do preparation if it's set
                if (mPreparation != null) {
                    mPreparation.prepare(processorPair.second, context, result);
                } else {
                    processorPair.second.proceed(context, result);
                }
                Log.i(TAG, "Routing url " + url + " using " + processorPair.first);
                return true;
            }
        }
        Log.e(TAG, "No route for url " + url);
        return false;
    }

    /**
     * URL priority sorting
     *
     * @param expression1 url 1
     * @param expression2 url 2
     * @return Comparation
     */
    private int sort(String expression1, String expression2) {

        // Replace all variable into * first
        expression1 = expression1.replaceAll(VARIABLE_REGEX, "*");
        expression2 = expression2.replaceAll(VARIABLE_REGEX, "*");

        // Return which is the longest expression
        return expression2.length() - expression1.length();
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
    private boolean parse(String url, String processedUrl, String expression, CastMap variableMap, CastMap queryMap) {
        List<String> variableNames = new ArrayList<>();

        Matcher variableMatcher = Pattern.compile(VARIABLE_REGEX).matcher(expression);

        // Collect variable names from expression
        while (variableMatcher.find()) {
            variableNames.add(variableMatcher.group(1));
        }

        // Sanitize expression into pure regex form
        String bodyRegex = expression
                .replaceAll("\\.", "\\\\.") // literally dot
                .replaceAll("\\*", ".+") // wildcard
                .replaceAll("<\\w+>", "(" + mDefaultVariableRegex + ")"); // variable with no specific regex

        Matcher variableRegexMatcher = Pattern.compile(VARIABLE_REGEX).matcher(bodyRegex);

        // Sanitize variable in expression with specific regex
        while (variableRegexMatcher.find()) {
            bodyRegex = bodyRegex.replaceFirst(VARIABLE_REGEX, "(" + variableRegexMatcher.group(3) + ")");
        }

        // Remove last slash in expression and URL
        if (bodyRegex.endsWith("/")) {
            bodyRegex = bodyRegex.substring(0, bodyRegex.length() - 1);
        }
        if (processedUrl.endsWith("/")) {
            processedUrl = processedUrl.substring(0, processedUrl.length() - 1);
        }

        Matcher bodyMatcher = Pattern.compile(bodyRegex).matcher(processedUrl);

        // If URL matches expression
        if (bodyMatcher.matches()) {

            // Put parsed variable value into the container
            for (int i = 0; i < variableNames.size(); i++) {
                String value = bodyMatcher.group(i + 1);
                variableMap.put(variableNames.get(i), value);
            }

            Uri uri = Uri.parse(url);

            // Parse the queries
            if (!nullToEmpty(uri.getQuery()).equals("")) {
                try {
                    Set<String> names = uri.getQueryParameterNames(); // It crashes in very long query value

                    // Put into the container
                    for (String name : names) {
                        queryMap.put(name, uri.getQueryParameter(name));
                    }
                } catch (Exception ignored) {
                    // Exception never been catched, internal bug from android (?)
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Null to empty string converter
     *
     * @param str String
     * @return Converted String
     */
    private String nullToEmpty(String str) {
        return str != null ? str : "";
    }
}