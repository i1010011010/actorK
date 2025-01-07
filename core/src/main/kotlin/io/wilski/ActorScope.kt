package io.wilski

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal fun <T> createActorScope(
    behavior: Behavior<T>,
    name: String,// FIXME: Find more elegant solution for default actor names
    scope: CoroutineScope,
    context: CoroutineContext,
    mailbox: Channel<T> = Channel<T>(capacity = Channel.UNLIMITED),
): ActorRef<T> =
    scope.launch(context)
    { actor(name, mailbox, coroutineContext.job, scope).run(behavior) }.let { actorRef(mailbox) }