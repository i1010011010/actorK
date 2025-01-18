package io.wilski

import io.wilski.ActorFlowPattern.actorFlow
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.random.Random


/**
 * Example of using actorFlow.
 * This function is used when we want to get data in real time from the actor system.
 * ActorFlow creates an actor system under the hood.
 * It is useful for the http server when we use Websocket or Server SentEvents.
 * Below is an example of using it in the Ktor framework.
 *
 * ```
 * fun main() {
 *     embeddedServer(Netty, port = 8080, host = 0.0.0.0, module  = Application::module).start(wait = true)
 * }
 *
 * fun Application.configure() {
 *     install(SSE)
 *     install(Sessions)
 *     install(ContentNegotiation) {
 *         json(
 *             Json {
 *                 isLenient = true
 *                 ignoreUnknownKeys = true
 *             }
 *         )
 *     }
 * }
 *
 * fun Application.module() {
 *     configure()
 *     symbolRoute()
 * }
 *
 * fun Application.symbolRoute() = routing {
 *     route("/symbol") {
 *         sse("/sse") {
 *             actorFlow(BrokerConnectorFacade()) { ref ->
 *                 BrokerConnectorFacadeProtocol.GetSymbols(
 *                     call.request.queryParameters["symbols"]?.split(",")?.toSet() ?: emptySet(),
 *                     ref
 *                 )
 *             }.collect {
 *                 val response = Json.encodeToString(StreamSymbolResponseEvent.serializer(), mapper(it))
 *                 log.info("data: $response")
 *                 send(ServerSentEvent(response))
 *             }
 *         }
 *     }
 * }
 *
 * ```
 */
data class Tick(val provider: String, val symbol: String, val price: BigDecimal)

suspend fun main() {
    val logger = LoggerFactory.getLogger("main")
    coroutineScope {
        actorFlow<TickReceiverProtocol, Tick>(TradingAppBackend()) { ref ->
            GetTicks("interactive Brokers", "AAPL", ref)
        }.collect { tick ->
            logger.info("Data provider: ${tick.provider} | Symbol: ${tick.symbol} | Price: $${tick.price}")
        }
    }
}


sealed interface TickReceiverProtocol
data class GetTicks(val provider: String, val symbol: String, val replyTo: ActorRef<Tick>) : TickReceiverProtocol
data class TickStreamData(val provider: String, val symbol: String, val price: BigDecimal) : TickReceiverProtocol
object TradingAppBackend {
    operator fun invoke(): Behavior<TickReceiverProtocol> = setup { ctx ->
        val stockExchangeRef = ctx.spawn(StockExchange())
        receiveMessage { msg ->
            when (msg) {
                is GetTicks -> {
                    stockExchangeRef tell StreamTickCommand(msg.provider, msg.symbol, ctx.self())
                    handleStockExchangeResponse(msg.replyTo)
                }

                is TickStreamData -> error("Wrong message type")
            }
        }
    }
}

fun handleStockExchangeResponse(replyTo: ActorRef<Tick>): Behavior<TickReceiverProtocol> = receiveMessage { tick ->
    when (tick) {
        is GetTicks -> error("Wrong message type")
        is TickStreamData -> {
            replyTo tell Tick(tick.provider, tick.symbol, tick.price)
            same()
        }
    }
}

data class StreamTickCommand(val provider: String, val symbol: String, val replyTo: ActorRef<TickReceiverProtocol>)
object StockExchange {
    operator fun invoke(): Behavior<StreamTickCommand> = setup { ctx ->
        receiveMessage { msg ->
            (0..10000).forEach { index ->
                msg.replyTo tell TickStreamData(msg.provider, msg.symbol, generateRandomStockPrice())
                delay(Random.nextLong(320))
            }
            same()
        }
    }
}

fun generateRandomStockPrice(): BigDecimal {
    val min = BigDecimal("197.00")
    val max = BigDecimal("200.00")
    val range = max.subtract(min)
    val randomDecimal = BigDecimal(Random.nextDouble())
    return min.add(randomDecimal.multiply(range)).setScale(2, RoundingMode.HALF_UP)
}
