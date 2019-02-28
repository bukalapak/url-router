package com.bukalapak.neovalidator;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoDeeplinkValidator {
    public static void main(String[] args) {
        check(args[0], args[1], new OnDeeplinkCheckListener() {
            @Override
            public void onDeeplinkValid(String key) {
                System.out.println("VALID JSON, key: " + key);
                System.exit(0);
            }

            @Override
            public void onDeeplinkInvalid(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    public static void check(String key, String filePath, OnDeeplinkCheckListener listener) {
        File file = new File(filePath);
        switch (key) {
            case "dynamic-deeplink":
                parseFileV1(key, file, listener);
                break;
            case "dynamic-deeplink-v2":
                parseFileV2(key, file, listener);
                break;
            default:
                listener.onDeeplinkInvalid(new IllegalArgumentException("version not found"));
        }
    }

    private static void parseFileV2(String keyVersion, File file, OnDeeplinkCheckListener listener) {
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            DynamicDeeplink.DynamicDeeplinkV2 result = gson.fromJson(br, DynamicDeeplink.DynamicDeeplinkV2.class);

            if (result == null) {
                listener.onDeeplinkInvalid(new NullPointerException("NullPointerException: dynamic-deeplink-v2"));
                return;
            }
            if (result.deeplink == null) {
                listener.onDeeplinkInvalid(new NullPointerException("NullPointerException: deeplink:{"));
                return;
            }

            List<String> keys = collectKey(result.deeplink);
            if (keys.size() <= 1) {
                listener.onDeeplinkInvalid(new IllegalArgumentException("IllegalArgumentException: deeplink:{"));
                return;
            }
            for (String key : keys) {
                DynamicDeeplink.DeeplinkMap map = result.deeplink.get(key).map;
                if (map.expressions == null) {
                    listener.onDeeplinkInvalid(new NullPointerException("NullPointerException: "+ key + "map expressions" ));
                }

                List<String> keyPaths = collectKey(result.deeplink.get(key).path);
                if (keyPaths != null) {
                    for (String keyPath : keyPaths) {
                        checkValue(keyPath, result.deeplink.get(key).path.get(keyPath).expressions);
                    }
                }
            }
            listener.onDeeplinkValid(keyVersion);
        } catch (Exception e) {
            listener.onDeeplinkInvalid(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    listener.onDeeplinkInvalid(e1);
                }
            }
        }
    }

    private static void parseFileV1(String keyVersion, File file, OnDeeplinkCheckListener listener) {
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            DynamicDeeplink.DynamicDeeplinkV1 result = gson.fromJson(br, DynamicDeeplink.DynamicDeeplinkV1.class);

            if (result == null) {
                listener.onDeeplinkInvalid(new NullPointerException("NullPointerException: dynamic-deeplink"));
                return;
            }
            if (result.map == null) {
                listener.onDeeplinkInvalid(new NullPointerException("NullPointerException: map:{"));
                return;
            }
            if (result.premap == null) {
                listener.onDeeplinkInvalid(new NullPointerException("NullPointerException: premap:{"));
                return;
            }

            List<String> keys = collectKey(result.map);
            for (String key : keys) {
                checkValue(key, result.map.get(key).expressions);
            }
            listener.onDeeplinkValid(keyVersion);
        } catch (Exception e) {
            listener.onDeeplinkInvalid(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    listener.onDeeplinkInvalid(e1);
                }
            }
        }
    }

    private static List<String> collectKey(Map<String, ?> map) {
        if (map != null) {
            return new ArrayList<>(map.keySet());
        } else {
            return null;
        }
    }

    private static void checkValue(String key, Map<String, String> map) throws NullPointerException, IllegalArgumentException {
        if (map == null) throw new NullPointerException("NullPointerException " + key + " expression");
        List<String> list = new ArrayList<>(map.values());
        for (String aList : list) {
            if (aList.contains("//")) {
                throw new IllegalArgumentException("Illegal expressions " + key + " : " + aList);
            }
        }
    }
}