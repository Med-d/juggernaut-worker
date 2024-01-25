package parser

import common.TaskKind
import common.{Monad, mapToMonad}

class UnrecognizedTaskKind

class TaskKindParser {
    def parse(rawTaskKind: String): Monad[UnrecognizedTaskKind, TaskKind] = 
        TaskKind.values
            .find(k => k.kind.equals(rawTaskKind))
            .mapToMonad(UnrecognizedTaskKind())
}
