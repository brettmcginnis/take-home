package com.brettmcgin.takehome.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brettmcgin.takehome.mvi.StateSharingBehavior.Reset
import com.brettmcgin.takehome.mvi.ViewState.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


abstract class StateViewModel<Intent, Data>(
    private val initialState: ViewState<Data> = Empty(),
    private val sharingBehavior: StateSharingBehavior<Data> = Reset(),
) : ViewModel() {

    private val intentFlow = MutableSharedFlow<Intent>()

    val state: StateFlow<ViewState<Data>> by lazy {
        transformIntents(intentFlow)
            .catch { error -> throw IllegalStateException("transformIntents emitted an error", error) }
            .onCompletion { cause ->
                check(cause != null) { "transformIntents completed unexpectedly" }
            }
            .stateIn(viewModelScope, sharingBehavior(this), initialState)
    }

    protected fun emitIntent(intent: Intent) {
        state; viewModelScope.launch(Dispatchers.Main) { intentFlow.emit(intent) }
    }

    protected abstract fun transformIntents(intentFlow: Flow<Intent>): Flow<ViewState<Data>>
}

