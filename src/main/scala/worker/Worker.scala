package worker

import common.Monad

trait Worker[TContext, TError] {
    def work(context: TContext): Monad[TError, Unit]
}
