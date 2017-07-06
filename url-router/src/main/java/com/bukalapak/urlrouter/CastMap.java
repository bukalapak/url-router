package com.bukalapak.urlrouter;

import java.util.HashMap;

/**
 * Created by mrhabibi on 5/26/17.
 */

public class CastMap extends HashMap<String, String> {

    public long getLong(String key) {
        if (containsKey(key)) {
            try {
                return Long.valueOf(get(key));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return 0L;
            }
        }
        return 0L;
    }

    public int getInt(String key) {
        if (containsKey(key)) {
            try {
                return Integer.valueOf(get(key));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public boolean getBool(String key) {
        if (containsKey(key)) {
            String value = get(key);
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
        }
        return false;
    }

}
