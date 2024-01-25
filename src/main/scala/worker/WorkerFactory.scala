package worker

trait WorkerFactory[TWorker <: Worker[_, _]] {
    def createWorker(): TWorker
}
