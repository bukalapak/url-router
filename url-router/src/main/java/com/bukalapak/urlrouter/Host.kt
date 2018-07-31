package com.bukalapak.urlrouter

class Host(var prefixes: List<String> = emptyList(),
           var expressions: List<String>,
           var postfixes: List<String> = emptyList())