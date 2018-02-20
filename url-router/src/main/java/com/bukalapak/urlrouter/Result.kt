package com.bukalapak.urlrouter

import android.content.Context
import android.os.Bundle

/**
 * Created by mrhabibi on 5/26/17.
 */

class Result(val context: Context,
             val url: String,
             val variables: CastMap?,
             val queries: CastMap?,
             val args: Bundle?)
