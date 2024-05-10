package com.brettmcgin.takehome.mvi

import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingCommand.STOP
import kotlinx.coroutines.flow.SharingCommand.STOP_AND_RESET_REPLAY_CACHE
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map


internal const val DEFAULT_STOP_TIMEOUT_MS = 3_000L

fun interface StateSharingBehavior<Data> {

    operator fun invoke(viewModel: StateViewModel<*, Data>): SharingStarted

    class Reset<Data>(
        private val disconnectDelayMs: Long = DEFAULT_STOP_TIMEOUT_MS,
        private val resetPredicate: ViewState<Data>.() -> Boolean = { true }
    ) : StateSharingBehavior<Data> {
        override fun invoke(viewModel: StateViewModel<*, Data>) = SharingStarted { subscriptionCount ->
            WhileSubscribed(disconnectDelayMs, replayExpirationMillis = 0)
                .command(subscriptionCount)
                .map { command -> mapCommand(command, viewModel.state.value) }
        }

        private fun mapCommand(command: SharingCommand, currentState: ViewState<Data>): SharingCommand {
            if (command != STOP_AND_RESET_REPLAY_CACHE || resetPredicate(currentState)) return command

            currentState.markStale()
            return STOP
        }
    }
}

