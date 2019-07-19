package com.bukalapak.neovalidator;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeeplinkValidator {
    public static boolean validateDeeplinkConfig(@NotNull String validatorName, @NotNull String configJson) {
        switch (validatorName) {
            case "dynamic-deeplink":
                return checkV1(configJson, null);
            case "dynamic-deeplink-v2":
                return checkV2(configJson, null);
            case "dynamic-deeplink-v3":
                return checkV3(configJson, null);
            default:
                return false;
        }
    }

    public static boolean validateDeeplinkConfig(@NotNull String validatorName, @NotNull String configJson, OnDeeplinkCheckListener listener) {
        switch (validatorName) {
            case "dynamic-deeplink":
                return checkV1(configJson, listener);
            case "dynamic-deeplink-v2":
                return checkV2(configJson, listener);
            case "dynamic-deeplink-v3":
                return checkV3(configJson, listener);
            default:
                invokeInvalidIfListenerNotNull(listener, new IllegalArgumentException("validator name not found"));
                return false;
        }
    }

    private static boolean checkV3(String configJson, OnDeeplinkCheckListener listener) {
        try {
            DynamicDeeplink.DynamicDeeplinkV3 result = new Gson().fromJson(configJson, DynamicDeeplink.DynamicDeeplinkV3.class);
            if (result == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: dynamic-deeplink-v3"));
                return false;
            }
            if (result.deeplinks == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: deeplinks:{"));
                return false;
            }

            List<String> keys = collectKey(result.deeplinks);
            if (keys.size() < 1 || keys.size() == 1 && keys.get(0).equals("deeplinks")) {
                invokeInvalidIfListenerNotNull(listener, new IllegalArgumentException("IllegalArgumentException: deeplinks:{"));
                return false;
            }
            for (String key : keys) {
                DynamicDeeplink.DeeplinkRoot baseUrl = result.deeplinks.get(key).baseUrl;

                if (baseUrl != null && baseUrl.hosts == null && baseUrl.schemes == null) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " base-url hosts or schemes"));
                    return false;
                }
                if (baseUrl != null && baseUrl.hosts != null && baseUrl.hosts.isEmpty()) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " base-url hosts"));
                    return false;
                }
                if (baseUrl != null && baseUrl.schemes != null && baseUrl.schemes.isEmpty()) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " base-url schemes"));
                    return false;
                }

                List<String> keyPaths = collectKey(result.deeplinks.get(key).paths);
                if (keyPaths != null) {
                    for (String keyPath : keyPaths) {
                        checkValue(key, keyPath, result.deeplinks.get(key).paths.get(keyPath).patterns);
                    }
                }
            }
            invokeValidIfListenerNotNull(listener);
            return true;
        } catch (Exception e) {
            invokeInvalidIfListenerNotNull(listener, e);
            return false;
        }
    }

    private static boolean checkV2(String configJson, OnDeeplinkCheckListener listener) {
        try {
            DynamicDeeplink.DynamicDeeplinkV2 result = new Gson().fromJson(configJson, DynamicDeeplink.DynamicDeeplinkV2.class);
            if (result == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: dynamic-deeplink-v2"));
                return false;
            }
            if (result.deeplink == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: deeplink:{"));
                return false;
            }

            List<String> keys = collectKey(result.deeplink);
            if (keys.size() < 1 || keys.size() == 1 && keys.get(0).equals("deeplink")) {
                invokeInvalidIfListenerNotNull(listener, new IllegalArgumentException("IllegalArgumentException: deeplink:{"));
                return false;
            }
            for (String key : keys) {
                DynamicDeeplink.DeeplinkMap map = result.deeplink.get(key).map;
                DynamicDeeplink.DeeplinkRoot root = result.deeplink.get(key).root;

                if (map == null && root == null) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " map or root"));
                    return false;
                }
                if (map != null && (map.expressions == null || map.expressions.isEmpty())) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " map expressions"));
                    return false;
                }
                if (root != null && root.hosts == null && root.schemes == null) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " root hosts or schemes"));
                    return false;
                }
                if (root != null && root.hosts != null && root.hosts.isEmpty()) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " root hosts"));
                    return false;
                }
                if (root != null && root.schemes != null && root.schemes.isEmpty()) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + " root schemes"));
                    return false;
                }

                List<String> keyPaths = collectKey(result.deeplink.get(key).path);
                if (keyPaths != null) {
                    for (String keyPath : keyPaths) {
                        checkValue(key, keyPath, result.deeplink.get(key).path.get(keyPath).expressions);
                    }
                }
            }
            invokeValidIfListenerNotNull(listener);
            return true;
        } catch (Exception e) {
            invokeInvalidIfListenerNotNull(listener, e);
            return false;
        }
    }

    private static boolean checkV1(String configJson, OnDeeplinkCheckListener listener) {
        try {
            DynamicDeeplink.DynamicDeeplinkV1 result = new Gson().fromJson(configJson, DynamicDeeplink.DynamicDeeplinkV1.class);

            if (result == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: new-dynamic-deeplink"));
                return false;
            }
            if (result.map == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: map:{"));
                return false;
            }
            if (result.premap == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: premap:{"));
                return false;
            }

            List<String> mapKeys = collectKey(result.map);
            for (String key : mapKeys) {
                checkValue("map", key, result.map.get(key).expressions);
            }

            List<String> premapKeys = collectKey(result.premap);
            for (String key : premapKeys) {
                if (result.premap.get(key).expressions == null || result.premap.get(key).expressions.isEmpty()) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException " + key + " expressions"));
                    return false;
                }
            }
            invokeValidIfListenerNotNull(listener);
            return true;
        } catch (Exception e) {
            invokeInvalidIfListenerNotNull(listener, e);
            return false;
        }
    }

    private static List<String> collectKey(Map<String, ?> map) {
        if (map != null) {
            return new ArrayList<>(map.keySet());
        } else {
            return null;
        }
    }

    private static void checkValue(String key, String keyPath, Map<String, String> map) throws NullPointerException, IllegalArgumentException {
        if (map == null || map.isEmpty())
            throw new NullPointerException("NullPointerException " + key + " " + keyPath);
        List<String> list = new ArrayList<>(map.values());
        for (String aList : list) {
            if (aList.contains("//")) {
                throw new IllegalArgumentException("IllegalArgumentException " + key + " " + keyPath + " : " + aList);
            }
            if (aList.length() > 0 && aList.charAt(0) != '/') {
                throw new IllegalArgumentException("IllegalArgumentException " + key + " " + keyPath + " : " + aList);
            }
        }
    }

    private static void invokeInvalidIfListenerNotNull(OnDeeplinkCheckListener listener, Exception e) {
        if (listener != null) {
            listener.onDeeplinkInvalid(e);
        }
    }

    private static void invokeValidIfListenerNotNull(OnDeeplinkCheckListener listener) {
        if (listener != null) {
            listener.onDeeplinkValid();
        }
    }
}
