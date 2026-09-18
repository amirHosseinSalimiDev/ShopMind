# Phase 1 — Database Design

Design the Room database around these entities:

### 1. Users

```text
id
username
email
passwordHash
firstName
lastName
createdAt
```

### 2. Products

Keep the product properties that are useful for recommendation and the UI.

Recommendation-relevant properties:

```text
id
category
brand
tags
price
discountPercentage
rating
```

Other UI properties can also be stored:

```text
title
description
images
thumbnail
stock
availabilityStatus
```

Do not store unnecessary DummyJSON metadata unless the application UI specifically needs them.

### 3. Orders

```text
id
userId
totalAmount
createdAt
status
```

### 4. OrderItems

```text
id
orderId
productId
quantity
unitPrice
```

The relationship should be:

```text
User
  ↓
Orders
  ↓
OrderItems
  ↓
Products
```

This structure must allow the application to determine exactly which products each user has purchased.

---

# Phase 2 — Product API & Retrofit Configuration

Use **Retrofit** to communicate with the DummyJSON API.

## 2.1 — Add Retrofit Dependencies

Add the required Retrofit dependencies and JSON converter/serialization dependencies to the project.

Configure them using the project's existing dependency management approach.

## 2.2 — Configure Retrofit

Create a centralized Retrofit instance using:

```text
Base URL:
https://dummyjson.com/
```

The Retrofit instance should be reusable across the application.

Suggested structure:

```text
data/
└── remote/
    ├── api/
    │   └── ProductApiService
    ├── dto/
    │   └── ProductDto
    └── RetrofitClient
```

## 2.3 — Define Product API

Create a Retrofit service with these endpoints:

### Get All Products

```text
GET /products
```

### Get Product By ID

```text
GET /products/{id}
```

Example:

```text
GET /products/1
```

### Search Products

```text
GET /products/search?q={query}
```

Example:

```text
GET /products/search?q=mascara
```

### Get Categories

```text
GET /products/categories
```

### Get Products By Category

```text
GET /products/category/{category}
```

Example:

```text
GET /products/category/beauty
```

---

## 2.4 — Create Concrete Product Response Model

Create a concrete DTO that matches the DummyJSON product response.

For `GET /products/{id}`, the response structure is:

```json
{
  "id": 1,
  "title": "Essence Mascara Lash Princess",
  "description": "The Essence Mascara Lash Princess is a popular mascara known for its volumizing and lengthening effects. Achieve dramatic lashes with this long-lasting and cruelty-free formula.",
  "category": "beauty",
  "price": 9.99,
  "discountPercentage": 10.48,
  "rating": 2.56,
  "stock": 99,
  "tags": [
    "beauty",
    "mascara"
  ],
  "brand": "Essence",
  "sku": "BEA-ESS-ESS-001",
  "weight": 4,
  "dimensions": {
    "width": 15.14,
    "height": 13.08,
    "depth": 22.99
  },
  "warrantyInformation": "1 week warranty",
  "shippingInformation": "Ships in 3-5business days",
  "availabilityStatus": "In Stock",
  "reviews": [
    {
      "rating": 3,
      "comment": "Would not recommend!",
      "date": "2025-04-30T09:41:02.053Z",
      "reviewerName": "Eleanor Collins",
      "reviewerEmail": "eleanor.collins@x.dummyjson.com"
    },
    {
      "rating": 4,
      "comment": "Very satisfied!",
      "date": "2025-04-30T09:41:02.053Z",
      "reviewerName": "Lucas Gordon",
      "reviewerEmail": "lucas.gordon@x.dummyjson.com"
    },
    {
      "rating": 5,
      "comment": "Highly impressed!",
      "date": "2025-04-30T09:41:02.053Z",
      "reviewerName": "Eleanor Collins",
      "reviewerEmail": "eleanor.collins@x.dummyjson.com"
    }
  ],
  "returnPolicy": "No return policy",
  "minimumOrderQuantity": 48,
  "meta": {
    "createdAt": "2025-10-09T14:47:01.588Z",
    "updatedAt": "2026-05-23T11:27:41.868Z",
    "barcode": "5784719087687",
    "qrCode": "https://cdn.dummyjson.com/public/qr-code.png"
  },
  "images": [
    "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp"
  ],
  "thumbnail": "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/thumbnail.webp"
}
```

Create the corresponding nested DTOs for:

