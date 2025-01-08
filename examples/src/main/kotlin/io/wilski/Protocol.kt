package io.wilski

sealed interface Protocol

sealed interface FirstActorProtocol : Protocol
sealed interface SecondActorProtocol : Protocol
sealed interface ThirdActorProtocol : Protocol

data class MessageToFirstActor(val content: String) : FirstActorProtocol
data class ResponseToFirstActor(val content: String) : FirstActorProtocol
data object FirstActorTerminationSignal: FirstActorProtocol
data class MessageToSecondActor(val content: String, val replyTo: ActorRef<FirstActorTerminationSignal>) : SecondActorProtocol
data class ResponseToSecondActor(val content: String, val replyTo: ActorRef<FirstActorTerminationSignal>) : SecondActorProtocol
data class MessageToThirdActor(val content: String, val replyTo: ActorRef<FirstActorTerminationSignal>) : ThirdActorProtocol
data class ResponseToThirdActor(val content: String, val replyTo: ActorRef<FirstActorTerminationSignal>) : ThirdActorProtocol
data class AuditLoggingProtocol(val changes: String, val timestamp: Long) : Protocol
