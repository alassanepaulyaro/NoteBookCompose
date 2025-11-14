# NoteBookCompose

A modern, mood-based note-taking Android application built with Jetpack Compose. This app allows users to create, manage, and organize personal notes with associated moods, images, and real-time cloud synchronization.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Technologies](#technologies)
5. [Prerequisites](#prerequisites)
6. [Setup Instructions](#setup-instructions)
7. [Project Structure](#project-structure)
8. [Key Components](#key-components)
9. [Screenshots](#screenshots)
10. [Author](#author)

---

## Project Overview

**NoteBookCompose** is a comprehensive note-taking application that demonstrates modern Android development practices. The app combines emotional intelligence with productivity by allowing users to associate moods with their notes, making it easier to track and reflect on thoughts and experiences over time.

The application leverages:
- **Jetpack Compose** for building reactive, declarative UIs
- **MongoDB Realm** with Device Sync for real-time cloud synchronization
- **Firebase** for authentication and cloud storage
- **Multi-module architecture** for scalability and maintainability
- **Material Design 3** for a modern, consistent user experience

---

## Features

### Core Functionality
- **Create Notes**: Write notes with titles, descriptions, and timestamps
- **Mood Tracking**: Associate notes with 16 different moods (Happy, Sad, Angry, Calm, etc.)
- **Image Support**: Add multiple images to notes with cloud storage
- **Edit & Delete**: Full CRUD operations on notes
- **Real-time Sync**: Automatic synchronization across devices via MongoDB Realm
- **Offline Support**: Work offline with automatic sync when connection is restored

### User Experience
- **Google Sign-In**: Quick and secure authentication with One-Tap sign-in
- **Modern UI**: Beautiful Material 3 design with mood-based color themes
- **Date & Time Selection**: Custom date and time pickers for note organization
- **Network Monitoring**: Connectivity status tracking
- **Splash Screen**: Smooth app launch experience

### Mood System
The app includes 16 distinct moods, each with unique icons and colors:
- Neutral, Happy, Angry, Bored
- Calm, Depressed, Disappointed, Humorous
- Lonely, Mysterious, Romantic, Shameful
- Awful, Surprised, Suspicious, Tense

---

## Architecture

### Multi-Module Architecture

The project follows a clean, modular architecture for better separation of concerns:

```
NoteBookCompose/
├── app/                    # Main application module
├── core/                   # Core shared modules
│   ├── ui/                # UI components and theme
│   └── util/              # Utilities and models
├── data/                   # Data layer
│   └── mongo/             # MongoDB Realm integration
└── feature/                # Feature modules
    ├── auth/              # Authentication feature
    ├── home/              # Home screen feature
    └── write/             # Note writing feature
```

### Design Patterns
- **MVVM (Model-View-ViewModel)**: Separates UI from business logic
- **Repository Pattern**: Abstracts data sources
- **Dependency Injection**: Using Dagger Hilt
- **Single Source of Truth**: MongoDB Realm as the primary data source
- **Unidirectional Data Flow**: State flows from ViewModel to UI

---

## Technologies

### Core Technologies
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material Design 3**: Latest Material Design guidelines
- **Coroutines**: Asynchronous programming
- **Flow**: Reactive streams

### Architecture Components
- **Dagger Hilt**: Dependency injection framework
- **Navigation Compose**: Navigation between screens
- **Lifecycle**: Lifecycle-aware components
- **ViewModel**: UI state management

### Data & Storage
- **MongoDB Realm**: NoSQL database with Device Sync
- **Room Database**: Local caching for images
- **Firebase Authentication**: User authentication with Google Sign-In
- **Firebase Storage**: Cloud storage for images

### Third-Party Libraries
- **Coil**: Image loading and caching
- **OneTapCompose**: Google One-Tap sign-in integration
- **MessageBarCompose**: User notifications
- **Accompanist Pager**: Pager layouts
- **Sheets Compose Dialogs**: Date/time pickers

### Build & Configuration
- **Gradle Version Catalog**: Centralized dependency management
- **KSP (Kotlin Symbol Processing)**: Annotation processing
- **Multi-module Gradle**: Modular build system

### SDK & API Levels
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **JVM Target**: 17

---

## Prerequisites

Before you begin, ensure you have the following installed:

1. **Android Studio**: Hedgehog (2023.1.1) or later
2. **JDK**: Java Development Kit 17
3. **Android SDK**: API Level 34
4. **Kotlin**: 1.9.10 or later

### Required Accounts & Services
1. **MongoDB Atlas Account**: For Realm Database and Device Sync
   - Create an app on [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
   - Enable Device Sync
   - Configure authentication

2. **Firebase Project**: For Authentication and Storage
   - Create a project on [Firebase Console](https://console.firebase.google.com/)
   - Enable Google Authentication
   - Enable Firebase Storage
   - Download `google-services.json`

---

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/alassanepaulyaro/NoteBookCompose.git
cd NoteBookCompose
```

### 2. Configure MongoDB Realm
1. Create a MongoDB Atlas account and project
2. Create a Realm App
3. Enable Device Sync with the following schema:
   ```javascript
   {
     "_id": "objectId",
     "ownerId": "string",
     "mood": "string",
     "title": "string",
     "description": "string",
     "images": ["string"],
     "date": "date"
   }
   ```
4. Update the App ID in your code (typically in `Constants.kt`)

### 3. Configure Firebase
1. Add your Android app to Firebase project
2. Download `google-services.json`
3. Place it in the `app/` directory
4. Enable Google Sign-In in Firebase Authentication
5. Configure Firebase Storage rules

### 4. Update Configuration Files
Update the following files with your credentials:

**`core/util/src/main/java/com/yaropaul/util/Constants.kt`**:
```kotlin
const val APP_ID = "your-mongodb-realm-app-id"
const val CLIENT_ID = "your-google-client-id"
```

### 5. Build the Project
```bash
./gradlew clean build
```

### 6. Run the Application
- Connect an Android device or start an emulator
- Click Run in Android Studio or use:
```bash
./gradlew installDebug
```

---

## Project Structure

### Module Details

#### `:app`
Main application module containing:
- `MainActivity`: Single-activity architecture
- `MyApplication`: Application class with Hilt
- `NavGraph`: Navigation configuration
- `DatabaseModule`: Database dependency injection

#### `:core:ui`
Shared UI components and theming:
- Material 3 theme configuration
- Custom color schemes for moods
- Reusable UI components (GoogleButton, AlertDialog)
- Typography and elevation definitions

#### `:core:util`
Shared utilities and models:
- `NoteBook`: Main data model
- `Mood`: Mood enum with 16 emotions
- `Screen`: Navigation routes
- Network connectivity observer
- Extension functions and constants

#### `:data:mongo`
Data layer with MongoDB integration:
- `MongoDB`: Realm database setup
- `MongoRepository`: Data operations interface
- `ImagesDatabase`: Room database for image sync
- DAOs for image upload/delete operations

#### `:feature:auth`
Authentication feature module:
- Google Sign-In integration
- Authentication UI screens
- Authentication ViewModel
- Auth navigation

#### `:feature:home`
Home screen feature module:
- Note list display
- Filtering by date and mood
- Pull-to-refresh
- Home screen top bar with actions
- Home ViewModel

#### `:feature:write`
Note writing/editing feature module:
- Note creation and editing UI
- Image picker integration
- Mood selector
- Date/time pickers
- Write ViewModel

---

## Key Components

### Data Models

**NoteBook**:
```kotlin
class NoteBook : RealmObject {
    @PrimaryKey var _id: BsonObjectId
    var ownerId: String
    var mood: String
    var title: String
    var description: String
    var images: RealmList<String>
    var date: RealmInstant
}
```

### State Management
- ViewModels handle UI state and business logic
- StateFlow/SharedFlow for reactive updates
- RequestState wrapper for loading/success/error states

### Dependency Injection
- Hilt modules for all dependencies
- Scoped ViewModels
- Repository pattern implementation

### Navigation
- Jetpack Navigation Compose
- Type-safe navigation with routes
- Deep linking support

---

## Screenshots

<div style="display: flex; flex-wrap: wrap; gap: 10px;">

<img src="screenshot/Screenshot_1.png" alt="Splash Screen" width="200">
<img src="screenshot/Screenshot_2.png" alt="Login Screen" width="200">
<img src="screenshot/Screenshot_3.png" alt="Empty Notes" width="200">
<img src="screenshot/Screenshot_4.png" alt="Navigation Drawer" width="200">
<img src="screenshot/Screenshot_5.png" alt="Date Time Picker" width="200">
<img src="screenshot/Screenshot_6.png" alt="Home View" width="200">
<img src="screenshot/Screenshot_7.png" alt="Edit View" width="200">

</div>

---

## Building for Production

### ProGuard Configuration
The app includes ProGuard rules for release builds. Ensure you test the release build thoroughly:

```bash
./gradlew assembleRelease
```

### Signing Configuration
Configure signing in `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("your-keystore.jks")
        storePassword = "your-store-password"
        keyAlias = "your-key-alias"
        keyPassword = "your-key-password"
    }
}
```

---

## Testing

The project includes:
- Unit tests with JUnit
- Instrumented tests with Espresso
- UI tests with Compose Testing

Run tests:
```bash
./gradlew test           # Unit tests
./gradlew connectedTest  # Instrumented tests
```

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is available for educational and personal use. Please ensure you comply with the licenses of all third-party libraries used in this project.

---

## Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [MongoDB Realm](https://www.mongodb.com/realm) - Mobile database and sync
- [Firebase](https://firebase.google.com/) - Backend services
- [Material Design 3](https://m3.material.io/) - Design system
- [Stevdza-San](https://github.com/stevdza-san) - OneTapCompose and MessageBarCompose libraries

---

## Support

For support, please:
- Open an issue in the GitHub repository
- Contact the author through GitHub

---

## Roadmap

Future enhancements planned:
- [ ] Dark mode support
- [ ] Search functionality
- [ ] Note categories/tags
- [ ] Export notes to PDF
- [ ] Voice notes
- [ ] Reminders and notifications
- [ ] Collaborative notes
- [ ] Analytics dashboard

---

## Author

**Alassane Paulyaro**
- GitHub: [@alassanepaulyaro](https://github.com/alassanepaulyaro)

---

## Project Status

This project is actively maintained and open for contributions. Last updated: November 2024

---

**Built with ❤️ using Jetpack Compose**
