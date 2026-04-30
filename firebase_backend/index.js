const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Listen for new match events (goals, red cards, etc.)
exports.sendEventNotification = functions.firestore
    .document("match_events/{eventId}")
    .onCreate(async (snap, context) => {
        const event = snap.data();

        // We only want to send push notifications for major events
        const isGoal = event.eventType === "GOAL" || event.eventType === "PENALTY_GOAL" || event.eventType === "OWN_GOAL";
        const isRedCard = event.eventType === "RED_CARD";
        
        if (!isGoal && !isRedCard) {
            return null; // Don't notify for minor events like normal yellow cards or subs
        }

        let title = "Match Update";
        if (isGoal) title = "GOAL! ⚽";
        if (isRedCard) title = "RED CARD! 🟥";

        const body = `${event.minute}' - ${event.playerName} (${event.eventType.replace(/_/g, " ")})`;

        const payload = {
            notification: {
                title: title,
                body: body,
            },
            data: {
                type: isGoal ? "goal" : "general",
                matchId: event.matchId,
                eventId: context.params.eventId,
            },
            topic: "all_matches",
        };

        try {
            const response = await admin.messaging().send(payload);
            console.log("Successfully sent message:", response);
            return response;
        } catch (error) {
            console.log("Error sending message:", error);
            return null;
        }
    });

// Listen for match status changes (e.g. Kickoff, Full Time)
exports.sendMatchStatusNotification = functions.firestore
    .document("matches/{matchId}")
    .onUpdate(async (change, context) => {
        const newValue = change.after.data();
        const previousValue = change.before.data();

        // Check if status changed
        if (newValue.matchStatus === previousValue.matchStatus) {
            return null;
        }

        const status = newValue.matchStatus;
        
        // We only notify for important status changes
        const isKickoff = status === "LIVE";
        const isFullTime = status === "FULLTIME";

        if (!isKickoff && !isFullTime) {
            return null;
        }

        const title = isKickoff ? "Match Started! ⚽" : "Full Time 🏁";
        const body = `${newValue.homeTeamName} vs ${newValue.awayTeamName}`;

        const payload = {
            notification: {
                title: title,
                body: body,
            },
            data: {
                type: isKickoff ? "match_start" : "full_time",
                matchId: context.params.matchId,
            },
            topic: "all_matches",
        };

        try {
            const response = await admin.messaging().send(payload);
            console.log("Successfully sent status message:", response);
            return response;
        } catch (error) {
            console.log("Error sending status message:", error);
            return null;
        }
    });
