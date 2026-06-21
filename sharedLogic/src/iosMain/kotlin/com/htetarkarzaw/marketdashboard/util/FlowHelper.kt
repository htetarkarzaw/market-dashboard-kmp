package com.htetarkarzaw.marketdashboard.util

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun <T> observe(
    flow: Flow<T>,
    onUpdate: (T) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    scope.launch {
        try {
            flow.collect { value -> onUpdate(value) }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }
    return { scope.cancel() }
}

// Kotlin generics erase to Any in ObjC/Swift, so this typed wrapper preserves type safety
// for iOS callers while delegating to the generic observe() internally.
fun observeCoins(
    useCase: GetCoinsUseCase,
    page: Int = 0,
    pageSize: Int = 20,
    onUpdate: (List<Coin>) -> Unit,
    onError: (String) -> Unit
): () -> Unit = observe(
    flow = useCase(page = page, pageSize = pageSize),
    onUpdate = onUpdate,
    onError = onError
)
