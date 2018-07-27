package com.bukalapak.urlrouter

class RouterMap(var expression: List<String> = emptyList(),
                var processor: Processor = {})