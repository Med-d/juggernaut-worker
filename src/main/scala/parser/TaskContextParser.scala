package parser

import common.Monad

class ContextParseError

class TaskContextParser {
    def parse[TContext](context: String): Monad[ContextParseError, TContext] = ???
}

