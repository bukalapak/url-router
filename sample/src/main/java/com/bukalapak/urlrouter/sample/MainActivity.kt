package com.bukalapak.urlrouter.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bukalapak.urlrouter.Router
import com.bukalapak.urlrouter.RouterMap

class MainActivity : AppCompatActivity() {

    private lateinit var editTextUrl: EditText
    private lateinit var buttonRoute: Button
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextUrl = findViewById(R.id.edittext_url)
        buttonRoute = findViewById(R.id.button_route)
        textViewResult = findViewById(R.id.textview_result)

        buttonRoute.setOnClickListener { _ ->
            Router.INSTANCE.route(this, editTextUrl.text.toString())
        }

        setMapping()
    }

    private fun setMapping() {
        val router = Router.INSTANCE

        router.map {
            addPrefix("*://")
            addPrefix("")
            addExpression("<subdomain:\\[a-z]+>.mysite.com")
            addExpression("mysite.com")
            addPostfix("/*")
            addPostfix("")
            addPostfix(":<port:[0-9]+>/*")
            addPostfix(":<port:[0-9]+>")
            setPreProcessor {
                val subdomain = it.variables.getString("subdomain")
                if (!"www".equals(subdomain)) {
                    displayResult("Launch intent: " + it.url)
                }
            }
            // https://www.mysite.com/about
            addPath("/about", {
                displayResult("Open about page")
            })
            // https://www.mysite.com/promo/tas-keren-pria
            addPath("/promo/*", {
                displayResult("Open promo page")
            })
            // https://www.mysite.com/promo/tas-keren-pria/discounted
            addPath("/promo/*/discounted", {
                displayResult("Open discounted promo page")
            })
            // https://www.mysite.com/register?referrer=anonymous
            addPath("/register", {
                val referrer = it.queries.getString("referrer")
                displayResult("Open registration page with referrer $referrer")
            })
            // https://www.mysite.com/transaction/981239/view
            addPath("/transaction/<transaction_id>/view", {
                val transactionId = it.variables.getLong("transaction_id")
                displayResult("Open transaction detail page $transactionId")
            })
            // https://www.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
            addPath("/product/<product_id:[a-z0-9]+>-*", {
                val productId = it.variables.getString("product_id")
                displayResult("Open product detail page $productId")
            })
        }

        router.map(RouterMap.builder()
                .addPrefixes(listOf("*://", ""))
                .addExpression("second.mysite.com")
                .setPostfixes(listOf("/*", "", ":<port:[0-9]+>/*", ":<port:[0-9]+>"))
                // https://second.mysite.com/about
                .addPaths(listOf("/about","/aboutme"), {
                    displayResult("Open second about page")
                })
                // https://second.mysite.com/promo/tas-keren-pria
                .addPath("/promo/*", {
                    displayResult("Open second promo page")
                })
                // https://second.mysite.com/promo/tas-keren-pria/discounted
                .addPath("/promo/*/discounted", {
                    displayResult("Open second discounted promo page")
                })
                // https://second.mysite.com/register?referrer=anonymous
                .addPath("/register", {
                    val referrer = it.queries.getString("referrer")
                    displayResult("Open second registration page with referrer $referrer")
                })
                // https://second.mysite.com/transaction/981239/view
                .addPath("/transaction/<transaction_id>/view", {
                    val transactionId = it.variables.getLong("transaction_id")
                    displayResult("Open second transaction detail page $transactionId")
                })
                // https://second.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
                .addPath("/product/<product_id:[a-z0-9]+>-*", {
                    val productId = it.variables.getString("product_id")
                    displayResult("Open second product detail page $productId")
                })
        )
    }

    private fun displayResult(result: String) {
        textViewResult.text = result
    }
}
