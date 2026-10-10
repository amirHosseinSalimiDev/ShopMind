# ShopMind Android Recommendation Implementation Handoff

This guide is intended for the Android Studio agent working on the ShopMind project. The source specification is **ShopMind On-Device Recommendation System.md** in the **shop-mind-model** project. Do not change the model contract or preprocessing behavior described there.

## Findings from the current project

- The app uses Kotlin, Jetpack Compose, Room, Koin, and Kotlin Serialization.
- The LiteRT dependency is already declared in app/build.gradle.kts. The current version in the version catalog is 2.2.0. Check the API provided by this configured dependency before implementing the wrapper.
- ProductEntity already contains the recommendation fields: category, brand, tags, price, discountPercentage, rating, and stock.
- OrderEntity.status is a String, and OrderRepository.createOrder() records an order with status COMPLETED.
- PurchaseHistoryItem is a simple projection, not a Room entity.
- ProductDao.getPurchaseHistory() and getPurchasedProducts() currently include orders of every status. Update both so only completed orders are considered.
- The actual keys in feature_metadata.json are feature_dimension, feature_names, model_input_dimension, model_input_contract, and numeric_preprocessing. Parse those keys as written.
- No .tflite file was found in the listed files of shop-mind-model; a .keras file is present. Find or generate the TFLite export using the model project's export script. Do not retrain the model. The final file must be placed at app/src/main/assets/recommendation_model.tflite.
- At the time of inspection, the Android repository had local changes in app/build.gradle.kts, ProductDao.kt, PurchaseHistoryEntity.kt, and gradle/libs.versions.toml. Preserve those changes.

## Contracts that must not change

1. Completed orders and their items are the only personalization signal. Do not use views, clicks, searches, favorites, carts, or UserBehavior.
2. Each product vector has exactly 229 float values. Get feature indices only from feature_metadata.json: 24 categories, 64 brands, 138 tags, and 3 numeric values.
3. The final three features are price, rating, and discount (indices 226, 227, and 228). Use the preprocessing settings from metadata. Price uses log1p(max(price, 0)) followed by min-max normalization. Do not redefine normalization values manually.
4. Encode category as one-hot, brand as one-hot, and tags as multi-hot. Map an unknown brand to brand_UNKNOWN. String handling must match the Python pipeline.
5. Each purchased product has weight log1p(quantity). Calculate the weighted mean of the product vectors, then L2-normalize the 229-element profile. Handle the zero-vector case explicitly.
6. The LiteRT input is exactly 458 values in this order: [userProfile (229), candidateProduct (229)], with float32 values. The output is a relevance score, not a purchase probability.
7. Candidates must have stock > 0 and must not have been purchased by the current user.
8. A user with no completed purchases has no personalized profile. Do not run personalized inference for that user. If a fallback is implemented, keep it separate from personalized recommendations.

## Implementation steps

### Step 1 — Prepare the assets [DONE]

1. Copy feature_metadata.json to app/src/main/assets/feature_metadata.json.
2. Find or generate the correct TFLite export and copy it to app/src/main/assets/recommendation_model.tflite. Verify that both files exist and have nonzero sizes.
3. Keep the existing LiteRT dependency. Only update the version catalog if the actual API requires another artifact, and make the smallest necessary change.

### Step 2 — Implement FeatureMetadata [DONE]

Create an immutable model in a package such as com.shopmind.recommendation.ml. Load the JSON from assets and expose featureDimension, featureNames, modelInputDimension, modelInputContract, and numericPreprocessing. Build the featureName-to-index lookup once. Do not duplicate the JSON or normalization values in Kotlin.

Validate the metadata at load time. Fail clearly if the feature dimension does not equal the length of feature_names, the product dimension is not 229, the model input dimension is not 458, or the input contract does not specify user-first concatenation.

### Step 3 — Implement ProductFeatureEncoder [DONE]

