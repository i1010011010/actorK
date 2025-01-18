package io.wilski

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object AskPattern {
    suspend fun <Req, Res> ask(
        targetActor: ActorRef<Req>,
        duration: Duration = 500.milliseconds,
        msgFactory: (ref: ActorRef<Res>) -> Req
    ): Deferred<Res> = coroutineScope() {
        val mailbox = Channel<Res>(capacity = Channel.UNLIMITED)
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
}

