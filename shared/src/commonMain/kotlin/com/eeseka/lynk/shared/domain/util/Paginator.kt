package com.eeseka.lynk.shared.domain.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class Paginator<Key, Item>(
    private val initialKey: Key,
    private val onLoadUpdated: (Boolean) -> Unit,
    private val onRequest: suspend (nextKey: Key) -> Result<List<Item>, DataError>,
    private val getNextKey: suspend (List<Item>) -> Key,
    private val onError: suspend (Throwable?) -> Unit,
    private val onSuccess: suspend (items: List<Item>, newKey: Key) -> Unit
) {
    private var currentKey = initialKey
    private var isMakingRequest = false
    private var lastRequestKey: Key? = null
    private var isClosed = false

    suspend fun loadNextItems() {
        if (isMakingRequest || isClosed) {
            return
        }

        if (currentKey != null && currentKey == lastRequestKey) {
            return
        }

        isMakingRequest = true
        onLoadUpdated(true)

        try {
            val result = onRequest(currentKey)
            // A replaced paginator's late answer would land in the list that replaced it
            if (isClosed) return

            result
                .onSuccess { items ->
                    val newKey = getNextKey(items)
                    onSuccess(items, newKey)
                    lastRequestKey = currentKey

                    currentKey = newKey
                }
                .onFailure { error ->
                    onError(DataErrorException(error))
                }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            if (!isClosed) onError(e)
        } finally {
            if (!isClosed) onLoadUpdated(false)
            isMakingRequest = false
        }
    }

    fun reset() {
        currentKey = initialKey
        lastRequestKey = null
    }

    fun close() {
        isClosed = true
    }
}