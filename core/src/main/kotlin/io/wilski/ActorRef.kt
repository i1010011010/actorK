package io.wilski

import kotlinx.coroutines.channels.SendChannel

/**
 * Receives a message of a certain type. Wraps a coroutine SendChannel as a mailbox
 */
interface ActorRef<in T> {

    /**
     * function pushes typed message to a mailbox (coroutine SendChannel())
     * @param msg
     * @return Unit
     */
    suspend infix fun tell(msg: T)
    fun <U : T> unsafeUpcast(): ActorRef<U>
}

/**
 * Create ActorRef type
 * @param mailbox
 * @return ActorRef instance
 */
internal fun <T> actorRef(mailbox: SendChannel<T>): ActorRef<T> = object : ActorRef<T> {
    override suspend fun tell(msg: T) = mailbox.send(msg)

    @Suppress("UNCHECKED_CAST")
    override fun <U : T> unsafeUpcast(): ActorRef<U> = this as ActorRef<U>
}