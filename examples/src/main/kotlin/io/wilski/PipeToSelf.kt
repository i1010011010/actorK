package io.wilski

/**
 * An example of how to use the pipeToSelf() function in an interaction between actors.
 */
suspend fun main() {
    actorsKSystem(Example1(), "example-actor") { sys ->
        sys tell MessageToFirstActor("message to example")
    }
}

object Example1 {
    operator fun invoke(): Behavior<FirstActorProtocol> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is MessageToFirstActor -> {
                    ctx.log().info("${ctx.name()} received message ${msg.content}")
                    ctx.pipeToSelf(ResponseToFirstActor(msg.content))
                    ctx.log().info("${ctx.name()} sent message to self ${msg.content}")
                    same()
                }

                is ResponseToFirstActor -> {
                    ctx.log().info("${ctx.name()} received response from self: ${msg.content}")
                    ctx.spawn(Example2()) tell MessageToSecondActor("message to example2", ctx.self())
                    same()
                }

                FirstActorTerminationSignal -> {
                    ctx.log().info("${ctx.name()} received termination signal")
                    stopped {
                        ctx.spawn(AuditLoggingActor()) tell AuditLoggingProtocol(
                            "actor ${ctx.name()} terminated",
                            System.currentTimeMillis()
                        )
                    }
                }
            }
        }
    }
}

object Example2 {
    operator fun invoke(): Behavior<SecondActorProtocol> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is MessageToSecondActor -> {
                    ctx.log().info("${ctx.name()} received message ${msg.content}")
                    ctx.pipeToSelf(ResponseToSecondActor(msg.content, msg.replyTo))
                    ctx.log().info("${ctx.name()} sent message to self ${msg.content}")
                    same()
                }

                is ResponseToSecondActor -> {
                    ctx.log().info("${ctx.name()} received response from self: ${msg.content}")
                    ctx.spawn(Example3()) tell MessageToThirdActor("message to example3", msg.replyTo)
                    same()
                }
            }
        }
    }
}

object Example3 {
    operator fun invoke(): Behavior<ThirdActorProtocol> = setup { ctx ->
        receiveMessage { msg ->
            when (msg) {
                is MessageToThirdActor -> {
                    ctx.log().info("${ctx.name()} received message ${msg.content}")
                    ctx.pipeToSelf(ResponseToThirdActor(msg.content, msg.replyTo))
                    ctx.log().info("${ctx.name()} sent message to self ${msg.content}")
                    same()
                }

                is ResponseToThirdActor -> {
                    ctx.log().info("${ctx.name()} received response from self: ${msg.content}")
                    msg.replyTo tell FirstActorTerminationSignal
                    same()
                }
            }
        }
    }
}

private object AuditLoggingActor {
    operator fun invoke(): Behavior<AuditLoggingProtocol> = setup { ctx ->
        receiveMessage { auditLog ->
            ctx.log().info("[${ctx.name()}] register changes: ${auditLog.changes}, at timestamp: ${auditLog.timestamp}")
            same()
        }
    }
}
