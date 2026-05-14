# Meal Planner

Meal Planner is an Android app for planning weekly meals, managing recipes, and discovering new recipe ideas through backend-powered suggestions.

## Overview

The app helps users organize what they want to cook throughout the week. Users can add their own recipes, schedule recipes for specific days, and request recipe suggestions from the backend when they want new meal ideas.

## Core Features

- Add and manage recipes
- Schedule recipes across the week
- View planned meals by day
- Request backend-generated recipe suggestions
- Use suggested recipes as inspiration for future meal plans

## Tech Stack

- Android
- Kotlin
- Jetpack Compose
- Gradle

## Project Structure

- `app/` - Android application module
- `app/src/main/java/com/p6ksolutions/mealplanner/` - Main Kotlin source code
- `app/src/main/res/` - Android resources
- `gradle/` - Gradle wrapper and version configuration

## Getting Started

### Prerequisites

- Android Studio
- JDK 11 or newer
- Android SDK with the configured compile SDK installed

### Build

```sh
./gradlew build
```

### Run

Open the project in Android Studio and run the `app` configuration on an emulator or physical Android device.

## Planned Functionality

The initial app experience will focus on:

1. Recipe creation and storage
2. Weekly meal scheduling
3. Backend integration for recipe suggestions
4. A simple meal planning interface optimized for Android
