package worker.downloadfile

import com.fasterxml.jackson.annotation.JsonProperty
import worker.Worker
import worker.WorkerFactory
import common.Monad
import common.ResultMonad
import com.google.inject.Inject
import common.clients.ControllerClientFactory
import io.github.heavypunk.controller.client.Settings
import io.github.heavypunk.controller.client.contracts.files.PullFileFromS3Request
import java.time.Duration
import common.{ ErrorMonad, mapToMonad }
import common.Retrier
import io.github.heavypunk.controller.client.contracts.files.PollTaskRequest

class DownloadFileWorkerFactory @Inject() (controllerClientFactory: ControllerClientFactory) extends WorkerFactory[DownloadFileWorker] {
    override def createWorker(): DownloadFileWorker = DownloadFileWorker(controllerClientFactory)
}

class DownloadFileWorker(controllerClientFactory: ControllerClientFactory) extends Worker[DownloadFileWorkerContext, Exception] {
    override def work(context: DownloadFileWorkerContext): Monad[Exception, Unit] = {
        try {
            val controllerClient = controllerClientFactory.getControllerClient(Settings(context.controllerScheme, context.controllerIp, context.controllerPort))
            val fileName = context.s3Path.split('/').last
            val pullFileFromS3Response = controllerClient.files.pullFileFromS3(PullFileFromS3Request(
                context.s3Bucket,
                context.s3Path,
                s"${context.destinationPath}/${fileName}"
            ), Duration.ofMinutes(2))
            if !pullFileFromS3Response.success then throw Exception("Pull file from S3 error: " + pullFileFromS3Response.error)
            
            val taskId = pullFileFromS3Response.taskId
            val onAttempt = s"Pulling task ${taskId}"
            val attemptsCount = 80
            val taskResult = Retrier.work(
                action = controllerClient.files.pollTask(PollTaskRequest(taskId), Duration.ofMinutes(2)),
                isSuccess = res => res.success && (res.taskStatus.equalsIgnoreCase("completed") || res.taskStatus.equalsIgnoreCase("failed")),
                attempts = attemptsCount,
                delay = Duration.ofSeconds(2),
                onException = e => printf(e.toString()),
                onAttempt = attempt => printf(onAttempt)
            ).mapToMonad(Exception("Didn't wait for task completed"))

            taskResult.flatMap(result => if result.taskStatus.equalsIgnoreCase("completed") then ResultMonad(()) else ErrorMonad(Exception(s"Task didn't completed successfully: ${result.taskError}")))
        } catch {
            case e: Exception => ErrorMonad(e)
        }
    }
}

case class DownloadFileWorkerContext(
    @JsonProperty("controller-scheme") val controllerScheme: String,
    @JsonProperty("controller-ip") val controllerIp: String,
    @JsonProperty("controller-port") val controllerPort: Int,
    @JsonProperty("s3-bucket") val s3Bucket: String,
    @JsonProperty("s3-path") val s3Path: String,
    @JsonProperty("destination-path") val destinationPath: String
)
