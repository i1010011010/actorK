package io.wilski

import kotlin.time.Duration.Companion.seconds

/**
 * An example of the Request-Response pattern using the askPattern method from outside the actor system.
 * This pattern differs from the pattern using the ActorContext in that it can be used from outside the actor system
 * and returns a value of type Deferred<T>.
 * It is useful for the http server when calling REST endpoints.
 * A good example of its use is the framework ktor.
 * Example below:
 * ```
 * fun main() {
 *     embeddedServer(Netty, port = 8080, host = 0.0.0.0, module  = Application::module).start(wait = true)
 * }
 * fun Application.module() {
 *     configure()
 *     symbolRoute()
 *     staticResourceRoutes()
 * }
 *
 * fun Application.symbolRoute() = routing {
 *     route("/symbol") {
 *         get("/all") {
 *             call.respond(
 *                 HttpStatusCode.OK, askPattern(BrokerConnectorFacade(), 2.seconds)
 *                 { ref -> BrokerConnectorFacadeProtocol.GetSignatures(ref)
 *                 }.await()
 *                     .signatures
 *             )
 *         }
 *     }
 * }
 *
 * ```
 */

suspend fun main() {
    println(QuestionerFromOutsideTheActorSystem.ask())
}

@JvmInline
value class Answer(val answer: String)
object QuestionerFromOutsideTheActorSystem {
    suspend fun ask(): Answer =
        askPattern<QuestionFromOutsideTheActorSystem, Answer>(actorsKSystem(AnswerActor()), 2.seconds)
        { replyTo: ActorRef<Answer> -> QuestionFromOutsideTheActorSystem("I'm asking to you!", replyTo) }.await()
}


data class QuestionFromOutsideTheActorSystem(val ask: String, val ref: ActorRef<Answer>)
object AnswerActor {
    operator fun invoke(): Behavior<QuestionFromOutsideTheActorSystem> = setup { ctx ->
        receiveMessage { msg ->
            msg.ref tell Answer("NO!!!")
            same()
        }
    }
}