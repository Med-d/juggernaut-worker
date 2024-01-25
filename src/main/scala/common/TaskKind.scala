package common

enum TaskKind(val kind: String) {
    case DownloadFile extends TaskKind("download-file")
}