```text
ProductDto
DimensionsDto
ReviewDto
MetaDto
```

The Retrofit response model should preserve the API's actual property names and structure.

Use appropriate JSON serialization annotations if required by the selected serialization library.

### ProductDto

It should represent all properties returned by the API:

```text
id
title
description
category
price
discountPercentage
rating
stock
tags
brand
sku
weight
dimensions
warrantyInformation
shippingInformation
availabilityStatus
reviews
returnPolicy
minimumOrderQuantity
meta
images
thumbnail
```

### DimensionsDto

```text
width
height
depth
```

### ReviewDto

```text
rating
comment
date
reviewerName
reviewerEmail
```

### MetaDto

```text
createdAt
updatedAt
barcode
qrCode
```

Do not remove fields from the **API response DTO** merely because they are not needed for recommendation.

The API DTO should accurately represent the server response.

Later, when mapping `ProductDto` to the local `ProductEntity`, only persist the properties required by the application and recommendation system.

The intended flow is:

```text
DummyJSON API
      ↓
ProductDto
      ↓
ProductEntity
      ↓
Room
      ↓
Purchase History + Product Features
      ↓
LiteRT
```

This keeps the **remote API model separate from the local database model** and prevents the Room schema from being unnecessarily coupled to DummyJSON's response structure.


# Phase 3 — Product Data Preparation

Fetch products from DummyJSON and store the required product information in Room.

For recommendation purposes, focus on:

```text
category
brand
tags
price
discountPercentage
rating
```

Treat these as the product features that can later be converted into model input.

Example:

```text
Product:
category = beauty
brand = Essence
tags = [beauty, mascara]
price = 9.99
discountPercentage = 10.48
rating = 2.56
```

---

# Phase 4 — Purchase History

After a successful checkout:

1. Create an `Order`.
2. Create its `OrderItems`.
3. Associate the order with the currently authenticated `userId`.
4. Keep the purchased `productId`, `quantity`, and `unitPrice`.

The recommendation system must be able to query:

```text
userId → purchased productIds → product features
```

For example:

```text
User 1
  ↓
Orders
  ↓
Product 1
Product 15
Product 42
  ↓
Product features
```

---

# Phase 5 — Determine Recommendation Eligibility

Before running LiteRT, check whether the current user has purchase history.

The logic should be:

```text
User logs in
      ↓
Get userId
      ↓
Get purchase history
      ↓
Has purchased products?
   /          \
 No            Yes
 ↓              ↓
No             Continue
personalized
recommendation
```

For a user with zero purchases:

```text
purchaseHistory.isEmpty()
    → do not show personalized recommendations
```

Do not create a generic recommendation model for new users unless explicitly added later as a separate feature.

---

# Phase 6 — Build the User Profile From Purchases

Do not store a permanent `UserPreference` table initially.

Instead, derive the user's recommendation features from their purchased products.

Example:

```text
User 1 purchased:

Product A:
category = beauty
brand = Essence
tags = [beauty, mascara]

Product B:
category = beauty
brand = Maybelline
tags = [beauty, lipstick]

Product C:
category = beauty
brand = Essence
tags = [beauty, foundation]
```

Derive a user preference representation such as:

```text
category:
    beauty → high preference

brands:
    Essence → high preference
    Maybelline → medium preference

tags:
    beauty → high preference
    mascara → high preference
    lipstick → medium preference
    foundation → medium preference
```

The exact representation should be designed according to the LiteRT model input.

---

# Phase 7 — Prepare LiteRT Input

Create a separate recommendation/model layer.

Do not make Room entities the direct input of LiteRT.

Use this transformation:

```text
Room Entities
     ↓
Purchase History
     ↓
Product Features
     ↓
Feature Extraction
     ↓
Model Input
     ↓
LiteRT
```

The model should receive numerical features rather than raw database objects.

Categorical properties such as:

```text
category
brand
tags
```

must be encoded into a numerical representation appropriate for the model.

Numerical properties such as:

```text
price
discountPercentage
rating
```

can be normalized/scaled as required by the model.

---

# Phase 8 — Candidate Products

Do not run recommendation against products the user has already purchased unless there is a specific reason to recommend repurchasing them.

Initially:

```text
All Products
    ↓
Remove already purchased products
    ↓
Candidate Products
    ↓
LiteRT
```

This creates a cleaner "discover something new" recommendation experience.

---

# Phase 9 — LiteRT Recommendation

For the current user:

