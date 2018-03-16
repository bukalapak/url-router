package com.bukalapak.urlrouter.sample

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bukalapak.urlrouter.Router

class MainActivity : AppCompatActivity() {

    private lateinit var editTextUrl: EditText
    private lateinit var buttonRoute: Button
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextUrl = findViewById(R.id.edittext_url) as EditText
        buttonRoute = findViewById(R.id.button_route) as Button
        textViewResult = findViewById(R.id.textview_result) as TextView

        buttonRoute.setOnClickListener { view ->
            Router.INSTANCE.route(this, editTextUrl.text.toString())
        }

        setMapping()
    }

    private fun setMapping() {
        val router = Router.INSTANCE

        router.preMap("*://<subdomain:[a-z]+>.mysite.com/*", {
            val subdomain = it.variables.getString("subdomain")
            if (subdomain == "blog") {
                displayResult("Launch intent: " + it.url)
                null // Don't continue routing
            } else {
                Uri.parse(it.url).path // Continue routing
            }
        })

        // https://www.mysite.com/about
        router.map("/about", { displayResult("Open about page") })

        // https://www.mysite.com/promo/tas-keren-pria
        router.map("/promo/*", { displayResult("Open promo page") })

        // https://www.mysite.com/promo/tas-keren-pria/discounted
        router.map("/promo/*/discounted", { displayResult("Open discounted promo page") })

        // https://www.mysite.com/register?referrer=anonymous
        router.map("/register", {
            val referrer = it.queries.getString("referrer")
            displayResult("Open registration page with referrer " + referrer)
        })

        // https://www.mysite.com/register?referrer=anonymous
        router.map("/transaction/<transaction_id>/view", {
            val transactionId = it.variables.getLong("transaction_id")
            displayResult("Open transaction detail page " + transactionId)
        })

        // https://www.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
        router.map("/product/<product_id:[a-z0-9]+>-*", {
            val productId = it.variables.getString("product_id")
            displayResult("Open product detail page " + productId)
        })
    }

    private fun displayResult(result: String) {
        textViewResult.text = result
    }
}
