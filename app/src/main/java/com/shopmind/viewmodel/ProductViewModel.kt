package com.shopmind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.data.remote.dto.ProductDto
import com.shopmind.data.repository.AuthRepository
import com.shopmind.data.repository.OrderRepository
import com.shopmind.data.repository.ProductRepository
import com.shopmind.recommendation.data.RecommendationRepository
import com.shopmind.recommendation.data.RecommendationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class PurchaseStage {
    STORE, CONFIRMATION, SUCCESS
}

data class ProductUiState(
    val products: List<ProductDto> = emptyList(),
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val cart: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val purchaseStage: PurchaseStage = PurchaseStage.STORE,
    val recommendations: List<ProductEntity> = emptyList(),
    val recommendationType: RecommendationType? = null,
    val isRecommendationsLoading: Boolean = false
) {
    val totalItems: Int get() = cart.values.sum()
    val totalPrice: Double
        get() = cart.entries.sumOf { entry ->
            val price = products.find { it.id == entry.key }?.price
                ?: recommendations.find { it.id == entry.key }?.price
                ?: 0.0
            price * entry.value
        }
}

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeUserAndLoadRecommendations()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val categoriesResult = productRepository.getCategories()
            categoriesResult.onSuccess { categories ->
                val categoryList: MutableList<String> = mutableListOf()
                categories.forEach {
                    categoryList.add(it.name)
                }

                _uiState.value = _uiState.value.copy(
                    categories = listOf("All") + categoryList
                )
            }

            loadProducts("All")
        }
    }

    private fun observeUserAndLoadRecommendations() {
        viewModelScope.launch {
            authRepository.userId.collect { userId ->
                if (userId != null && userId > 0L) {
                    loadRecommendationsForUser(userId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        recommendations = emptyList(),
                        recommendationType = null,
                        isRecommendationsLoading = false
                    )
                }
            }
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            val userId = authRepository.userId.firstOrNull()
            if (userId != null && userId > 0L) {
                loadRecommendationsForUser(userId)
            } else {
                _uiState.value = _uiState.value.copy(
                    recommendations = emptyList(),
                    recommendationType = null,
                    isRecommendationsLoading = false
                )
            }
        }
    }

    private suspend fun loadRecommendationsForUser(userId: Long) {
        _uiState.value = _uiState.value.copy(isRecommendationsLoading = true)
        try {
            val result = recommendationRepository.getRecommendationsForUser(userId)
            _uiState.value = _uiState.value.copy(
                recommendations = result.products,
                recommendationType = result.type,
                isRecommendationsLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isRecommendationsLoading = false
            )
        }
    }

    fun selectCategory(category: String) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadProducts(category)
    }

    private fun loadProducts(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (category == "All") {
                productRepository.getAllProducts()
            } else {
                productRepository.getProductsByCategory(category.lowercase())
            }

            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    products = response.products,
                    isLoading = false,
                    error = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load products"
                )
            }
        }
    }

    fun addToCart(productId: Int) {
        val currentCart = _uiState.value.cart.toMutableMap()
        val count = currentCart.getOrDefault(productId, 0)
        currentCart[productId] = count + 1
        _uiState.value = _uiState.value.copy(cart = currentCart)
    }

    fun removeFromCart(productId: Int) {
        val currentCart = _uiState.value.cart.toMutableMap()
        val count = currentCart.getOrDefault(productId, 0)
        if (count > 1) {
            currentCart[productId] = count - 1
        } else {
            currentCart.remove(productId)
        }
        _uiState.value = _uiState.value.copy(cart = currentCart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cart = emptyMap())
    }

    fun startPurchaseFlow() {
        if (_uiState.value.cart.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(purchaseStage = PurchaseStage.CONFIRMATION)
        }
    }

    fun cancelPurchaseFlow() {
        _uiState.value = _uiState.value.copy(purchaseStage = PurchaseStage.STORE)
    }

    fun confirmPurchase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = authRepository.userId.first()
                if (userId != null) {
                    val currentCart = _uiState.value.cart
                    val productList = _uiState.value.products
                    val recList = _uiState.value.recommendations
                    val items = currentCart.mapNotNull { entry ->
                        val price = productList.find { it.id == entry.key }?.price
                            ?: recList.find { it.id == entry.key }?.price
                        if (price != null) {
                            entry.key to (entry.value to price)
                        } else null
                    }

                    val totalPrice = _uiState.value.totalPrice
                    val result = orderRepository.createOrder(userId, totalPrice, items)
                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            purchaseStage = PurchaseStage.SUCCESS
                        )
                        loadRecommendations()
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to place order"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun completePurchaseFlow() {
        clearCart()
        _uiState.value = _uiState.value.copy(purchaseStage = PurchaseStage.STORE)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
