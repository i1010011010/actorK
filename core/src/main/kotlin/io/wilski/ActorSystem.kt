package io.wilski

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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

internal fun <T> CoroutineScope.actorsKSystem(
    guardianBehavior: Behavior<T>,
    name: String = "default-system",
): ActorRef<T> = createActorScope<T>(guardianBehavior, name, this, CoroutineName(name))