```text
currentUserId
      ↓
purchase history
      ↓
purchased product features
      ↓
user feature representation
      ↓
candidate products
      ↓
LiteRT
      ↓
recommendation score
      ↓
sort by score
      ↓
Top N products
```

Example:

```text
User 1

Purchased:
- Essence Mascara
- Maybelline Lipstick
- Essence Foundation

        ↓

LiteRT

        ↓

Product scores:

Product 7  → 0.91
Product 12 → 0.84
Product 25 → 0.76

        ↓

Recommended:
Product 7
Product 12
Product 25
```

The same model must be able to produce different results for another user because their purchase history is different.

---

# Phase 10 — Repository Architecture

Keep recommendation logic separate from the database layer.

Suggested architecture:

```text
UI
 ↓
RecommendationViewModel
 ↓
RecommendationRepository
 ↓
PurchaseRepository
 ↓
Room
```

And:

```text
RecommendationRepository
        ↓
Purchase History
        ↓
Feature Extraction
        ↓
LiteRT Recommendation Engine
        ↓
Recommended Products
```

Suggested packages:

```text
data/
├── local/
│   ├── dao/
│   │   ├── UserDao
│   │   ├── ProductDao
│   │   ├── OrderDao
│   │   └── OrderItemDao
│   │
│   ├── entity/
│   │   ├── UserEntity
│   │   ├── ProductEntity
│   │   ├── OrderEntity
│   │   └── OrderItemEntity
│   │
│   └── database/
│
├── remote/
│   ├── api/
│   │   └── ProductApiService
│   ├── dto/
│   └── RetrofitClient
│
└── repository/
    ├── ProductRepository
    ├── OrderRepository
    └── RecommendationRepository

domain/
└── recommendation/
    ├── RecommendationEngine
    ├── ProductFeatureExtractor
    └── RecommendationResult

ml/
└── litert/
    └── LiteRTRecommendationEngine
```

---

# Phase 11 — Login Integration

When the user logs in:

```text
Login
 ↓
Get authenticated userId
 ↓
Load purchase history
 ↓
If history is empty
      ↓
No personalized recommendations

If history exists
      ↓
Prepare recommendation input
      ↓
Run LiteRT
      ↓
Display recommendations
```

The recommendation screen must always use the **currently authenticated ****`userId`**.

Never use a global recommendation list that is shared between users.

---

# Phase 12 — UI Integration

On the Home screen:

```text
Home
├── Categories
├── Popular Products
└── Recommended For You
```

The `Recommended For You` section should only appear when the current user has enough purchase history to generate personalized recommendations.

For a new user:

```text
Home
├── Categories
├── Popular Products
└── No personalized recommendation section
```

Do not show an empty "Recommended For You" section.

---

# Phase 13 — Testing

Test at least these scenarios:

### Scenario 1 — New User

```text
User A
Purchases = 0
```

Expected:

```text
No personalized recommendations
```

### Scenario 2 — User With Purchases

```text
User A
Purchased:
Beauty products
```

Expected:

```text
Recommendations related to User A's purchased product features
```

### Scenario 3 — Different User

```text
User B
Purchased:
Electronics products
```

Expected:

```text
Recommendations based on User B's purchase history
```

The recommendation results for User A and User B should be generated from their respective purchase histories.

### Scenario 4 — Same User, More Purchases

```text
User A initially:
Beauty purchases

Later:
Electronics purchase
```

The next recommendation generation should use the updated purchase history.

---

# Final Architecture

The complete recommendation pipeline should be:

```text
                    ┌──────────────┐
                    │    User      │
                    └──────┬───────┘
                           │ userId
                           ▼
                    ┌──────────────┐
                    │    Orders    │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ OrderItems   │
                    └──────┬───────┘
                           │ productId
                           ▼
                    ┌──────────────┐
                    │   Products   │
                    └──────┬───────┘
                           │
                           ▼
                 Product Feature Extraction
                           │
                           ▼
                    ┌──────────────┐
                    │    LiteRT    │
                    └──────┬───────┘
                           │
                           ▼
                  Recommendation Scores
                           │
                           ▼
                     Top N Products
                           │
                           ▼
                  "Recommended For You"
```

The key principle is:

**`userId → purchase history → purchased product features → LiteRT → personalized recommendations`**

There should be **no ****`UserBehavior`**** table** and no view/favorite/cart signals involved in the recommendation model.
