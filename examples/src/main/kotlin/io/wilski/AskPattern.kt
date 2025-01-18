package io.wilski

import kotlin.time.Duration.Companion.milliseconds


/**
 * An example of the Request-Response pattern using the ask method.
 * This pattern differs from the one using the tell function in that it is called by the ActorContext
 * and allows you to send only one message, waiting for a response within a specified time.
 * The default waiting time for a response is 500 ms.
 */
suspend fun main() {
    actorsKSystem(AskExample1(), "ask-system") { ctx -> ctx tell MessageToFirstActor("Hello world!") }
}

object AskExample1 {
    operator fun invoke(): Behavior<FirstActorProtocol> = setup { ctx ->
        val behavior = ctx.spawn(AskExample2())
        receiveMessage { msg ->
            when (msg) {
                FirstActorTerminationSignal -> {
                    ctx.log().info("$msg")
                    same()
                }

                is MessageToFirstActor -> {
                    ctx.ask(behavior, 500.milliseconds) { replyTo -> AskToSecondActor("Hi", replyTo) }
                    same()
                }

                is ResponseToFirstActor -> {
                    ctx.log().info("AskExample1.ResponseToFirstActor received response ${msg.content}")
                    stopped()
                }
            }
        }
    }
}

object AskExample2 {
    operator fun invoke(): Behavior<SecondActorProtocol> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is AskToSecondActor -> {
                    ctx.log().info("AskExample2 receiver ask: ${msg.content}")
                    val msg1 = ResponseToFirstActor("AskExample2 sent response")
                    msg.replyTo.tell(msg1)
                    ctx.log().info("AskExample2 sent response: ${msg1.content}")
                    same()
                }

                is MessageToSecondActor -> TODO()
                is ResponseToSecondActor -> TODO()
            }
        }
    }
}
