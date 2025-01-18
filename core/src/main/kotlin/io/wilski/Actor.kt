package io.wilski

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield

internal interface Actor<T> {
    suspend fun run(initialBehavior: Behavior<T>)
}

internal fun <T> actor(name: String, mailbox: Channel<T>, job: Job, scope: CoroutineScope) = object : Actor<T> {
    private val ctx = actorContext(name, actorRef(mailbox), job, scope)
    override suspend fun run(initialBehavior: Behavior<T>) = let {
        while (true) {
            when (initialBehavior) {
                is Setup -> {
                    val newBehavior = initialBehavior.init(ctx)
                    ctx.log().debug("actor ${ctx.name()} created") //FIXME: Zmienić na debug i dodać obsługę ustawień
                    nextBehavior(newBehavior, initialBehavior)
                }

                is Receive -> {
                    val msg = mailbox.receive()
                    val newBehavior = initialBehavior.receive(ctx, msg)
                    nextBehavior(newBehavior, initialBehavior)
                }

                is Stopped -> {
                    mailbox.close()
                    ctx.log().debug("$name has been stopped.")
                    job.cancelAndJoin()
                }

                is StoppedWithEffect -> {
                    val newBehavior: Behavior<T> = initialBehavior.postStop(ctx)
                    mailbox.close()
                    ctx.log().debug("$name has been stopped with effect.")
                    job.cancelAndJoin()
                    nextBehavior(newBehavior, initialBehavior)
                }

                is Same -> error("The INSTANCE of Same is illegal")
                is BehaviorBuilder<*> -> error("The INSTANCE of BehaviorBuilder is illegal")
            }
        }
        yield()
    }

    private suspend fun Actor<T>.nextBehavior(newBehavior: Behavior<T>, behavior: Behavior<T>) = let {
        when (newBehavior) {
            is Same -> run(behavior)
            else -> run(newBehavior)
        }
    }
}
