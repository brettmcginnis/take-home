package com.brettmcgin.takehome.mvi

sealed class ViewState<out D>(open val data: D? = null, open val error: Throwable? = null) {
    class Empty<D> : ViewState<D>()
    data class Error<D>(override val error: Throwable, override val data: D? = null) : ViewState<D>()

    @Suppress("DataClassShouldBeImmutable") // Internally mutable on purpose
    data class Data<D>(override val data: D, internal var stale: Boolean = false) : ViewState<D>() {
        val isStale get() = stale
    }

    /**
     * If the state is [Data], marks it as stale and [Data.isStale] will return true.
     */
    internal fun markStale() {
        if (this is Data) stale = true
    }
}

