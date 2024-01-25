package common

enum TaskStatus(val status: String) {
    case Enqueued extends TaskStatus("ENQUEUED")
    case InProgress extends TaskStatus("IN_PROGRESS")
    case NotFound extends TaskStatus("NOT_FOUND")
    case Completed extends TaskStatus("COMPLETED")
    case Error extends TaskStatus("ERROR")
}
