package io.wilski

data object Start
data class SendMessage(val msg: String)

/**
 * Examples of Fire and Forget Pattern.
 * Fire and Forget is a communication pattern where the sender sends a message to an actor
 * but does not wait for any response or confirmation that the message has been processed.
 * In ActorK, this is done using the tell(msg) method.
 */
suspend fun main() {
    actorsKSystem(MessageSender(), "fire-and-forget-system") { ctx ->
        ctx tell Start
    }
}

object MessageSender {
    operator fun invoke(): Behavior<Start> = setup { ctx ->
        val receiverRef = ctx.spawn(MessageReceiver(), "receiver")
        receiveMessage { msg ->
            receiverRef tell SendMessage("Hi, i'm sending message to you")
            same()
        }
    }
}

object MessageReceiver {
    operator fun invoke(): Behavior<SendMessage> = setup { ctx ->
        receiveMessage { msg ->
            ctx.log().info("Received message: $msg")
            stopped()
        }
    }
}