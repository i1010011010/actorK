package io.wilski


sealed interface MessagesToTheCustomer
data object StartConversation : MessagesToTheCustomer
data class SellerAnswer(val answer: String) : MessagesToTheCustomer

/**
 * Example of the Request-Response pattern.
 * The Request-Response pattern is a way of
 * communication in which the sender sends a message to an actor and expects a response.
 * In ActorK, this is done by sending a message using the tell(msg) method.
 * Msg can be any immutable object that, in addition to the message,
 * sends a reference to the sending actor as an ActorRef type.
 *
 * Example of sending object:
 * ```
 * data class GetAnswer(val question: String, val replyTo: ActorRef<AnswerToTheQuestion>)
 * ```
 */
suspend fun main() {
    actorsKSystem(Customer(), "customer-seller-interaction-system") { ref ->
        ref tell StartConversation
    }
}

object Customer {
    operator fun invoke(): Behavior<MessagesToTheCustomer> = setup { ctx ->
        val sellerRef = ctx.spawn(Seller(), "seller")
        receiveMessage { msg ->
            when (msg) {
                StartConversation -> {
                    val askForPrice = AskForPrice("Hi, how much does this laptop cost?", ctx.self())
                    sellerRef tell askForPrice
                    ctx.log().info(askForPrice.question)
                    same()
                }

                is SellerAnswer -> {
                    ctx.log().info("damn, I don't have that much money, the customer thought")
                    stopped()
                }
            }
        }
    }
}

sealed interface MessagesToSeller
data class AskForPrice(val question: String, val replyTo: ActorRef<MessagesToTheCustomer>) : MessagesToSeller
object Seller {
    operator fun invoke(): Behavior<MessagesToSeller> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is AskForPrice -> {
                    val sellerAnswer = SellerAnswer("Hi, this laptop costs \$2500.")
                    msg.replyTo tell sellerAnswer
                    ctx.log().info(sellerAnswer.answer)
                    stopped()
                }
            }
        }
    }
}



