package com.bukalapak.urlrouter.sample

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bukalapak.urlrouter.Router
import com.bukalapak.urlrouter.RouterMap
import java.util.regex.Pattern

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

        val firstMap = mutableListOf<RouterMap>()

        // https://www.mysite.com/about
        firstMap.add(RouterMap(listOf("/about")) {
            displayResult("Open about page")
        })

        // https://www.mysite.com/promo/tas-keren-pria
        firstMap.add(RouterMap(listOf("/promo/*")) {
            displayResult("Open promo page")
        })

        // https://www.mysite.com/promo/tas-keren-pria/discounted
        firstMap.add(RouterMap(listOf("/promo/*/discounted")) {
            displayResult("Open discounted promo page")
        })

        // https://www.mysite.com/register?referrer=anonymous
        firstMap.add(RouterMap(listOf("/register")) {
            val referrer = it.queries.getString("referrer")
            displayResult("Open registration page with referrer $referrer")
        })

        // https://www.mysite.com/transaction/981239/view
        firstMap.add(RouterMap(listOf("/transaction/<transaction_id>/view")) {
            val transactionId = it.variables.getLong("transaction_id")
            displayResult("Open transaction detail page $transactionId")
        })

        // https://www.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
        firstMap.add(RouterMap(listOf("/product/<product_id:[a-z0-9]+>-*")) {
            val productId = it.variables.getString("product_id")
            displayResult("Open product detail page $productId")
        })

        router.preMap(listOf("*://", ""),
                listOf("<subdomain:\\[a-z]+>.mysite.com", "mysite.com"),
                listOf("/*", "", ":<port:[0-9]+>/*", ":<port:[0-9]+>"),
                firstMap) {
            val subdomain = it.variables.getString("subdomain")
            if (subdomain == "www") {
                var url = it.url
                if (!Pattern.matches("\\w+://.+", it.url)) {
                    url = "https://" + url
                }
                Uri.parse(url).path // Continue routing
            } else {
                displayResult("Launch intent: " + it.url)
                null // Don't continue routing
            }
        }

        val secondMap = mutableListOf<RouterMap>()

        // https://second.mysite.com/about
        secondMap.add(RouterMap(listOf("/about")) {
            displayResult("Open second about page")
        })

        // https://second.mysite.com/promo/tas-keren-pria
        secondMap.add(RouterMap(listOf("/promo/*")) {
            displayResult("Open second promo page")
        })

        // https://second.mysite.com/promo/tas-keren-pria/discounted
        secondMap.add(RouterMap(listOf("/promo/*/discounted")) {
            displayResult("Open second discounted promo page")
        })

        // https://second.mysite.com/register?referrer=anonymous
        secondMap.add(RouterMap(listOf("/register")) {
            val referrer = it.queries.getString("referrer")
            displayResult("Open second registration page with referrer $referrer")
        })

        // https://second.mysite.com/transaction/981239/view
        secondMap.add(RouterMap(listOf("/transaction/<transaction_id>/view")) {
            val transactionId = it.variables.getLong("transaction_id")
            displayResult("Open second transaction detail page $transactionId")
        })

        // https://second.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
        secondMap.add(RouterMap(listOf("/product/<product_id:[a-z0-9]+>-*")) {
            val productId = it.variables.getString("product_id")
            displayResult("Open second product detail page $productId")
        })

        router.preMap(listOf("*://", ""),
                listOf("second.mysite.com"),
                listOf("/*", "", ":<port:[0-9]+>/*", ":<port:[0-9]+>"),
                secondMap) {
            var url = it.url
            if (!Pattern.matches("\\w+://.+", it.url)) {
                url = "https://" + url
            }
            Uri.parse(url).path // Continue routing
        }

        router.preMap("*://third.mysite.com/*", {
            Uri.parse(it.url).path // Continue routing
        }, {
            expression = listOf("/about")
            processor = {
                displayResult("Open third about page")
            }
        }, {
            expression = listOf("/promo/*")
            processor = {
                displayResult("Open third promo page")
            }
        }, {
            expression = listOf("/promo/*/discounted")
            processor = {
                displayResult("Open third discounted promo page")
            }
        }, {
            expression = listOf("/register")
            processor = {
                val referrer = it.queries.getString("referrer")
                displayResult("Open third registration page with referrer $referrer")
            }
        }, {
            expression = listOf("/transaction/<transaction_id>/view")
            processor = {
                val transactionId = it.variables.getLong("transaction_id")
                displayResult("Open third transaction detail page $transactionId")
            }
        }, {
            expression = listOf("/product/<product_id:[a-z0-9]+>-*")
            processor = {
                val productId = it.variables.getString("product_id")
                displayResult("Open third product detail page $productId")
            }
        })

        router.preMap(listOf("*://", ""),
                listOf("fourth.mysite.com"),
                listOf("/*", "", ":<port:[0-9]+>/*", ":<port:[0-9]+>"), {
            var url = it.url
            if (!Pattern.matches("\\w+://.+", it.url)) {
                url = "https://" + url
            }
            Uri.parse(url).path // Continue routing
        }, {
            expression = listOf("/about")
            processor = {
                displayResult("Open fourth about page")
            }
        }, {
            expression = listOf("/promo/*")
            processor = {
                displayResult("Open fourth promo page")
            }
        }, {
            expression = listOf("/promo/*/discounted")
            processor = {
                displayResult("Open fourth discounted promo page")
            }
        }, {
            expression = listOf("/register")
            processor = {
                val referrer = it.queries.getString("referrer")
                displayResult("Open fourth registration page with referrer $referrer")
            }
        }, {
            expression = listOf("/transaction/<transaction_id>/view")
            processor = {
                val transactionId = it.variables.getLong("transaction_id")
                displayResult("Open fourth transaction detail page $transactionId")
            }
        }, {
            expression = listOf("/product/<product_id:[a-z0-9]+>-*")
            processor = {
                val productId = it.variables.getString("product_id")
                displayResult("Open fourth product detail page $productId")
            }
        })
    }

    private fun displayResult(result: String) {
        textViewResult.text = result
    }
}
