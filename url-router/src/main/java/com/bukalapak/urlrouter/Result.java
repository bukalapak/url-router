package com.bukalapak.urlrouter;

import android.os.Bundle;

/**
 * Created by mrhabibi on 5/26/17.
 */

public class Result {
    public final String url;
    public final CastMap variables;
    public final CastMap queries;
    public final Bundle args;

    Result(String url, CastMap variables, CastMap queries, Bundle args) {
        this.url = url;
        this.variables = variables;
        this.queries = queries;
        this.args = args;
    }
}
