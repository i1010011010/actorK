package io.wilski

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T, R> CoroutineScope.actorFlow(behavior: Behavior<T>, msgFactory: (ref: ActorRef<R>) -> T): Flow<R> =
    callbackFlow {
        val mailbox = Channel<R>(capacity = Channel.UNLIMITED)
        actorsKSystem(behavior) tell msgFactory(actorRef(mailbox))

        while (true) {
            trySend(mailbox.receive())
        }

        awaitClose {
            mailbox.close()
        }
    }
