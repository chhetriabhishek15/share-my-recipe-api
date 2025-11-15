🍽️ ShareMyRecipe — REST API Platform

A modern backend service for publishing, discovering, and managing recipes — with secure authentication, image uploads, chef following, and background workers for image processing.

⭐ Overview

ShareMyRecipe is a Spring Boot–based backend that powers a recipe-sharing platform where:

👨‍🍳 Chefs can publish recipes
📸 Upload multiple images (with automatic async resizing)
👥 Users can follow chefs
📰 Users get a personalized recipe feed
🔐 Secure access via JWT authentication
⚙️ Images processed via a separate worker service (RabbitMQ)
📦 Built using clean architecture, SOLID, KISS, DRY principles

This README explains the app architecture, how to run it, how to use it, and how developers can continue building features.

🏗️ Architecture
Backend consists of 4 major components:
1️⃣ REST API (Spring Boot)

User signup/login (JWT)

Recipe authoring (multipart upload)

Public recipe listing with filters

Follow/unfollow chefs

User feed (recipes from followed chefs)

H2 database for local development

2️⃣ Image Storage

Images stored under /uploads/recipes/<recipeId>/<imageId>_<filename>

Structured file system approach for easy future cloud migration

3️⃣ Message Queue (RabbitMQ)

Whenever a recipe image is uploaded, an "image-resize-task" is published to RabbitMQ

Worker app consumes the queue

4️⃣ Worker Service (Spring Boot App #2)

Listens on queue

Performs:

Image resizing

Thumbnail generation

Updates DB with resized URLs

⚙️ Tech Stack
Layer	Technology
Language	Java 23+
Framework	Spring Boot 3.4
Auth	JWT (stateless)
Database	H2 (local)
ORM	Hibernate / JPA
Queue	RabbitMQ
Image Handling	Worker + Java ImageIO
Logging	SLF4J + Lombok
Build	Gradle
Principles	SOLID, KISS, DRY, Composition > Inheritance
📁 Project Structure

sharemyrecipe/
│
├── src/main/java/com/example/sharemyrecipe/
│   ├── core/config/         # SecurityConfig, JWT settings
│   ├── controller/          # REST API endpoints
│   ├── dto/                 # Request/Response DTOs
│   ├── entity/              # JPA Entities (User, Recipe, Image, Follow)
│   ├── repository/          # Spring Data JPA Repos
│   ├── security/            # JWT filter, JWT util, auth logic
│   ├── service/             # Interfaces
│   ├── service/impl/        # Business logic implementations
│   ├── mapper/              # Entity <-> DTO mapping
│   ├── util/                # Upload helpers, file utils
│   └── ShareMyRecipeApp.java
│
├── uploads/                 # Local image storage
└── worker/                  # Separate spring boot worker app (future)

🔐 Authentication Flow
Signup
POST /api/auth/signup


Fields:

email

password

handle

role (user/chef)

Login
POST /api/auth/login


Returns:

JWT Access Token

Use token in all protected routes:

Authorization: Bearer <jwt>

🥗 Recipes Module
1️⃣ Public Recipe Listing
GET /api/recipes


Query params:

q (keyword search)

published_from, published_to (ISO datetime)

chef_id

chef_handle

page, page_size

2️⃣ Create Recipe (with images)
POST /api/recipes
Content-Type: multipart/form-data
Authorization: Bearer <jwt>


Form-data fields:

Key	Type	Value
data	text	JSON string
images	file	1..n image files

Example JSON for data:

{
"title": "Chicken Biryani",
"summary": "Delicious and flavorful biryani",
"ingredients": "Chicken, rice, spices",
"steps": "Marinate → Cook → Serve",
"labels": ["indian", "spicy"],
"status": "PUBLISHED"
}


Every uploaded image triggers:

RabbitMQ → image-resize-task

3️⃣ Get Recipe
GET /api/recipes/{id}

4️⃣ Update Recipe
PUT /api/recipes/{id}
Authorization: Bearer <jwt>

5️⃣ Delete Recipe
DELETE /api/recipes/{id}
Authorization: Bearer <jwt>

👥 Follow System
Follow a chef
POST /api/chefs/{chefId}/follow
Authorization: Bearer <jwt>

Unfollow a chef
DELETE /api/chefs/{chefId}/follow
Authorization: Bearer <jwt>

📰 User Feed

Returns latest recipes from chefs the user follows.

GET /api/feed
Authorization: Bearer <jwt>

🛠️ How to Run Locally
1️⃣ Start RabbitMQ

Docker (recommended):

docker run -d --hostname rabbitmq --name recipe-rabbit -p 5672:5672 rabbitmq:3-management

2️⃣ Start Spring Boot API
./gradlew bootRun

3️⃣ API available at
http://localhost:8001/

4️⃣ H2 Database
http://localhost:8001/h2-console
