package io.wilski

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope

interface ActorSystem<in T> {
    fun name(): String
}

suspend fun <T> actorsKSystem(
    guardianBehavior: Behavior<T>,
    name: String = "default-system",
    action: suspend (ActorRef<T>) -> Unit
): ActorSystem<T> = coroutineScope {
    action(createActorScope<T>(guardianBehavior, name, this, CoroutineName(name)))
    object : ActorSystem<T> {
        override fun name(): String = name
    }
}

suspend fun <T> actorsKSystem(
    guardianBehavior: Behavior<T>,
    name: String = "default-system",
): ActorRef<T> = coroutineScope {
    createActorScope<T>(guardianBehavior, name, this, CoroutineName(name))
}


