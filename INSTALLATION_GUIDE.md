# Complete Installation Guide - PolyScores Kenya

This guide will take you from zero to a fully running app. Follow each step carefully.

---

## Table of Contents

1. [Prerequisites](#step-1-install-prerequisites)
2. [Install Android Studio](#step-2-install-android-studio)
3. [Set Up Firebase](#step-3-set-up-firebase)
4. [Configure the Project](#step-4-configure-the-project)
5. [Set Up Firestore Database](#step-4-set-up-firestore-database)
6. [Build and Run the App](#step-5-build-and-run-the-app)
7. [Add Sample Data](#step-6-add-sample-data)
8. [Test the App](#step-7-test-the-app)
9. [Troubleshooting](#troubleshooting)

---

## Step 1: Install Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17**
   - Download from: https://www.oracle.com/java/technologies/downloads/#jdk17-windows
   - Or use OpenJDK: https://adoptium.net/
   - Install and note the installation path

2. **Android Studio**
   - Download from: https://developer.android.com/studio
   - Choose the latest stable version (recommended: Hedgehog 2023.1.1 or newer)

3. **Git (Optional but recommended)**
   - Download from: https://git-scm.com/downloads

### System Requirements

- **OS:** Windows 10/11 (64-bit)
- **RAM:** Minimum 8GB (16GB recommended)
- **Disk Space:** At least 10GB free
- **Screen Resolution:** 1920 x 1080 minimum

---

## Step 2: Install Android Studio

### Installation on Windows

1. **Run the installer** (`android-studio-2023.1.1.26-windows.exe` or similar)

2. **Follow the setup wizard:**
   - Click "Next" on the welcome screen
   - Choose installation location (default: `C:\Program Files\Android\Android Studio`)
   - Check "Create Desktop Shortcut"
   - Check "Install Android Virtual Device" (for emulator)
   - Click "Install"

3. **Complete the setup:**
   - Click "Finish" when installation completes
   - Check "Start Android Studio"
   - Click "Finish" again

4. **First-time setup:**
   - Click "Do not import settings" → OK
   - Click "Next" through the setup wizard
   - Choose "Standard" installation type
   - Select a theme (Light or Dark)
   - Click "Finish" to download components

5. **Wait for downloads:**
   - Android Studio will download SDK components
   - This may take 10-30 minutes depending on your internet

6. **Complete the setup:**
   - Click "Finish" when downloads complete

---

## Step 3: Set Up Firebase

### 3.1 Create Firebase Project

1. **Go to Firebase Console:**
   - Open browser and visit: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Create a new project:**
   - Click **"Add project"** or **"Create a project"**
   - Enter project name: `PolyScores Kenya`
   - Click "Continue"

3. **Configure Google Analytics (optional):**
   - Toggle "Enable Google Analytics" OFF (not needed for basic functionality)
   - Click "Create project"

4. **Wait for project creation:**
   - This takes about 30 seconds
   - Click "Continue" when ready

### 3.2 Add Android App to Firebase

1. **Register your app:**
   - On the Firebase project overview, click the **Android** icon (</>)
   - Or click "Add app" → Android

2. **Enter app information:**
   - **Android package name:** `com.polyscores.kenya` (MUST match exactly)
   - **App nickname:** `PolyScores Kenya` (optional)
   - **Debug signing certificate SHA-1:** (skip for now, can add later)

3. **Download google-services.json:**
   - Click "Download google-services.json"
   - Save the file

4. **Add the file to your project:**
   - Navigate to: `C:\Users\Pi\PolyScoresApp\app\`
   - Delete the placeholder `google-services.json` file
   - Copy your downloaded `google-services.json` to this folder
   - The path should be: `C:\Users\Pi\PolyScoresApp\app\google-services.json`

5. **Click "Next"** in Firebase Console

### 3.3 Add Firebase SDK to Project

The project already has Firebase configured in the build files. Verify:

1. **Check `app/build.gradle.kts`:**
   - Should have: `id("com.google.gms.google-services")`
   - Should have Firebase dependencies

2. **Check `build.gradle.kts` (project level):**
   - Should have: `id("com.google.gms.google-services") version "4.4.0" apply false`

### 3.4 Enable Firebase Services

#### Enable Firestore Database

1. **Go to Firestore:**
   - In Firebase Console, click **"Build"** in the left sidebar
   - Click **"Firestore Database"**

2. **Create database:**
   - Click **"Create database"**

3. **Choose security rules:**
   - Select **"Start in test mode"** (for development)
   - Click **"Next"**

4. **Choose location:**
   - Select a location close to Kenya:
     - `europe-west` (Belgium) - Recommended
     - `asia-south1` (Mumbai) - Alternative
   - Click **"Enable"**

5. **Wait for database creation:**
   - This takes about 1-2 minutes

#### Enable Authentication

1. **Go to Authentication:**
   - Click **"Build"** → **"Authentication"**

2. **Get started:**
   - Click **"Get started"**

3. **Enable Email/Password:**
   - Click **"Email/Password"**
   - Toggle **"Enable"**
   - Click **"Save"**

#### Enable Cloud Messaging (FCM)

FCM is automatically enabled. No action needed.

---

## Step 4: Configure the Project

### 4.1 Open Project in Android Studio

1. **Launch Android Studio**

2. **Open the project:**
   - Click **"Open"** (or File → Open)
   - Navigate to: `C:\Users\Pi\PolyScoresApp`
   - Select the folder and click "OK"

3. **Wait for Gradle sync:**
   - Android Studio will sync Gradle files
   - This downloads dependencies (may take 5-15 minutes)
   - Wait for "Sync Successful" message

### 4.2 Configure Gradle (if needed)

If you see Gradle sync errors:

1. **Update Gradle version:**
   - Open `gradle/wrapper/gradle-wrapper.properties`
   - Ensure it has:
     ```
     distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
     ```

2. **Sync again:**
   - Click "Sync Now" when prompted

### 4.3 Create Local Properties (if needed)

If you see SDK location errors:

1. **Create `local.properties` file:**
   - In the project root, create a new file: `local.properties`
   - Add this line (adjust path if needed):
     ```
     sdk.dir=C\:\\Users\\Pi\\AppData\\Local\\Android\\Sdk
     ```

---

## Step 5: Set Up Firestore Database

### 5.1 Create Collections

In Firebase Console → Firestore Database:

#### Create `teams` Collection

1. Click **"Start collection"**
2. Collection ID: `teams`
3. Click **"Next"**
4. Add first document:
   - Document ID: (leave as auto-generated)
   - Add fields:
     ```
     name: "Technical FC"
     shortName: "TECH"
     logoUrl: ""
     primaryColor: "#009900"
     secondaryColor: "#FFFFFF"
     department: "Technical Studies"
     yearEstablished: 2020
     captainId: ""
     coachName: "John Kamau"
     contactPhone: "+254700000000"
     isActive: true
     createdAt: (select "Timestamp" type, current time)
     ```
5. Click **"Save"**

#### Create `leagues` Collection

1. Click **"Start collection"**
2. Collection ID: `leagues`
3. Add document:
   ```
   name: "Polytechnic League 2024"
   season: "2024"
   isActive: true
   format: "LEAGUE"
   teamIds: []
   createdAt: (Timestamp)
   startDate: (Timestamp)
   endDate: (Timestamp)
   ```

#### Create `matches` Collection

1. Click **"Start collection"**
2. Collection ID: `matches`
3. Add a sample match document:
   ```
   leagueId: "copy_league_id_from_leagues_collection"
   homeTeamId: "copy_team_id_from_teams_collection"
   awayTeamId: "copy_team_id_from_teams_collection"
   homeTeamName: "Technical FC"
   awayTeamName: "Business FC"
   homeTeamLogo: ""
   awayTeamLogo: ""
   homeScore: 0
   awayScore: 0
   matchStatus: "SCHEDULED"
   scheduledTime: (Timestamp - select future time)
   venue: "Main Field"
   round: "Round 1"
   lastUpdated: (Timestamp)
   ```

#### Create `admins` Collection

1. Click **"Start collection"**
2. Collection ID: `admins`
3. Add admin document:
   ```
   email: "admin@polytechnic.ac.ke"
   name: "Sports Administrator"
   role: "SUPER_ADMIN"
   department: "Sports"
   phone: "+254700000000"
   isActive: true
   createdAt: (Timestamp)
   ```

### 5.2 Set Up Firestore Security Rules

1. **Go to Rules tab:**
   - In Firestore Database, click **"Rules"** tab

2. **Replace with these rules:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Allow public read access
    match /{collection}/{document} {
      allow read: if true;
    }

    // Allow authenticated users to write matches
    match /matches/{matchId} {
      allow write: if request.auth != null;
    }

    // Allow authenticated users to write match events
    match /match_events/{eventId} {
      allow write: if request.auth != null;
    }

    // Allow authenticated users to write standings
    match /standings/{leagueId} {
      allow write: if request.auth != null;
    }

    // Admins collection - read only for authenticated
    match /admins/{adminId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

3. **Click "Publish"**

---

## Step 6: Build and Run the App

### 6.1 Create an Android Virtual Device (Emulator)

1. **Open Device Manager:**
   - In Android Studio, click **Tools** → **Device Manager**
   - Or click the Device Manager icon in the toolbar

2. **Create new device:**
   - Click **"Create device"**

3. **Select hardware:**
   - Choose: **Pixel 6** (or any phone)
   - Click **"Next"**

4. **Select system image:**
   - Choose: **API 34 (Android 14)** or **API 33 (Android 13)**
   - Click **"Download"** if not downloaded
   - Click **"Next"**

5. **Configure AVD:**
   - AVD Name: `Pixel_6_API_34`
   - Click **"Finish"**

### 6.2 Build the App

1. **Build → Make Project:**
   - Click **Build** → **Make Project**
   - Or press `Ctrl + F9`
   - Wait for build to complete

2. **Check for errors:**
   - Look at the "Build" tab at the bottom
   - Should show "BUILD SUCCESSFUL"

### 6.3 Run the App

1. **Select the emulator:**
   - In the toolbar, click the device dropdown
   - Select your created emulator

2. **Run the app:**
   - Click the green **Run** button (▶)
   - Or press `Shift + F10`

3. **Wait for emulator to start:**
   - First launch takes 2-5 minutes
   - Subsequent launches are faster

4. **App should launch:**
   - You should see the PolyScores Kenya app
   - Bottom navigation bar should appear
   - Home screen should load (may show empty state)

---

## Step 7: Add Sample Data

To see the app in action, add more data via Firebase Console:

### Add More Teams

In Firestore → `teams` collection, add:

```
Document 2:
  name: "Business FC"
  shortName: "BUS"
  department: "Business Studies"
  isActive: true
  ...

Document 3:
  name: "Engineering United"
  shortName: "ENG"
  department: "Engineering"
  isActive: true
  ...

Document 4:
  name: "Hospitality Rangers"
  shortName: "HOSP"
  department: "Hospitality"
  isActive: true
  ...
```

### Add More Matches

In Firestore → `matches` collection, add:

```
Match 1 (Live):
  homeTeamName: "Technical FC"
  awayTeamName: "Business FC"
  homeScore: 2
  awayScore: 1
  matchStatus: "LIVE"
  ...

Match 2 (Scheduled):
  homeTeamName: "Engineering United"
  awayTeamName: "Hospitality Rangers"
  homeScore: 0
  awayScore: 0
  matchStatus: "SCHEDULED"
  scheduledTime: (future timestamp)
  ...

Match 3 (Finished):
  homeTeamName: "Technical FC"
  awayTeamName: "Engineering United"
  homeScore: 3
  awayScore: 0
  matchStatus: "FULLTIME"
  ...
```

### Create an Admin User (for authentication)

1. **Go to Authentication:**
   - Firebase Console → **Build** → **Authentication**

2. **Add user:**
   - Click **"Add user"**
   - Email: `admin@polytechnic.ac.ke`
   - Password: `admin123` (or your choice)
   - Click **"Add user"**

3. **Copy the UID:**
   - Copy the User UID from the user list

4. **Add to admins collection:**
   - Go to Firestore → `admins` collection
   - Add document with the copied UID as Document ID
   - Add fields as shown earlier

---

## Step 8: Test the App

### Test Basic Features

1. **Home Screen:**
   - Open app
   - Should show matches (if data added)
   - Bottom navigation should work

2. **Matches Screen:**
   - Tap "Matches" in bottom nav
   - Should show all matches
   - Search should filter matches

3. **Standings Screen:**
   - Tap "Standings" in bottom nav
   - Should show league table (if matches completed)

4. **Teams Screen:**
   - Tap "Teams" in bottom nav
   - Should show all teams in grid

5. **Settings Screen:**
   - Tap "Settings" in bottom nav
   - Toggle notification settings

6. **Admin Panel:**
   - Go to Settings → Admin Panel
   - Should see active matches
   - Tap a match to update score

### Test Score Updates

1. **Open Admin Panel**
2. **Tap on a match**
3. **Update score:**
   - Enter home and away scores
   - Select match status (Live, Half-Time, Full-Time)
   - Click "Update"
4. **Verify:**
   - Go to Home screen
   - Score should be updated

---

## Troubleshooting

### Build Errors

**Error: "google-services.json not found"**
- Ensure file is at: `C:\Users\Pi\PolyScoresApp\app\google-services.json`
- Not in `PolyScoresApp\google-services.json` (wrong location)

**Error: "SDK not found"**
- Create `local.properties` in project root:
  ```
  sdk.dir=C\:\\Users\\Pi\\AppData\\Local\\Android\\Sdk
  ```

**Error: "Gradle sync failed"**
- Click "File" → "Invalidate Caches" → "Invalidate and Restart"
- Wait for Android Studio to restart
- Try syncing again

### Runtime Errors

**App crashes on startup:**
- Check Logcat for error messages
- Verify `google-services.json` is correct
- Ensure package name matches Firebase: `com.polyscores.kenya`

**No data showing:**
- Check internet connection
- Verify Firestore has data
- Check Firestore security rules allow read access

**Emulator is slow:**
- Enable Virtualization in BIOS
- Allocate more RAM to emulator (4GB recommended)
- Use a system image with Google Play (often faster)

### Firebase Issues

**"Permission denied" errors:**
- Check Firestore security rules
- Ensure rules allow read access

**FCM notifications not working:**
- Verify FCM is enabled in Firebase
- Check app has notification permissions
- Test on physical device (emulator notifications can be unreliable)

---

## Quick Reference

### Project Structure
```
C:\Users\Pi\PolyScoresApp\
├── app/
│   ├── src/main/
│   │   ├── java/com/polyscores/kenya/
│   │   │   ├── data/          # Models, Repositories
│   │   │   ├── domain/        # Domain layer
│   │   │   ├── presentation/  # UI, ViewModels
│   │   │   └── PolyScoresApplication.kt
│   │   ├── res/               # Resources
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Important Commands

| Action | Shortcut/Menu |
|--------|---------------|
| Build Project | `Ctrl + F9` or Build → Make Project |
| Run App | `Shift + F10` or Run → Run 'app' |
| Sync Gradle | `Ctrl + Shift + O` or File → Sync Project |
| Open Logcat | `Alt + 6` or View → Tool Windows → Logcat |

### Firebase Console URLs

- Firebase Console: https://console.firebase.google.com/
- Firestore Database: https://console.firebase.google.com/project/YOUR_PROJECT/firestore
- Authentication: https://console.firebase.google.com/project/YOUR_PROJECT/authentication

---

## Next Steps

After getting the app running:

1. **Customize branding:**
   - Update colors in `res/values/colors.xml`
   - Change app name in `res/values/strings.xml`
   - Replace app icon

2. **Add more features:**
   - Match details screen
   - Player statistics
   - Multiple leagues support

3. **Prepare for release:**
   - Generate signed APK
   - Test on multiple devices
   - Set up production Firebase rules

---

**Congratulations!** You now have a fully functional live scores app for your polytechnic!

For questions or issues, check the Logcat output in Android Studio or refer to the Firebase Console for data verification.
