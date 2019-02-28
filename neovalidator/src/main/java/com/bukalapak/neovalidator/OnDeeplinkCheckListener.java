package com.bukalapak.neovalidator;

public interface OnDeeplinkCheckListener {
    void onDeeplinkValid();
    void onDeeplinkInvalid(Exception e);
}