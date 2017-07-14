package com.bukalapak.urlrouter.sample;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bukalapak.urlrouter.Router;

public class MainActivity extends AppCompatActivity {

    EditText editTextUrl;
    Button buttonRoute;
    TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUrl = (EditText) findViewById(R.id.edittext_url);
        buttonRoute = (Button) findViewById(R.id.button_route);
        textViewResult = (TextView) findViewById(R.id.textview_result);

        buttonRoute.setOnClickListener(view -> {
            Router.getInstance().route(this, editTextUrl.getText().toString(), null);
        });

        setMapping();
    }

    private void setMapping() {
        Router router = Router.getInstance();

        router.preMap("*://<subdomain:[a-z]+>.mysite.com/*", (ctx, result) -> {
            String subdomain = result.variables.get("subdomain");
            if (subdomain.equals("blog")) {
                displayResult("Launch intent: " + result.url);
                return null; // Don't continue routing
            } else {
                return Uri.parse(result.url).getPath(); // Continue routing
            }
        });

        // https://www.mysite.com/about
        router.map("/about", (ctx, result) -> {
            displayResult("Open about page");
        });

        // https://www.mysite.com/promo/tas-keren-pria
        router.map("/promo/*", (ctx, result) -> {
            displayResult("Open promo page");
        });

        // https://www.mysite.com/promo/tas-keren-pria/discounted
        router.map("/promo/*/discounted", (ctx, result) -> {
            displayResult("Open discounted promo page");
        });

        // https://www.mysite.com/register?referrer=anonymous
        router.map("/register", (ctx, result) -> {
            String referrer = result.queries.get("referrer");
            displayResult("Open registration page with referrer " + referrer);
        });

        // https://www.mysite.com/register?referrer=anonymous
        router.map("/transaction/<transaction_id>/view", (ctx, result) -> {
            String transactionId = result.variables.get("transaction_id");
            displayResult("Open transaction detail page " + transactionId);
        });

        // https://www.mysite.com/product/kj9fd8-tas-paling-keren-masa-kini
        router.map("/product/<product_id:[a-z0-9]+>-*", (ctx, result) -> {
            String productId = result.variables.get("product_id");
            displayResult("Open product detail page " + productId);
        });
    }

    private void displayResult(String result) {
        textViewResult.setText(result);
    }
}
