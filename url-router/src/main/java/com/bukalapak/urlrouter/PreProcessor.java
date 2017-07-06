package com.bukalapak.urlrouter;

import android.content.Context;

/**
 * Created by mrhabibi on 5/23/17.
 */

public interface PreProcessor {
    String proceed(Context ctx, Result result);
}