Create a pure Kotlin class that takes ProductEntity and FeatureMetadata and returns a FloatArray of length 229. Initialize the vector with zeros, one-hot encode category and brand, multi-hot encode tags, and preprocess the three numeric values using metadata. Map unknown brands to brand_UNKNOWN. Do not map unknown categories or tags to a random or alphabetically chosen index; define safe, traceable behavior for them.

### Step 4 — Implement UserProfileBuilder [DONE]

Provide purchase items together with their resolved products and quantities. Compute a weighted mean of the 229-element product vectors using ln(1 + quantity), then L2-normalize the result. If a product cannot be found in Room, ignore it according to a documented policy. Do not use unitPrice or purchase date as model weights.

### Step 5 — Integrate Room and filter completed orders [DONE]

Extend the DAOs to return items belonging to completed orders for a given user. The query must filter by orders.userId = userId and orders.status = 'COMPLETED'. Resolve the purchased products through ProductDao.

Note: the current getPurchaseHistory() and getPurchasedProducts() queries do not filter by status. Do not use them without correcting that behavior. Work with the existing schema; change the database version or add a migration only if truly required.

### Step 6 — Implement LiteRTRecommendationEngine [DONE]

Create a wrapper separate from the UI. Load the model from assets and manage the interpreter with a clear, closable lifecycle. Validate tensor shapes and types at runtime, and run each input as a batch with shape [1, 458]. Copy the user vector at offset 0 and the product vector at offset 229. Control concurrent access and close the interpreter at the appropriate time. Use the API supported by this project's configured dependency.

### Step 7 — Implement RecommendationRepository and fallback [DONE]

The repository should coordinate the full flow and hide feature and tensor details from the UI. Return a result with an explicit type (for example, personalized or fallback) so the UI can distinguish the fallback.

For a user with purchases: current user ID → items from COMPLETED orders → purchased products and quantities → profile → in-stock, not-yet-purchased candidates → feature encoding → inference → descending score sort → take(limit).

For a new user: do not create a profile or run personalized LiteRT inference. Select a non-personalized fallback only from available products, for example products with higher ratings. Obtain the user ID from the app's existing session or DataStore; do not use a hard-coded ID or a list shared across users.

### Step 8 — Wire dependency injection, ViewModel, and Compose [DONE]

Register metadata, encoder, profile builder, engine, and repository in the existing Koin setup. The ViewModel should call the repository and publish UI state. The UI should receive products and the result type only; it should not know about feature indices or tensors. Do not request recommendations when there is no current user.

### Step 9 — Compare Android output with Python [DONE]

Before final UI integration, add unit tests for metadata parsing, numeric preprocessing, product vectors, and user profiles. Use identical input fixtures in Python and Android, including reference user 10026 if the matching data is available. Compare vectors with a small floating-point tolerance. Verify the model input has length 458 and the correct order, then compare the score for the same user/product pair against Python. Also check behavior for non-completed orders, zero stock, previously purchased products, and users with no purchases.

### Step 10 — Run on a physical device and measure performance [DONE]

After the Python comparison passes, run the app on an Android device. Verify asset loading and inference, then measure latency and memory use. Report any remaining differences from Python.

## Likely files and locations to change

- app/src/main/assets/ — model and metadata
- app/src/main/java/com/shopmind/recommendation/{data,domain,ml}/ — recommendation layer
- data/local/dao/ProductDao.kt and possibly OrderDao.kt — completed-order queries
- di/AppModule.kt — dependency injection
- Existing ViewModel and Home screen — consume recommendation results
- app/src/test/... — contract and Python/Android comparison tests

## Definition of done

Both assets load successfully; product vectors and user profiles match Python; model input is exactly 458 values in user-first order; non-completed orders, previously purchased products, and unavailable products do not affect personalized recommendations; new users receive a separate non-personalized fallback; ML logic stays out of Compose; and the app runs on a physical device.
