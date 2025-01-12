package io.wilski

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun <T, R> askPattern(
    targetActor: ActorRef<T>,
    duration: Duration = 1000.milliseconds,
    msgFactory: (ref: ActorRef<R>) -> T
): Deferred<R> = coroutineScope() {
    val mailbox = Channel<R>(capacity = Channel.UNLIMITED)
    async {
        try {
            withTimeout(duration) {
                targetActor tell msgFactory(actorRef(mailbox))
                mailbox.receive()
            }
        } finally {
            mailbox.close()
        }
    }
}


