package com.bukalapak.lib_deeplinkvalidator;

public interface OnDeeplinkCheckListener {
    void onDeeplinkValid(String key);
    void onDeeplinkInvalid(Exception e);
}