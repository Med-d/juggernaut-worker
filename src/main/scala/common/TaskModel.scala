package common

import com.fasterxml.jackson.annotation.JsonProperty
import worker.downloadfile.DownloadFileWorkerContext

// NOTE: Пришлось сделать реализации моделей для каждого вида тасок из-за кривой работы scala с дженериками в рантайме
final case class TaskModel(
    @JsonProperty("id") val taskId: String,
    @JsonProperty("kind") val taskKind: String,
    @JsonProperty("context") val taskContext: String,
)
