# ShopMind 🛍️

**ShopMind** is an Android e-commerce application built with **Kotlin and Jetpack Compose**, featuring a modern shopping experience and an experimental on-device product recommendation system powered by machine learning.

The project explores how machine learning can be integrated into an Android application to provide personalized product recommendations based on users' purchase history.

## ✨ Features

- **Authentication:** User registration and login.
- **Product Catalog:** Browse products with detailed information.
- **Categories:** Explore products by category.
- **Search:** Search and discover products.
- **Product Details:** View product descriptions, prices, ratings, and images.
- **Favorites:** Save products for later.
- **Shopping Cart:** Add, remove, and manage cart items.
- **Checkout:** Complete the shopping process.
- **Order History:** Review previous purchases.
- **Profile & Settings:** Manage user profile and application preferences.
- **Product Recommendations:** Personalized product recommendations based on purchase history (ML implementation in progress).

## 🧠 Recommendation System

One of the main objectives of ShopMind is exploring **on-device machine learning for personalized product recommendations**.

The recommendation system is designed to run locally on Android devices using LiteRT, reducing the need for a dedicated recommendation backend.

### Current Approach

The system uses purchase history as its primary source of user preferences.

- Product features are extracted from categories, brands, tags, prices, ratings, and discounts.
- User profiles are constructed from previously purchased products.
- Candidate products are scored and ranked to generate personalized recommendations.
- New users without purchase history are handled through a non-personalized fallback strategy.

### Machine Learning Pipeline

The model development process uses Python and TensorFlow/Keras.

```text
Product Dataset + Purchase History
              |
              v
      Feature Engineering
              |
              v
       User Profile Building
              |
              v
       Recommendation Model
              |
              v
         LiteRT Conversion
              |
              v
       Android Integration
```

The current dataset contains:

| Metric | Count |
|---|---:|
| Products | 194 |
| Users | 21 |
| Orders | 21 |
| Purchase Interactions | 67 |
| Unique Purchased Products | 60 |
| Product Feature Dimensions | 229 |

**Note:** The recommendation model is experimental. Due to the limited dataset size, model complexity and recommendation quality are being evaluated against a content-based baseline.

## 🛠️ Tech Stack

### Android Development

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Design System:** Material 3
- **Architecture:** MVVM / Clean Architecture principles
- **Navigation:** Navigation Compose
- **Local Storage:** Room Database
- **Preferences & Authentication State:** DataStore
- **Asynchronous Programming:** Kotlin Coroutines & Flow

### Machine Learning

- **Language:** Python
- **ML Framework:** TensorFlow / Keras
- **On-device Inference:** LiteRT
- **Recommendation Approach:** Content-Based Filtering and Neural Network experimentation

### Data Source

- **Product API:** [DummyJSON](https://dummyjson.com/)

## 🏗️ Project Structure

The Android application follows a modular and layered approach to separate UI, business logic, and data responsibilities.

A simplified representation:

```text
ShopMind/
│
├── app/
│   └── Application entry point
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
    ├── feature engineering
    ├── user profiles
    └── ML inference
```

*This is a conceptual overview; the actual source tree may differ.*

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

<!-- Add application screenshots here -->

| Home | Product Details | Categories |
|:---:|:---:|:---:|
| Coming Soon | Coming Soon | Coming Soon |

| Shopping Cart | Favorites | Profile |
|:---:|:---:|:---:|
| Coming Soon | Coming Soon | Coming Soon |

## 🎯 Project Goals

ShopMind is designed as a practical exploration of modern Android development and machine learning integration.

Key objectives:

- Building a modern Android application using Kotlin and Jetpack Compose.
- Applying clean architecture and separation of concerns.
- Managing local application data with Room and DataStore.
- Integrating remote product data through REST APIs.
- Exploring machine learning for personalized recommendations.
- Running recommendation inference directly on Android devices using LiteRT.

## 📌 Future Improvements

- Complete LiteRT model integration.
- Improve recommendation quality and evaluation.
- Expand the product and purchase datasets.
- Enhance recommendation ranking.
- Improve UI/UX and application performance.
- Add more comprehensive unit and integration tests.

## 📄 License

This project was developed for educational and experimental purposes.
