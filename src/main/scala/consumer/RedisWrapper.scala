package consumer

import common.Monad
import common.TaskStatus
import com.google.inject.Inject
import redis.clients.jedis.Jedis
import common.ResultMonad
import redis.clients.jedis.JedisPool
import common.ErrorMonad

class RedisWrapper @Inject() (jedisPool: JedisPool) {
    def setTaskStatus(taskId: String, status: TaskStatus): Monad[Exception, Unit] = {
        try {
            val jedis = jedisPool.getResource()
            jedis.set(taskId, status.status)
            jedis.close()
            ResultMonad(())
        } catch {
            case e: Exception => ErrorMonad(e)
        }
    }
}
