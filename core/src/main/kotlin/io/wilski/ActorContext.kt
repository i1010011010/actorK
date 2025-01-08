package io.wilski

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import kotlin.coroutines.CoroutineContext

interface ActorContext<T> {
    fun self(): ActorRef<T>
    fun name(): String
    fun log(): Logger
    suspend fun pipeToSelf(msg: T)
    suspend fun <S> spawn(behavior: Behavior<S>, name: String = anonymousName()): ActorRef<S>
}

internal fun <T> actorContext(
    name: String,
    self: ActorRef<T>,
    job: Job,
    scope: CoroutineScope,
): ActorContext<T> =
    object : ActorContext<T> {
        override fun self(): ActorRef<T> = self
        override fun name(): String = name
        override fun log(): Logger = LoggerFactory.getLogger(name)
        override suspend fun pipeToSelf(msg: T) = let { self tell msg }
        override suspend fun <S> spawn(behavior: Behavior<S>, name: String): ActorRef<S> = createActorScope<S>(
            behavior,
            name,
            scope,
            buildCoroutineContext(job, name),
        )
    }

private fun buildCoroutineContext(parentJob: Job, name: String): CoroutineContext = parentJob + CoroutineName(name)
private fun anonymousName(): String = "\$actor-${SecureRandom().nextLong(Long.MAX_VALUE)}"
