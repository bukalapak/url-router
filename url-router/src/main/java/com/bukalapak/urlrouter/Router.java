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
 */

public class Router {

    private final static String TAG = "UrlRouter";

    private final static String VARIABLE_REGEX = "<(\\w+)(:([^>]+))?>";

    private static Router router;

    public static Router getInstance() {
        if (router == null) {
            router = new Router();
        }
        return router;
    }

    private List<Pair<String, Processor>> mProcessors;
    private List<Pair<String, PreProcessor>> mPreProcessors;
    private Preparation mPreparation;
    private String mDefaultVariableRegex;

    public Router() {
        this.mProcessors = new ArrayList<>();
        this.mPreProcessors = new ArrayList<>();
        this.mDefaultVariableRegex = "[^\\/]+";
    }

    public void setDefaultVariableRegex(String defaultVariableRegex) {
        this.mDefaultVariableRegex = defaultVariableRegex;
    }

    public void prepare(Preparation preparation) {
        this.mPreparation = preparation;
    }

    public void map(String expression, Processor processor) {
        mProcessors.add(new Pair<>(expression, processor));
    }

    public void map(List<String> expressions, Processor processor) {
        for (String expression : expressions) {
            mProcessors.add(new Pair<>(expression, processor));
        }
    }

    public void preMap(String expression, PreProcessor processor) {
        mPreProcessors.add(new Pair<>(expression, processor));
    }

    public void preMap(List<String> prefixes, List<String> expressions, List<String> postfixes, PreProcessor processor) {
        for (String prefix : prefixes) {
            for (String expression : expressions) {
                for (String postfix : postfixes) {
                    mPreProcessors.add(new Pair<>(nullToEmpty(prefix) + expression + nullToEmpty(postfix), processor));
                }
            }
        }
    }

    public boolean route(Context context, String url, Bundle args) {
        String urlWithoutQuery = url;
        if (urlWithoutQuery.contains("?")) {
            urlWithoutQuery = urlWithoutQuery.split("\\?")[0];
        }

        if (mPreProcessors.size() > 0) {
            Collections.sort(mPreProcessors, new Comparator<Pair<String, PreProcessor>>() {
                @Override
                public int compare(Pair<String, PreProcessor> value1, Pair<String, PreProcessor> value2) {
                    return sort(value1.first, value2.first);
                }
            });
            for (Pair<String, PreProcessor> preProcessorPair : mPreProcessors) {
                CastMap variableMap = new CastMap();
                CastMap queryMap = new CastMap();

                boolean match = parse(url, urlWithoutQuery, preProcessorPair.first, variableMap, queryMap);

                if (match) {
                    Result result = new Result(url, variableMap, queryMap, args);
                    String processedUrl = preProcessorPair.second.proceed(context, result);
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

    private boolean routeUrl(Context context, String url, String processedUrl, Bundle args) {
        Collections.sort(mProcessors, new Comparator<Pair<String, Processor>>() {
            @Override
            public int compare(Pair<String, Processor> value1, Pair<String, Processor> value2) {
                return sort(value1.first, value2.first);
            }
        });
        for (Pair<String, Processor> processorPair : mProcessors) {
            CastMap variableMap = new CastMap();
            CastMap queryMap = new CastMap();
            boolean match = parse(url, processedUrl, processorPair.first, variableMap, queryMap);

            if (match) {
                Result result = new Result(url, variableMap, queryMap, args);
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

    private int sort(String expression1, String expression2) {
        expression1 = expression1.replaceAll(VARIABLE_REGEX, "*");
        expression2 = expression2.replaceAll(VARIABLE_REGEX, "*");
        return expression2.length() - expression1.length();
    }

    private boolean parse(String url, String processedUrl, String expression, CastMap variableMap, CastMap queryMap) {
        List<String> variableNames = new ArrayList<>();

        Matcher variableMatcher = Pattern.compile(VARIABLE_REGEX).matcher(expression);

        while (variableMatcher.find()) {
            variableNames.add(variableMatcher.group(1));
        }

        String bodyRegex = expression
                .replaceAll("\\.", "\\\\.") // literally dot
                .replaceAll("\\*", ".+") // wildcard
                .replaceAll("<\\w+>", "(" + mDefaultVariableRegex + ")"); // variable

        Matcher variableRegexMatcher = Pattern.compile(VARIABLE_REGEX).matcher(bodyRegex);

        while (variableRegexMatcher.find()) {
            bodyRegex = bodyRegex.replaceFirst(VARIABLE_REGEX, "(" + variableRegexMatcher.group(3) + ")");
        }

        if (bodyRegex.endsWith("/")) {
            bodyRegex = bodyRegex.substring(0, bodyRegex.length() - 1);
        }

        if (processedUrl.endsWith("/")) {
            processedUrl = processedUrl.substring(0, processedUrl.length() - 1);
        }

        Matcher bodyMatcher = Pattern.compile(bodyRegex).matcher(processedUrl);

        if (bodyMatcher.matches()) {
            for (int i = 0; i < variableNames.size(); i++) {
                String value = bodyMatcher.group(i + 1);
                variableMap.put(variableNames.get(i), value);
            }
            Uri uri = Uri.parse(url);
            if (!nullToEmpty(uri.getQuery()).equals("")) {
                try {
                    Set<String> names = uri.getQueryParameterNames(); // It crashes in very long query value
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

    private String nullToEmpty(String str) {
        return str != null ? str : "";
    }
}