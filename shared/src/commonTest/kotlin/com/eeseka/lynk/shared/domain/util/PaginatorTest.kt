package com.eeseka.lynk.shared.domain.util

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PaginatorTest {

    private val requestedKeys = mutableListOf<Int>()
    private val loadingUpdates = mutableListOf<Boolean>()
    private val loadedItems = mutableListOf<String>()
    private var reportedError: Throwable? = null

    private var resultToReturn: Result<List<String>, DataError> = Result.Success(listOf("a", "b"))
    private var nextKeyToReturn: (Int) -> Int = { requestedKey -> requestedKey + 1 }

    @Test
    fun `first load requests the initial key and hands over the items`() = runTest {
        val paginator = createPaginator()

        paginator.loadNextItems()

        assertThat(requestedKeys).containsExactly(1)
        assertThat(loadedItems).containsExactly("a", "b")
        assertThat(reportedError).isNull()
    }

    @Test
    fun `loading is reported on and then off around a request`() = runTest {
        val paginator = createPaginator()

        paginator.loadNextItems()

        assertThat(loadingUpdates).containsExactly(true, false)
    }

    @Test
    fun `next load requests the key the last page pointed to`() = runTest {
        val paginator = createPaginator()

        paginator.loadNextItems()
        paginator.loadNextItems()

        assertThat(requestedKeys).containsExactly(1, 2)
    }

    @Test
    fun `a next key equal to the page just loaded is not requested again`() = runTest {
        nextKeyToReturn = { requestedKey -> requestedKey }
        val paginator = createPaginator()

        paginator.loadNextItems()
        paginator.loadNextItems()

        assertThat(requestedKeys).containsExactly(1)
    }

    @Test
    fun `a failed request reports the error and the same key can be retried`() = runTest {
        resultToReturn = Result.Failure(DataError.Remote.SERVER_ERROR)
        val paginator = createPaginator()

        paginator.loadNextItems()

        assertThat((reportedError as? DataErrorException)?.error).isEqualTo(DataError.Remote.SERVER_ERROR)
        assertThat(loadingUpdates).containsExactly(true, false)

        paginator.loadNextItems()

        assertThat(requestedKeys).containsExactly(1, 1)
    }

    @Test
    fun `a thrown exception is reported through onError`() = runTest {
        val failure = IllegalStateException("boom")
        val paginator = createPaginator(onRequest = { throw failure })

        paginator.loadNextItems()

        assertThat(reportedError).isEqualTo(failure)
        assertThat(loadingUpdates).containsExactly(true, false)
    }

    @Test
    fun `reset starts again from the initial key`() = runTest {
        val paginator = createPaginator()

        paginator.loadNextItems()
        paginator.loadNextItems()
        paginator.reset()
        paginator.loadNextItems()

        assertThat(requestedKeys).containsExactly(1, 2, 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a second load while one is in flight is ignored`() = runTest {
        val response = CompletableDeferred<Result<List<String>, DataError>>()
        val paginator = createPaginator(onRequest = { response.await() })

        launch { paginator.loadNextItems() }
        runCurrent()
        paginator.loadNextItems()

        response.complete(Result.Success(listOf("a")))
        runCurrent()

        assertThat(requestedKeys).containsExactly(1)
        assertThat(loadedItems).containsExactly("a")
    }

    private fun createPaginator(onRequest: suspend (Int) -> Result<List<String>, DataError> = { resultToReturn }) =
        Paginator(
            initialKey = 1,
            onLoadUpdated = { isLoading -> loadingUpdates.add(isLoading) },
            onRequest = { key ->
                requestedKeys.add(key)
                onRequest(key)
            },
            getNextKey = { nextKeyToReturn(requestedKeys.last()) },
            onError = { error -> reportedError = error },
            onSuccess = { items, _ -> loadedItems.addAll(items) }
        )
}
