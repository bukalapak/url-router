[ ![Download](https://api.bintray.com/packages/mrhabibi/maven/url-router/images/download.svg) ](https://bintray.com/mrhabibi/maven/url-router/_latestVersion)

# URL Router
A wrapper for easily routing URL on Android

## Examples

### Initialization

```
Router router = new Router();

// Mapping the schema and authority
router.preMap("*://<subdomain>.mysite.com/*", (ctx, result) -> {
    String subdomain = result.variables.get("subdomain");
    if (subdomain.equals("blog")) {
        // Launch intent
        return null; // Don't continue to path routing below by returning null
    } else {
        return Uri.parse(result.url).getPath(); // Continue to path routing below
    }
});

// Mapping the path

// Simple mapping
router.map("/about", (ctx, result) -> {
    // Launch about activity
});

// Wildcard at the end for any characters until end of url
router.map("/promo/*", (ctx, result) -> {
    // Launch promo activity
});

// Wildcard in segment for any character in specific segment
router.map("/promo/*/discounted", (ctx, result) -> {
    // Launch discounted promo activity
});

// Get value from parsed queries
router.map("/login", (ctx, result) -> {
    String referrer = result.queries.get("referrer")
    // Launch login activity
});

// Get value from parsed variables in segment of path
router.map("/transaction/<transaction_id>/view", (ctx, result) -> {
    String transactionId = result.variables.get("transaction_id")
    // Launch transaction activity
});

// Get value from parsed variables in subsegment of segment
router.map("/product/<product_id>-*", (ctx, result) -> {
    String productId = result.variables.get("product_id")
    // Launch product activity
});
```

### Routing Usage

```
router.route(context, url, optionalArgs)
```

## Installation

### Gradle

Add this line in your `build.gradle` file:

```
compile 'com.mrhabibi:url-router:0.0.2'
```

This library is hosted in the [JCenter repository](https://bintray.com/bukalapak/maven), so you have to ensure that the repository is included:

```
buildscript {
   repositories {
      jcenter()
   }
}
```

## Contributions

Feel free to create issues and pull requests.

## License

```
URL Router library for Android
Copyright (c) 2017 Bukalapak (http://github.com/bukalapak).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```