package com.bukalapak.deeplinkvalidator;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class DynamicDeeplink {

    public class DynamicDeeplinkV1 {
        @SerializedName("map")
        public Map<String, DeeplinkPath> map;
        @SerializedName("premap")
        public Map<String, DeeplinkMap> premap;
    }

    public class DynamicDeeplinkV2 {
        @SerializedName("deeplink")
        public Map<String, Deeplink> deeplink;
    }

    public class Deeplink {
        @SerializedName("map")
        public DeeplinkMap map;
        @SerializedName("path")
        public Map<String, DeeplinkPath> path;
    }

    public class DeeplinkMap {
        @SerializedName("expressions")
        public Map<String, String> expressions;
        @SerializedName("postfixes")
        public Map<String, String> postfixes;
        @SerializedName("prefixes")
        public Map<String, String> prefixes;
    }

    public class DeeplinkPath {
        @SerializedName("expressions")
        public Map<String, String> expressions;
    }
}
