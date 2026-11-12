# ShopMind 🛍️

**ShopMind** is a native Android e-commerce application built with **Kotlin and Jetpack Compose**, providing a complete shopping experience with an integrated **on-device machine learning recommendation system**.

The application combines modern Android development practices with local data management, REST API integration, and machine learning to deliver personalized product recommendations directly on users' devices.

ShopMind has been completed and a working application release is available.

## ✨ Features

- **Authentication:** User registration and login.
- **Product Catalog:** Browse and discover products.
- **Categories:** Explore products by category.
- **Search:** Search for products.
- **Product Details:** View product descriptions, prices, ratings, and images.
- **Favorites:** Save products for later.
- **Shopping Cart:** Add, remove, and manage cart items.
- **Checkout:** Complete the shopping process.
- **Order History:** Review previous purchases.
- **Profile & Settings:** Manage user information and application preferences.
- **Personalized Recommendations:** Discover relevant products using an on-device ML model.

## 🧠 On-Device Recommendation System

One of the core features of ShopMind is its machine learning-based product recommendation system.

Unlike recommendation systems that rely on remote inference services, ShopMind performs recommendation inference directly on the Android device using **LiteRT**.

### Recommendation Approach

The system uses users' purchase history to build personalized recommendations.

The recommendation pipeline consists of:

1. **Product Feature Engineering:** Extracting product features from categories, brands, tags, prices, ratings, and discounts.
2. **Purchase History Processing:** Collecting completed purchase interactions from the local database.
3. **User Profile Construction:** Building user preference vectors from previously purchased products.
4. **Recommendation Inference:** Using a trained machine learning model to calculate recommendation scores.
5. **Product Ranking:** Ranking candidate products based on predicted scores.

### Machine Learning Pipeline

The model was developed using Python and TensorFlow/Keras, then converted to LiteRT for on-device inference.

```text
       Product Dataset
              |
              v
      Feature Engineering
              |
              v
       Purchase History
              |
              v
      User Profile Building
              |
              v
       Model Training
       (TensorFlow/Keras)
              |
              v
        LiteRT Conversion
              |
              v
      Android Integration
              |
              v
     On-Device Inference
              |
              v
    Personalized Recommendations
```

### Dataset Overview

The recommendation model was developed using the following dataset:

| Metric | Count |
|---|---:|
| Products | 194 |
| Users | 21 |
| Orders | 21 |
| Purchase Interactions | 67 |
| Unique Purchased Products | 60 |
| Product Feature Dimensions | 229 |

### Recommendation Characteristics

- Uses purchase history as the primary personalization signal.
- Builds product representations using 229 features.
- Performs inference locally on Android devices.
- Does not require a remote ML inference service.
- Handles users without purchase history through a non-personalized recommendation fallback.

## 🛠️ Tech Stack

### Android Development

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Declarative UI |
| Material 3 | UI components and design system |
| MVVM | Presentation architecture |
| Clean Architecture principles | Separation of concerns |
| Navigation Compose | Screen navigation |
| Room | Local database |
| DataStore | Persistent preferences |
| Kotlin Coroutines | Asynchronous operations |
| Kotlin Flow | Reactive data streams |

### Machine Learning

| Technology | Purpose |
|---|---|
| Python | ML pipeline development |
| TensorFlow / Keras | Model development and training |
| LiteRT | On-device model inference |
| Feature Engineering | Product representation |
| Content-Based Filtering | Recommendation baseline |

### Data Source

- [DummyJSON](https://dummyjson.com/) — Product catalog and product information.

## 🏗️ Architecture

ShopMind follows a layered architecture inspired by Clean Architecture principles, separating presentation, domain, and data responsibilities.

The recommendation system is also isolated from the main shopping functionality.

A simplified conceptual structure:

```text
ShopMind/
│
├── app/
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   ├── dao/
│   │   └── entities/
│   │
│   └── remote/
│       ├── api/
│       └── models/
│
├── domain/
│   ├── models/
│   ├── repositories/
│   └── usecases/
│
├── presentation/
│   ├── authentication/
│   ├── home/
│   ├── categories/
│   ├── search/
│   ├── product/
│   ├── favorites/
│   ├── cart/
│   ├── checkout/
│   ├── orders/
│   └── profile/
│
└── recommendation/
    ├── data/
    ├── domain/
    └── ml/
```

*The structure above is a conceptual overview.*

## 🚀 Getting Started

### Prerequisites

- Android Studio
- JDK compatible with the project's Gradle configuration
- Android SDK
- Android device or emulator (Android 13+)

### Installation

1. Clone the repository:

```bash
git clone <REPOSITORY_URL>
```

2. Open the project in Android Studio.

3. Wait for Gradle synchronization.

4. Run the `app` configuration on an Android device or emulator.

## 📱 Screenshots

<!-- Replace placeholders with actual application screenshots -->

| Home | Categories | Product Details |
|:---:|:---:|:---:|
| Add Screenshot | Add Screenshot | Add Screenshot |

| Favorites | Shopping Cart | Profile |
|:---:|:---:|:---:|
| Add Screenshot | Add Screenshot | Add Screenshot |

| Personalized Recommendations |
|:---:|
| Add Recommendation Screenshot |

## 🎯 Project Highlights

- Developed a complete native Android shopping application.
- Applied modern Android development practices with Kotlin and Jetpack Compose.
- Implemented local persistence using Room and DataStore.
- Integrated remote product data through REST APIs.
- Developed a machine learning recommendation pipeline using Python and TensorFlow/Keras.
- Converted and integrated the trained model using LiteRT.
- Implemented on-device ML inference for personalized product recommendations.

## 📄 License

This project was developed for educational purposes.
