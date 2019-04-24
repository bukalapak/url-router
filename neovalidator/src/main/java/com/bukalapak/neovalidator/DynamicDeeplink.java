package com.bukalapak.neovalidator;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

class DynamicDeeplink {

    //V1
    class DynamicDeeplinkV1 {
        @SerializedName("map")
        Map<String, DeeplinkPath> map;
        @SerializedName("premap")
        Map<String, DeeplinkMap> premap;
    }
    //==================================

    //V2
    class DynamicDeeplinkV2 {
        @SerializedName("deeplink")
        Map<String, DeeplinkV2> deeplink;
    }

    class DeeplinkV2 {
        @SerializedName("map")
        DeeplinkMap map;
        @SerializedName("path")
        Map<String, DeeplinkPath> path;
        @SerializedName("root")
        DeeplinkRoot root;

    }
    //===================================

    //V3
    class DynamicDeeplinkV3 {
        @SerializedName("deeplinks")
        Map<String, DeeplinkV3> deeplinks;
    }

    class DeeplinkV3 {
        @SerializedName("base-url")
        DeeplinkRoot baseUrl;
        @SerializedName("paths")
        Map<String, DeeplinkPattern> paths;
    }

    class DeeplinkPattern {
        @SerializedName("patterns")
        Map<String, String> patterns;
    }
    //====================================

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

    class DeeplinkRoot {
        @SerializedName("schemes")
        Map<String, String> schemes;
        @SerializedName("hosts")
        Map<String, String> hosts;
        @SerializedName("ports")
        Map<String, Integer> ports;
    }
}