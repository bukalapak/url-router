package com.bukalapak.neovalidator;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

class DynamicDeeplink {

    class DynamicDeeplinkV1 {
        @SerializedName("map")
        Map<String, DeeplinkPath> map;
        @SerializedName("premap")
        Map<String, DeeplinkMap> premap;
    }

    class DynamicDeeplinkV2 {
        @SerializedName("deeplink")
        Map<String, Deeplink> deeplink;
    }

    class Deeplink {
        @SerializedName("map")
        DeeplinkMap map;
        @SerializedName("path")
        Map<String, DeeplinkPath> path;
    }

    class DeeplinkMap {
        @SerializedName("expressions")
        Map<String, String> expressions;
        @SerializedName("postfixes")
        Map<String, String> postfixes;
        @SerializedName("prefixes")
        Map<String, String> prefixes;
    }

    class DeeplinkPath {
        @SerializedName("expressions")
        Map<String, String> expressions;
    }
}