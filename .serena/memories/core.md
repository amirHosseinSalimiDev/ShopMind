# ShopMind project map

ShopMind is an Android shopping application built with Kotlin, Jetpack Compose, Room, Koin, Kotlin Serialization, and LiteRT. Read `Android_Recommendation_Implementation_Handoff.md` before changing recommendation behavior; it is the detailed integration contract. `Implementation_Plan.md` describes the planned work. `graphify-out/GRAPH_REPORT.md` is the generated navigation map when present.

## Main application areas

- App entry and navigation: `MainActivity.kt`, `screen/App.kt`, `screen/MainScreen.kt`, and `screen/authentication/AuthNavHost.kt`.
- Authentication screens include login and signup under `screen/authentication/`.
- Persistence and data access use Room entities, DAOs, repositories, and the `AppDatabase` setup under `data/`.
- Dependency wiring is in `di/AppModule.kt` and uses Koin.
- UI is implemented with Compose; ViewModels publish state for screens.
- Recommendation code is separated into ML, domain, and data responsibilities. Key symbols include `ProductFeatureEncoder`, `UserProfileBuilder`, `LiteRTRecommendationEngine`, and `RecommendationRepositoryImpl`.

## Recommendation contract

- Personalization uses only items from orders whose status is `COMPLETED`.
- Product feature vectors have exactly 229 float values: 24 categories, 64 brands, 138 tags, and 3 numeric values.
- Feature order and numeric scaling come from the model's `feature_metadata.json`; do not duplicate or reorder indices in Kotlin.
- Model input is exactly 458 float32 values in this order: 229-value user profile, then 229-value candidate product.
- The output is a relevance/ranking score, not a calibrated purchase probability.
- A user profile is a quantity-weighted mean of purchased product vectors using `log1p(quantity)`, followed by L2 normalization.
- Recommend only products with stock greater than zero that the user has not already purchased. Do not run personalized inference for a user without a completed-purchase profile.
- Keep feature encoding, profile building, and tensor handling out of Compose UI.

## Working notes

- Preserve existing user changes; inspect `git status` and the relevant diff before editing.
- Keep changes scoped to the responsible layer and follow existing Kotlin/Compose/Room patterns.
- The Python model project is maintained separately; use the handoff document and model metadata as the Android-side contract.
