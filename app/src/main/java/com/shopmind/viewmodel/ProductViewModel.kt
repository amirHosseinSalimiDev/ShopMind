package com.shopmind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopmind.data.remote.dto.ProductDto
import com.shopmind.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<ProductDto> = emptyList(),
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val cart: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val totalItems: Int get() = cart.values.sum()
    val totalPrice: Double get() = cart.entries.sumOf { entry ->
        val product = products.find { it.id == entry.key }
        (product?.price ?: 0.0) * entry.value
    }
}

class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val categoriesResult = productRepository.getCategories()
            categoriesResult.onSuccess { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = listOf("All") + categories
                )
            }

            loadProducts("All")
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
}
