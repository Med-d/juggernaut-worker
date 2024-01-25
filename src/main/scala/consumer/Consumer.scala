package consumer

import worker.Worker
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.google.inject.Inject
import parser.TaskParser
import worker.WorkerFactory
import scala.reflect.ClassTag
import common.{ ResultMonad, zipWith, enrichWith }
import common.configuration.Settings
import common.TaskStatus
import common.TaskModel

class Consumer[TTaskContext : ClassTag, TWorker <: Worker[TTaskContext, _]] @Inject() (
    rabbitClient: RabbitMqWrapper,
    redis: RedisWrapper,
    taskParser: TaskParser,
    settings: Settings
) {
    val threadPool = Executors.newFixedThreadPool(settings.threadsCount) // NOTE: было бы круто всё-таки выделять потоки per-request
    val executionContext = ExecutionContext.fromExecutor(threadPool)
    given ExecutionContext = executionContext
    var executionTask: Future[Unit] = null
    var cancellation = false

    var queue: QueueInfo = null
    var workerFactory: WorkerFactory[TWorker] = null

    def configureFor(queue: QueueInfo, workerFactory: WorkerFactory[TWorker]) = {
        this.queue = queue
        this.workerFactory = workerFactory
    }

    def startScanTask(): Unit = {
        if !isReadyForWork() then throw new IllegalStateException("ScanTask is not ready for work")
        rabbitClient.setMessagesHandler(queue, queued => Future {
            val worker = workerFactory.createWorker()
            val taskMonad = taskParser.parse[TaskModel](queued)
            val pipelineResult = taskMonad
                .flatMap(task => redis.setTaskStatus(task.taskId, TaskStatus.InProgress)).zipWith(taskMonad)
                .flatMap((_, task) => taskParser.parse[TTaskContext](task.taskContext))
                .flatMap(context => worker.work(context)).zipWith(taskMonad)
                .flatMap((_, task) => redis.setTaskStatus(task.taskId, TaskStatus.Completed))

            val (err, _) = pipelineResult.tryGetValue
            if (err != null)
                println(err.toString())
        })
    }

    def stopScanTask(force: Boolean = false): Unit = {
        cancellation = true
        Await.ready(executionTask, Duration(10, "sec"))
    }

    private def isReadyForWork() = queue != null && workerFactory != null
}
