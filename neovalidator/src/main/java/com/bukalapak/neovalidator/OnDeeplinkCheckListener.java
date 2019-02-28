package com.bukalapak.neovalidator;

public interface OnDeeplinkCheckListener {
    void onDeeplinkValid(String key);
    void onDeeplinkInvalid(Exception e);
}