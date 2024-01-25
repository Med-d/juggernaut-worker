package parser

import com.fasterxml.jackson.databind.json.JsonMapper
import com.google.inject.Inject
import scala.reflect.ClassTag
import common.Monad
import common.ErrorMonad
import common.ResultMonad

case class TaskCannotBeParsed(message: String)

class TaskParser @Inject() (
    jsonMapper: JsonMapper
) {
    def parse[TTaskModel: ClassTag](jsonTask: String): Monad[TaskCannotBeParsed, TTaskModel] = {
        try {
            val t = implicitly[ClassTag[TTaskModel]].runtimeClass
            val deserialized = jsonMapper.readValue(jsonTask, t)
            val obj = deserialized.asInstanceOf[TTaskModel]
            obj match
                case null => ErrorMonad(TaskCannotBeParsed("Parse error"))
                case _: TTaskModel => ResultMonad(obj)
        } catch {
            case e: Exception => ErrorMonad(TaskCannotBeParsed(e.toString()))
        }
    }
}
