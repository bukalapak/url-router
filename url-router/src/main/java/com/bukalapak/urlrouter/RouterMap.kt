package com.bukalapak.urlrouter

class RouterMap private constructor(
        val prefixes: List<String>,
        val expressions: List<String>,
        val postfixes: List<String>,
        val preProcessor: Processor,
        val path: List<Path>) {

    class Builder {
        private var prefixes: MutableList<String> = mutableListOf()
        private var expressions: MutableList<String> = mutableListOf()
        private var postfixes: MutableList<String> = mutableListOf()
        private var preProcessor: PreProcessor = {}
        private var path: MutableList<Path> = mutableListOf()

        fun addPrefix(prefix: String): Builder {
            this.prefixes.add(prefix)
            return this
        }

        fun addPrefixes(prefixes: List<String>): Builder {
            this.prefixes.addAll(prefixes)
            return this
        }

        fun setPrefixes(prefix: List<String>): Builder {
            this.prefixes = prefix.toMutableList()
            return this
        }

        fun addExpression(expression: String): Builder {
            this.expressions.add(expression)
            return this
        }

        fun addExpressions(expressions: List<String>): Builder {
            this.expressions.addAll(expressions)
            return this
        }

        fun setExpressions(expression: List<String>): Builder {
            this.expressions = expression.toMutableList()
            return this
        }

        fun addPostfix(postfix: String): Builder {
            this.postfixes.add(postfix)
            return this
        }

        fun addPostfixes(postfixes: List<String>): Builder {
            this.postfixes.addAll(postfixes)
            return this
        }

        fun setPostfixes(postfix: List<String>): Builder {
            this.postfixes = postfix.toMutableList()
            return this
        }

        fun setPreProcessor(preProcessor: PreProcessor): Builder {
            this.preProcessor = preProcessor
            return this
        }

        fun addPath(expression: String, processor: Processor): Builder {
            this.path.add(Path(expression, processor))
            return this
        }

        fun addPaths(expressions: List<String>, processor: Processor): Builder {
            this.path.addAll(expressions.map{ Path(it, processor) })
            return this
        }

        fun build(): RouterMap {
            return RouterMap(prefixes, expressions, postfixes, preProcessor, path)
        }
    }

    companion object {
        fun builder(): Builder {
            return RouterMap.Builder()
        }
    }
}