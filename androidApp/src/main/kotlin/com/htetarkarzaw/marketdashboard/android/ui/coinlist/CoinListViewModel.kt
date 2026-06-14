package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.DismissError
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.LoadInitial
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.ReachedEnd
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.Refresh
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Error
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Loading
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Success
import com.htetarkarzaw.marketdashboard.android.ui.model.toUiModel
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CoinListViewModel(
    private val getCoinsUseCase: GetCoinsUseCase,
    private val refreshCoinsUseCase: RefreshCoinsUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<CoinListUiState> = MutableStateFlow(Loading)
    val uiState: StateFlow<CoinListUiState> = _uiState.asStateFlow()

    private val pageSize = 20

    fun onIntent(intent: CoinListIntent) {
        when (intent) {
            is LoadInitial -> loadInitial()
            is Refresh -> refresh()
            is ReachedEnd -> {
                val current = _uiState.value as? Success ?: return
                if (current.hasReachedEnd || current.isLoadingMore) return
                loadNextPage()
            }
            is DismissError -> dismissError()
        }
    }

    private fun loadInitial() {
        _uiState.value = Loading
        viewModelScope.launch {
            try {
                refreshCoinsUseCase()
                getCoinsUseCase(page = 0, pageSize = pageSize)
                    .catch { e -> _uiState.value = Error(e.message ?: "Unknown error") }
                    .collect { coins ->
                        _uiState.value = Success(coins = coins.map { it.toUiModel() })
                    }
            } catch (e: Exception) {
                _uiState.value = Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            try {
                refreshCoinsUseCase()
                getCoinsUseCase(page = 0, pageSize = pageSize)
                    .catch { e ->
                        val currentCoins = (_uiState.value as? Success)?.coins ?: emptyList()
                        _uiState.value = Success(coins = currentCoins, errorMessage = e.message)
                    }
                    .collect { coins ->
                        _uiState.value = Success(coins = coins.map { it.toUiModel() }, hasReachedEnd = false)
                    }
            } catch (e: Exception) {
                val currentCoins = (_uiState.value as? Success)?.coins ?: emptyList()
                _uiState.value = Success(coins = currentCoins, errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    private fun loadNextPage() {
        val currentState = _uiState.value as? Success ?: return
        val nextPage = currentState.coins.size / pageSize
        _uiState.value = currentState.copy(isLoadingMore = true)
        viewModelScope.launch {
            getCoinsUseCase(page = nextPage, pageSize = pageSize)
                .catch { e ->
                    val currentCoins = (_uiState.value as? Success)?.coins ?: emptyList()
                    _uiState.value = Success(coins = currentCoins, errorMessage = e.message)
                }
                .collect { newCoins ->
                    val current = _uiState.value as? Success ?: return@collect
                    if (newCoins.isEmpty()) {
                        _uiState.value = current.copy(isLoadingMore = false, hasReachedEnd = true)
                        return@collect
                    }
                    _uiState.value = Success(coins = current.coins + newCoins.map { it.toUiModel() })
                }
        }
    }

    private fun dismissError() {
        val currentState = _uiState.value as? Success ?: return
        _uiState.value = currentState.copy(errorMessage = null)
    }
}
