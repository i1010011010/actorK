package io.wilski

sealed interface Behavior<in T>

fun <T> setup(init: suspend (ActorContext<T>) -> Behavior<T>): Behavior<T> = Setup(init)
fun <T> receive(handler: suspend (ActorContext<T>, T) -> Behavior<T>): Behavior<T> = Receive(handler)
fun <T> receiveMessage(handler: suspend (T) -> Behavior<T>): Behavior<T> = Receive { _, msg -> handler(msg) }

@Suppress("UNCHECKED_CAST")
fun <T> same(): Behavior<T> = Same as Behavior<T>

@Suppress("UNCHECKED_CAST")
fun <T> stopped(): Behavior<T> = Stopped as Behavior<T>

fun <T> stopped(postStop: suspend (ActorContext<T>) -> Unit): Behavior<T> = object : StoppedWithEffect<T> {
    override suspend fun postStop(ctx: ActorContext<T>): Behavior<T> = let {
        postStop(ctx)
        this
    }
}

@JvmInline
internal value class Receive<T>(val receive: suspend (ActorContext<T>, T) -> Behavior<T>) : Behavior<T>

@JvmInline
internal value class Setup<T>(val init: suspend (ActorContext<T>) -> Behavior<T>) : Behavior<T>
internal data object Same : Behavior<Nothing>
internal data object Stopped : Behavior<Nothing>
internal interface StoppedWithEffect<T> : Behavior<T> {
    suspend fun postStop(ctx: ActorContext<T>): Behavior<T>
}

interface BehaviorBuilder<T>: Behavior<T>
