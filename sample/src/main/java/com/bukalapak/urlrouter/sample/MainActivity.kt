package com.bukalapak.urlrouter.sample

import android.net.Uri
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

        val firstMap = mutableListOf<RouterMap>()

        // https://first.mysite.com/about
        firstMap.add(RouterMap(listOf("/about")){
            displayResult("Open first about page")
        })

        // https://first.mysite.com/promo/tas-keren-pria
        firstMap.add(RouterMap(listOf("/promo/*")){
            displayResult("Open first promo page")
        })

        // https://first.mysite.com/promo/tas-keren-pria/discounted
        firstMap.add(RouterMap(listOf("/promo/*/discounted")){
            displayResult("Open first discounted promo page")
        })

        // https://first.mysite.com/register?referrer=anonymous
        firstMap.add(RouterMap(listOf("/register")){
            val referrer = it.queries.getString("referrer")
            displayResult("Open first registration page with referrer $referrer")
        })

        // https://first.mysite.com/transaction/981239/view
        firstMap.add(RouterMap(listOf("/transaction/<transaction_id>/view")){
            val transactionId = it.variables.getLong("transaction_id")
            displayResult("Open first transaction detail page $transactionId")
        })

        // https://first.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
        firstMap.add(RouterMap(listOf("/product/<product_id:[a-z0-9]+>-*")){
            val productId = it.variables.getString("product_id")
            displayResult("Open first product detail page $productId")
        })

        router.preMap("*://first.mysite.com/*", firstMap) {
            val subdomain = it.variables.getString("subdomain")
            if (subdomain == "blog") {
                displayResult("Launch intent: " + it.url)
                null // Don't continue routing
            } else {
                Uri.parse(it.url).path // Continue routing
            }
        }

        val secondMap = mutableListOf<RouterMap>()

        // https://second.mysite.com/about
        secondMap.add(RouterMap(listOf("/about")){
            displayResult("Open second about page")
        })

        // https://second.mysite.com/promo/tas-keren-pria
        secondMap.add(RouterMap(listOf("/promo/*")){
            displayResult("Open second promo page")
        })

        // https://second.mysite.com/promo/tas-keren-pria/discounted
        secondMap.add(RouterMap(listOf("/promo/*/discounted")){
            displayResult("Open second discounted promo page")
        })

        // https://second.mysite.com/register?referrer=anonymous
        secondMap.add(RouterMap(listOf("/register")){
            val referrer = it.queries.getString("referrer")
            displayResult("Open second registration page with referrer $referrer")
        })

        // https://second.mysite.com/transaction/981239/view
        secondMap.add(RouterMap(listOf("/transaction/<transaction_id>/view")){
            val transactionId = it.variables.getLong("transaction_id")
            displayResult("Open second transaction detail page $transactionId")
        })

        // https://second.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
        secondMap.add(RouterMap(listOf("/product/<product_id:[a-z0-9]+>-*")){
            val productId = it.variables.getString("product_id")
            displayResult("Open second product detail page $productId")
        })

        router.preMap("*://second.mysite.com/*", secondMap) {
            val subdomain = it.variables.getString("subdomain")
            if (subdomain == "blog") {
                displayResult("Launch intent: " + it.url)
                null // Don't continue routing
            } else {
                Uri.parse(it.url).path // Continue routing
            }
        }
    }

    private fun displayResult(result: String) {
        textViewResult.text = result
    }
}
