[ ![Download](https://api.bintray.com/packages/mrhabibi/maven/url-router/images/download.svg) ](https://bintray.com/mrhabibi/maven/url-router/_latestVersion)

# URL Router
A wrapper for easily routing URL on Android

## Examples

### Initialization

```kotlin
/* 
 * Mapping the schema and host
 */
 
Router.INSTANCE.preMap("*://<subdomain:[a-z]+>.mysite.com/*", {
    val subdomain = it.variables.getString("subdomain")
    if (subdomain == "blog") {
        // Launch intent
        ...
        null // Don't continue to path routing below by returning null
    } else {
        Uri.parse(it.url).path // Continue to path routing below
    }
})

/* 
 * Mapping the path
 */

/* Simple mapping */

Router.INSTANCE.map("/about", {
    ...
})

/* Wildcard at the end for any characters until end of url */

Router.INSTANCE.map("/promo/*", {
    ...
})

/* Wildcard in segment for any character in specific segment */

Router.INSTANCE.map("/promo/*/discounted", {
    ...
})

/* Get value from parsed queries */

Router.INSTANCE.map("/register", {
    val referrer = it.queries.getString("referrer")
    ...
})

/* Get value from parsed variables in segment of path */

Router.INSTANCE.map("/transaction/<transaction_id>/view", {
    val transactionId = it.variables.getLong("transaction_id")
    ...
})

/* Get value from parsed variables in subsegment of segment */

Router.INSTANCE.map("/product/<product_id:[a-z0-9]+>-*", {
    val productId = it.variables.getString("product_id")
    ...
})
```

### Usage

```kotlin
Router.INSTANCE.route(context, url, optionalArgs)
```

### Multiple Instances

```kotlin
val loginUserRouter = Router()
val nonloginUserRouter = Router()
```

### Language

#### Wildcard : `*`
- Can be placed anywhere except query
- Used for replacing any character inside URL
- Regex: `.+` -> Any character more than 1

#### Variable Name : `<variable_name>`
- Can be placed anywhere except query
- Used for getting value inside URL
- Variable name can only use `A-Z`, `a-z`, `0-9`, and `_`
- Default regex: `[^/]+` -> Any character more than 1 except `/`
- Default regex can be changed

#### Variable Name With Specific Pattern : `<variable_name:[a-z0-9]+>`
- Can be placed anywhere except query
- Used for getting value inside URL
- Variable name can only use `A-Z`, `a-z`, `0-9`, and `_`
- Default regex: `[^/]+` -> Any character more than 1 except `/`
- Default regex can be changed
- Variable name and regex separated with `:`
- Specific regex overrides default regex

## Installation

### Gradle

Add this line in your `build.gradle` file:

```
compile 'com.bukalapak:url-router:1.1.2'
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