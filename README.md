# PolyScores Kenya - Live Football Scores App

A native Android application for tracking live football scores for your polytechnic in Kenya.

## Features

### For Users
- **Live Scores**: Real-time score updates for ongoing matches
- **Match Fixtures**: View upcoming and scheduled matches
- **League Standings**: Automatic calculation of league tables
- **Team Profiles**: View team information and player rosters
- **Push Notifications**: Get notified for goals, match start, and full-time results
- **Match Statistics**: View shots, possession, corners, and cards

### For Admins
- **Score Updates**: Easy-to-use interface for updating match scores
- **Match Management**: Create new matches and manage fixtures
- **Status Control**: Start matches, set half-time, full-time status
- **Event Logging**: Add goals, cards, and other match events

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase (Firestore, Authentication, Cloud Messaging)
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Dependency Injection**: Manual DI (can be upgraded to Hilt/Koin)

## Project Structure

```
app/src/main/java/com/polyscores/kenya/
├── data/
│   ├── model/          # Data classes (Match, Team, Player, etc.)
│   ├── repository/     # Firebase repositories
│   └── remote/         # Firebase services (Firestore, FCM)
├── domain/
│   └── model/          # Domain models (if using clean architecture)
└── presentation/
    ├── ui/
    │   ├── components/ # Reusable UI components
    │   ├── screens/    # App screens (Home, Matches, etc.)
    │   └── theme/      # App theming
    └── viewModel/      # ViewModels
```

## Setup Instructions

### Prerequisites
1. Android Studio Hedgehog (2023.1.1) or newer
2. JDK 17
3. Android SDK 34
4. Firebase account

### Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app with package name: `com.polyscores.kenya`
4. Download `google-services.json` and place it in `app/` directory
5. Enable the following Firebase services:
   - **Firestore Database**: Create a new database (start in test mode)
   - **Authentication**: Enable Email/Password for admin login
   - **Cloud Messaging**: For push notifications

### Firestore Database Structure

Create the following collections:

```
- teams
  - {teamId}
    - name, shortName, logoUrl, department, etc.

- players
  - {playerId}
    - teamId, name, jerseyNumber, position, etc.

- leagues
  - {leagueId}
    - name, season, isActive, teamIds, etc.

- matches
  - {matchId}
    - leagueId, homeTeamId, awayTeamId, scores, status, etc.

- match_events
  - {eventId}
    - matchId, eventType, playerId, minute, etc.

- standings
  - {leagueId}
    - standings: [{teamId, points, played, won, drawn, lost, etc.}]

- admins
    - {adminId}
    - email, name, role, etc.
```

### Build & Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Add your Firebase configuration
4. Run on an emulator or physical device (Android 7.0+)

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Admin Access

To add admin users:
1. Manually add documents to the `admins` collection in Firestore
2. Or implement an admin registration screen (currently not in UI)

Example admin document:
```json
{
  "email": "admin@polytechnic.ac.ke",
  "name": "Admin User",
  "role": "SUPER_ADMIN",
  "department": "Sports",
  "isActive": true
}
```

## Customization

### Branding
- Update colors in `res/values/colors.xml`
- Change app name in `res/values/strings.xml`
- Replace app icon in `res/drawable/` and `res/mipmap-*/`

### Features to Add
- User authentication for admins
- Match details screen with full statistics
- Player statistics and top scorers
- Multiple leagues support
- Match commentary
- Photo gallery for matches
- Export standings to PDF

## Permissions

The app requires the following permissions:
- `INTERNET`: For Firebase connectivity
- `ACCESS_NETWORK_STATE`: To check network availability
- `POST_NOTIFICATIONS`: For push notifications (Android 13+)
- `VIBRATE`: For notification vibrations

## License

This project is created for educational purposes for your polytechnic institution.

## Support

For issues or feature requests, please contact the development team.

---

**Built with ❤️ for Kenyan Polytechnics**
