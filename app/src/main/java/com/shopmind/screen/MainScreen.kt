package com.shopmind.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.shopmind.data.local.entity.ProductEntity
import com.shopmind.data.remote.dto.ProductDto
import com.shopmind.recommendation.data.RecommendationType
import com.shopmind.ui.theme.indigo600
import com.shopmind.viewmodel.ProductViewModel
import com.shopmind.viewmodel.PurchaseStage
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun MainScreen(
    paddingValues: PaddingValues,
    viewModel: ProductViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            bottomBar = {
                PurchaseBottomBar(
                    totalItems = uiState.totalItems,
                    totalPrice = uiState.totalPrice,
                    onPurchaseClick = { viewModel.startPurchaseFlow() }
                )
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 24.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header (App Title & Logout)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ShopMind",
                            color = indigo600,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = { viewModel.logout() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Logout",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Category List
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryList(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }

                // Recommendations Section
                if (uiState.recommendations.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RecommendationSection(
                            recommendations = uiState.recommendations,
                            recommendationType = uiState.recommendationType,
                            cart = uiState.cart,
                            onAdd = { viewModel.addToCart(it) },
                            onRemove = { viewModel.removeFromCart(it) }
                        )
                    }
                }

                // Products Title
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = if (uiState.selectedCategory == "All") "All Products" else "${uiState.selectedCategory} Products",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Products Grid or Loading State
                if (uiState.isLoading && uiState.products.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = indigo600)
                        }
                    }
                } else {
                    items(
                        items = uiState.products,
                        key = { it.id }
                    ) { product ->
                        ProductCard(
                            product = product,
                            quantity = uiState.cart.getOrDefault(product.id, 0),
                            onAdd = { viewModel.addToCart(product.id) },
                            onRemove = { viewModel.removeFromCart(product.id) }
                        )
                    }
                }
            }
        }

        // Overlays for Purchase Stages
        if (uiState.purchaseStage == PurchaseStage.CONFIRMATION) {
            ConfirmPurchaseSheet(
                paddingValues = paddingValues,
                cart = uiState.cart,
                products = uiState.products,
                recommendations = uiState.recommendations,
                totalPrice = uiState.totalPrice,
                onConfirm = { viewModel.confirmPurchase() },
                onDismiss = { viewModel.cancelPurchaseFlow() }
            )
        }

        if (uiState.purchaseStage == PurchaseStage.SUCCESS) {
            PurchaseSuccessDialog(
                onBackToStore = { viewModel.completePurchaseFlow() }
            )
        }
    }
}

@Composable
fun RecommendationSection(
    recommendations: List<ProductEntity>,
    recommendationType: RecommendationType?,
    cart: Map<Int, Int>,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    if (recommendations.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recommended for You",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            if (recommendationType != null) {
                val label = if (recommendationType == RecommendationType.PERSONALIZED) "Personalized" else "Popular"
                val bgColor = if (recommendationType == RecommendationType.PERSONALIZED) Color(0xFFEEF2FF) else Color(0xFFFEF3C7)
                val textColor = if (recommendationType == RecommendationType.PERSONALIZED) indigo600 else Color(0xFFD97706)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(
                items = recommendations,
                key = { it.id }
            ) { product ->
                RecommendationCard(
                    product = product,
                    quantity = cart.getOrDefault(product.id, 0),
                    onAdd = { onAdd(product.id) },
                    onRemove = { onRemove(product.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(
    product: ProductEntity,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$${String.format(Locale.US, "%.2f", product.price)}",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (quantity == 0) {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color(0xFFEEF2FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = indigo600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp))
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(text = quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(24.dp)
                            .background(indigo600, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add More",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryList(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) indigo600 else Color.White,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.LightGray
                )
            ) {
                Text(
                    text = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductDto,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$${String.format(Locale.US, "%.2f", product.price)}",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (quantity == 0) {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color(0xFFEEF2FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = indigo600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp))
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(text = quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .background(indigo600, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add More",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseBottomBar(
    totalItems: Int,
    totalPrice: Double,
    onPurchaseClick: () -> Unit
) {
    AnimatedVisibility(
        visible = totalItems > 0,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$totalItems items selected",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }

                Button(
                    onClick = onPurchaseClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = indigo600),
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(text = "Purchase", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ConfirmPurchaseSheet(
    paddingValues: PaddingValues,
    cart: Map<Int, Int>,
    products: List<ProductDto>,
    recommendations: List<ProductEntity>,
    totalPrice: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color(0xFFCCCCCC), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Confirm Purchase",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Cart Items list
                cart.entries.forEach { entry ->
                    val title = products.find { it.id == entry.key }?.title
                        ?: recommendations.find { it.id == entry.key }?.title
                    val price = products.find { it.id == entry.key }?.price
                        ?: recommendations.find { it.id == entry.key }?.price
                    if (title != null && price != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${entry.value}x $title",
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "$${
                                    String.format(
                                        Locale.US,
                                        "%.2f",
                                        price * entry.value
                                    )
                                }",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Price",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", totalPrice)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = indigo600
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = indigo600)
                ) {
                    Text(
                        text = "Confirm Purchase",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseSuccessDialog(
    onBackToStore: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Checkmark Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFEEF2FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = indigo600,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Purchase Successful",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your pickup code is ready on the campus dashboard.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onBackToStore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = indigo600)
                ) {
                    Text(
                        text = "Back to Store",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}