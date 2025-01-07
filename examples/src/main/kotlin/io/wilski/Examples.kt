package io.wilski

sealed interface Protocol
sealed interface FirstActorProtocol : Protocol
sealed interface SecondActorProtocol : Protocol
sealed interface ThirdActorProtocol : Protocol
sealed interface FourthActorProtocol : Protocol
data class MessageToFirstActor(val content: String) : FirstActorProtocol
data class ResponseToFirstActor(val content: String) : FirstActorProtocol
data class MessageToSecondActor(val content: String, val self: ActorRef<ResponseToFirstActor>) : SecondActorProtocol
data class ResponseToSecondActor(val content: String) : SecondActorProtocol
data class MessageToThirdActor(val content: String, val self: ActorRef<ResponseToSecondActor>) : ThirdActorProtocol
data class ResponseToThirdActor(val content: String) : ThirdActorProtocol
data class MessageToFourthActor(val content: String, val self: ActorRef<ResponseToThirdActor>) : FourthActorProtocol

data class AuditLoggingProtocol(val changes: String, val timestamp: Long) : Protocol

suspend fun main() {
    actorsKSystem<FirstActorProtocol>(FirstExampleActor()) { guardian ->
        guardian tell MessageToFirstActor("The payload")
    }

}

object FirstExampleActor {
    operator fun invoke(): Behavior<FirstActorProtocol> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is MessageToFirstActor -> {
                    ctx.log().info("[${ctx.name()}] received message: ${msg.content}]")
                    ctx.spawn(SecondActor()).tell(MessageToSecondActor(msg.content, ctx.self()))
                    ctx.log().info("[${ctx.name()}] message sent to second-actor]")
                    same()
                }

                is ResponseToFirstActor -> {
                    ctx.log().info("[${ctx.name()}] received response: ${msg.content}]")
                    same()
                }
            }
        }
    }
}

object SecondActor {
    operator fun invoke(): Behavior<SecondActorProtocol> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is MessageToSecondActor -> {
                    ctx.log().info("[${ctx.name()}] received message: ${msg.content}]")
                    ctx.spawn(ThirdActor()).tell(MessageToThirdActor(msg.content, ctx.self()))
                    ctx.log().info("[${ctx.name()}] message sent to third-actor]")
                    handleMessage(msg)
                }

                else -> {
                    IllegalStateException("")
                    same()
                }
            }
        }
    }

    private fun handleMessage(msg: MessageToSecondActor): Behavior<SecondActorProtocol> = setup { ctx ->
        receiveMessage { resp ->
            when (resp) {
                is ResponseToSecondActor -> {
                    ctx.log().info("[${ctx.name()}] received response: ${msg.content}]")
                    msg.self.tell(ResponseToFirstActor(resp.content))
                    ctx.log().info("[${ctx.name()}] response sent to first-actor")
                    same()
                }

                else -> throw IllegalStateException("message cannot be a ResponseToSecondActor")
            }
        }
    }
}

object ThirdActor {
    operator fun invoke(): Behavior<ThirdActorProtocol> = setup { ctx ->
        receive { ctx, msg ->
            when (msg) {
                is MessageToThirdActor -> {
                    ctx.log().info("[${ctx.name()}] received message: ${msg.content}}")
                    ctx.spawn(FourthActor()) tell MessageToFourthActor(msg.content, ctx.self())
                    handleMessage(msg)
                }

                is ResponseToThirdActor -> throw IllegalStateException("message cannot be a ResponseToThirdActor")
            }
        }
    }

    private fun handleMessage(msg: MessageToThirdActor): Behavior<ThirdActorProtocol> =
        receive { ctx, resp ->
            when (resp) {
                is ResponseToThirdActor -> {
                    ctx.log().info("[${ctx.name()}] received response: ${msg.content}]")
                    msg.self.tell(ResponseToSecondActor(resp.content))
                    ctx.log().info("[${ctx.name()}] response sent to first-actor")
                    same()
                }

                else -> throw IllegalStateException("")
            }
        }
}

object FourthActor {
    operator fun invoke(): Behavior<FourthActorProtocol> = setup { ctx ->
        receiveMessage {
            when (it) {
                is MessageToFourthActor -> {
                    ctx.log().info("[${ctx.name()}] creating response:  RESPONSE")
                    it.self.tell(ResponseToThirdActor("RESPONSE"))
                    ctx.log().info("[${ctx.name()}] response sent to third-actor")
                    stopped {
                        ctx.log().info("[${ctx.name()}] ended work")
                        ctx.spawn(AuditLoggingActor())
                            .tell(AuditLoggingProtocol("RESPONSE", System.currentTimeMillis()))
                    }
                }
            }
        }
    }
}

object AuditLoggingActor {
    operator fun invoke(): Behavior<AuditLoggingProtocol> = setup { ctx ->
        receiveMessage { auditLog ->
            ctx.log().info("[${ctx.name()}] register changes: ${auditLog.changes}, at timestamp: ${auditLog.timestamp}")
            same()
        }
    }
}