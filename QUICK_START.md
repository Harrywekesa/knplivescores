# Quick Start Guide - PolyScores Kenya

**For users who want to get running fast. Follow these steps in order.**

---

## 5-Minute Firebase Setup

### 1. Create Firebase Project (2 minutes)

1. Go to: https://console.firebase.google.com/
2. Click **"Add project"**
3. Name: `PolyScores Kenya` → Continue → Create project
4. Wait for creation → Continue

### 2. Add Android App (1 minute)

1. Click the **Android** icon (</>)
2. Package name: `com.polyscores.kenya` (exact match required)
3. App nickname: `PolyScores Kenya`
4. Click **Register app**
5. **Download `google-services.json`**
6. Copy to: `C:\Users\Pi\PolyScoresApp\app\google-services.json` (replace existing file)
7. Click **Next** → **Continue to console**

### 3. Enable Firestore (2 minutes)

1. Click **"Build"** → **"Firestore Database"**
2. Click **"Create database"**
3. Select **"Start in test mode"** → Next
4. Location: `europe-west` → **Enable**
5. Wait for database to be created

### 4. Enable Authentication (1 minute)

1. Click **"Build"** → **"Authentication"**
2. Click **"Get started"**
3. Click **"Email/Password"**
4. Toggle **"Enable"** → **Save**

**Firebase setup complete!**

---

## Open and Run in Android Studio

### 1. Open Project

1. Launch **Android Studio**
2. Click **Open** → Navigate to `C:\Users\Pi\PolyScoresApp`
3. Click **OK**

### 2. Wait for Gradle Sync

- Android Studio will download dependencies
- Wait for **"Sync Successful"** (5-15 minutes first time)

### 3. Create Emulator (First Time Only)

1. Click **Tools** → **Device Manager**
2. Click **"Create device"**
3. Select **Pixel 6** → Next
4. Select **API 34** (download if needed) → Next
5. Click **"Finish"**

### 4. Run the App

1. Select your emulator from device dropdown
2. Click green **Run** button (▶)
3. Wait for app to launch

---

## Add Sample Data (via Firebase Console)

### Add a Team

1. Go to Firestore: https://console.firebase.google.com/project/YOUR_PROJECT/firestore
2. Click **"Start collection"**
3. Collection ID: `teams`
4. Add document with these fields:
   ```
   name: "Technical FC"
   shortName: "TECH"
   department: "Technical"
   isActive: true
   createdAt: (Timestamp - click "Timestamp" type)
   ```
5. Click **Save**

### Add a Match

1. Click **"Start collection"**
2. Collection ID: `matches`
3. Add document:
   ```
   homeTeamName: "Technical FC"
   awayTeamName: "Business FC"
   homeScore: 0
   awayScore: 0
   matchStatus: "SCHEDULED"
   scheduledTime: (Timestamp)
   venue: "Main Field"
   round: "Round 1"
   lastUpdated: (Timestamp)
   ```
4. Click **Save**

### Update Firestore Rules

1. In Firestore, click **"Rules"** tab
2. Replace with:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{collection}/{document} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```
3. Click **"Publish"**

---

## Test the App

1. **Pull to refresh** or restart app
2. You should see your match data
3. Navigate using bottom bar:
   - 🏠 Home - Main screen
   - ⚽ Matches - All matches
   - 📊 Standings - League table
   - 👥 Teams - Team list
   - ⚙️ Settings - App settings

---

## Common Issues & Quick Fixes

| Problem | Solution |
|---------|----------|
| "google-services.json not found" | Copy file to `app/google-services.json` |
| App crashes on startup | Check package name matches Firebase |
| No data showing | Add data via Firestore Console |
| Gradle sync failed | File → Invalidate Caches → Restart |
| Emulator slow | Enable virtualization in BIOS |

---

## Next Steps

1. **Add more teams and matches** via Firestore Console
2. **Test admin panel** - Settings → Admin Panel
3. **Update scores** - Tap any match in Admin Panel
4. **Customize branding** - Edit `res/values/colors.xml`

---

## File Locations

- **Project:** `C:\Users\Pi\PolyScoresApp`
- **google-services.json:** `C:\Users\Pi\PolyScoresApp\app\google-services.json`
- **Main code:** `C:\Users\Pi\PolyScoresApp\app\src\main\java\com\polyscores\kenya\`

---

**Done!** Your app is now running. For detailed setup, see `INSTALLATION_GUIDE.md`.
