# Firebase Setup Guide for PolyScores Kenya

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** (or select existing project)
3. Enter project name: `PolyScores Kenya` (or your preferred name)
4. Disable Google Analytics (optional)
5. Click **Create project**

## Step 2: Add Android App to Firebase

1. In Firebase Console, click the **Android** icon to add an Android app
2. Enter package name: `com.polyscores.kenya`
3. App nickname (optional): `PolyScores Kenya`
4. Click **Register app**
5. Download the `google-services.json` file
6. Place the file in: `app/google-services.json`

## Step 3: Enable Firebase Services

### Firestore Database

1. In Firebase Console, go to **Build** > **Firestore Database**
2. Click **Create database**
3. Choose **Start in test mode** (for development)
4. Select a location closest to Kenya (e.g., `europe-west` or `asia-south1`)
5. Click **Enable**

### Firebase Authentication

1. Go to **Build** > **Authentication**
2. Click **Get started**
3. Enable **Email/Password** sign-in method
4. Click **Save**

### Firebase Cloud Messaging (FCM)

1. FCM is automatically enabled when you add Firebase to your app
2. No additional setup required for basic notifications

## Step 4: Create Firestore Collections

In Firebase Console, go to **Firestore Database** and create these collections:

### Collection: `teams`

```
Document: (auto-generated ID)
Fields:
  - name: "Technical Department FC"
  - shortName: "TECH"
  - logoUrl: ""
  - primaryColor: "#009900"
  - secondaryColor: "#FFFFFF"
  - department: "Technical"
  - yearEstablished: 2020
  - captainId: ""
  - coachName: "John Doe"
  - contactPhone: "+254700000000"
  - createdAt: (Timestamp)
  - isActive: true
```

### Collection: `players`

```
Document: (auto-generated ID)
Fields:
  - teamId: "team_document_id"
  - name: "Player Name"
  - jerseyNumber: 10
  - position: "FORWARD"
  - age: 22
  - phoneNumber: "+254700000000"
  - isCaptain: false
  - photoUrl: ""
  - createdAt: (Timestamp)
```

### Collection: `leagues`

```
Document: (auto-generated ID)
Fields:
  - name: "Polytechnic League 2024"
  - season: "2024"
  - startDate: (Timestamp)
  - endDate: (Timestamp)
  - isActive: true
  - format: "LEAGUE"
  - teamIds: ["team1_id", "team2_id", ...]
  - createdAt: (Timestamp)
```

### Collection: `matches`

```
Document: (auto-generated ID)
Fields:
  - leagueId: "league_document_id"
  - homeTeamId: "home_team_id"
  - awayTeamId: "away_team_id"
  - homeTeamName: "Home Team FC"
  - awayTeamName: "Away Team FC"
  - homeTeamLogo: ""
  - awayTeamLogo: ""
  - homeScore: 0
  - awayScore: 0
  - matchStatus: "SCHEDULED"
  - scheduledTime: (Timestamp)
  - venue: "Main Field"
  - round: "Round 1"
  - lastUpdated: (Timestamp)
```

### Collection: `match_events`

```
Document: (auto-generated ID)
Fields:
  - matchId: "match_document_id"
  - eventType: "GOAL"
  - teamId: "team_id"
  - playerId: "player_id"
  - playerName: "Player Name"
  - minute: 45
  - extraTime: 0
  - description: "Great header from corner"
  - timestamp: (Timestamp)
```

### Collection: `standings`

```
Document: league_id
Fields:
  - leagueId: "league_document_id"
  - standings: [
      {
        teamId: "team1_id",
        teamName: "Team 1",
        played: 5,
        won: 3,
        drawn: 1,
        lost: 1,
        goalsFor: 10,
        goalsAgainst: 5,
        goalDifference: 5,
        points: 10,
        form: ["WIN", "DRAW", "WIN", "LOSS", "WIN"],
        position: 1
      },
      ...
    ]
  - lastUpdated: (Timestamp)
```

### Collection: `admins`

```
Document: (auto-generated ID)
Fields:
  - email: "admin@polytechnic.ac.ke"
  - name: "Admin Name"
  - role: "SUPER_ADMIN"
  - department: "Sports"
  - phone: "+254700000000"
  - createdAt: (Timestamp)
  - lastLogin: (Timestamp or null)
  - isActive: true
```

## Step 5: Firestore Security Rules

Replace the default rules with these:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Public read access for most collections
    match /{collection}/{document} {
      allow read: if true;
    }

    // Only admins can write to sensitive collections
    match /matches/{matchId} {
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/admins/$(request.auth.uid)).data.role == 'SUPER_ADMIN';
    }

    match /match_events/{eventId} {
      allow write: if request.auth != null;
    }

    match /admins/{adminId} {
      allow read: if request.auth != null;
      allow write: if false; // Only server-side or manual addition
    }

    match /standings/{leagueId} {
      allow write: if request.auth != null;
    }
  }
}
```

## Step 6: Create First Admin User

### Option A: Via Firebase Console

1. Go to **Authentication** > **Users**
2. Click **Add user**
3. Enter email and password
4. Note the User UID

Then manually add to Firestore `admins` collection:

```
Document ID: (same as Auth UID)
Fields:
  - email: "admin@polytechnic.ac.ke"
  - name: "Admin Name"
  - role: "SUPER_ADMIN"
  - department: "Sports"
  - isActive: true
```

### Option B: Direct Firestore Entry

1. Go to **Firestore Database**
2. Select `admins` collection
3. Add document with Auth UID as document ID
4. Add the fields as shown above

## Step 7: Configure App Build

1. Replace `app/google-services.json` with your downloaded file
2. Update `app/build.gradle.kts` if your Firebase project has different settings
3. Sync Gradle files

## Step 8: Test the App

1. Build and run the app on a device or emulator
2. Verify Firebase connection by checking if data loads
3. Add sample teams and matches via Firestore Console
4. Test score updates in the Admin Panel

## Troubleshooting

### App crashes on startup
- Check if `google-services.json` is in the correct location
- Verify package name matches in Firebase Console
- Check Logcat for specific error messages

### No data showing
- Verify Firestore collections exist and have data
- Check Firestore security rules allow read access
- Ensure internet connection is available

### Notifications not working
- Check if FCM is enabled in Firebase
- Verify notification channel is created (Android 8+)
- Check app permissions for notifications

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Setup Guide](https://firebase.google.com/docs/firestore/quickstart)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Android Studio Setup](https://developer.android.com/studio/intro)
