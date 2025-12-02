# 🌬️ Smart Air


Smart Air is a kid-friendly asthma support app designed to help children, parents, and healthcare providers work together in managing asthma safely and confidently. Inspired by pediatric asthma action plans, Smart Air brings symptom logging, inhaler-technique coaching, and parent-controlled data sharing into one supportive mobile app.
Living with asthma can be overwhelming for families — especially when symptoms fluctuate or a child struggles with medication technique, schedules, or knowing when symptoms are serious. Smart Air aims to make daily asthma management easier by offering a playful, encouraging interface for kids while giving parents a clear outlook of trends and safety alerts.
Smart Air is not a medical diagnostic tool or a replacement for emergency services. It is a companion that encourages strong habits, better adherence, and more informed conversations with care providers.



## ⭐ What Smart Air Offers
Smart Air includes several clinically relevant features designed specifically for children, their families, and their healthcare teams.

## 👦 For Kids
Friendly Symptom & Medication Logging
Kids can quickly record:
Rescue inhaler use
Controller medication
How they’re feeling (“Better / Same / Worse”)
Simple symptom check-ins like cough, wheeze, or activity limits
Everything is simplified so kids can log their day with just a few taps.
Technique Helper
A guided sequence teaches proper inhaler technique using animations and step-by-step prompts:
Seal lips
Slow deep breath
Hold for ~10 seconds
Wait between puffs
Spacer/mask reminders
Kids earn streaks and badges for consistent, high-quality technique.

## 👨‍👩‍👧 For Parents
Child Management & Oversight
Parents can link multiple children, each with their own dashboard, history, and sharing controls.
Safety Tools
Peak-flow (PEF) entry
Automatic zone calculation (Green / Yellow / Red)
One-tap triage (“Having trouble breathing?”)
Identifies red-flag symptoms
Suggests next steps
Times rechecks and escalates if needed
Push notifications for:
Red-zone days
Rapid rescue repeats
Low medication canister
“Worse after dose”
Triage escalation
Inventory Tracking
Parents can track:
Medication expiry
Amount remaining
Purchase dates
Replacement reminders
Trend Dashboard
A parent-friendly, at-a-glance view:
Today’s zone
Last rescue use
Weekly rescue count
7-day and 30-day trends

## 🩺 For Healthcare Providers
Providers get read-only access only when a parent enables sharing.
Shareable Report
Parents can export or share a concise report showing:
Rescue & controller usage
Symptom burden
Trigger patterns
Zone distribution
Triage history
Time-series and categorical charts
This helps support real clinical visits and discussions.

## 🔐 Privacy & Sharing
Smart Air puts parents entirely in control.
Parent-Controlled Sharing
Parents choose exactly what is shared with a provider:
Rescue logs
Controller adherence
Symptoms
Triggers
Peak flow
Triage incidents
Dashboard charts
Everything can be toggled on/off at any time.
Invite-Based Access
Providers join through a one-time code or link that:
Expires in 7 days
Can be revoked instantly
Children only see their own data; nothing is shared automatically.

## 🔧 Feature Details
Symptom, Trigger & History Tracking
Daily check-ins
Multiple triggers (exercise, cold air, pets, smoke, illness, etc.)
3–6 month history viewer with filters
Export as PDF or CSV
Motivation, Streaks & Badges
Controller streaks
Technique streaks
Badges for strong habits
Flexible threshold settings

## 🧱 How It Works
Tech Stack
Android (Java)
Firebase Authentication
Firebase Firestore
Firebase Cloud Messaging
RecyclerView / CardView
SharedPreferences
JSON + GSON
MVP architecture (login module)
Architecture Notes
Login flow follows MVP for clean separation and easy testing
RecyclerViews + adapters used throughout
Firebase used for persistent data
Local caching improves performance

## ▶️ Getting Started
Clone the repository:
git clone https://github.com/Amelie-png/B07-12.git


Open the project in Android Studio
Set up Firebase (google-services.json)
Run the app using the Run button

## 🧪 Testing
The login module is fully tested using:
JUnit
Mockito for mocking views & models
MVP presenter logic coverage

## 📝 Disclaimer
Smart Air provides supportive tools for asthma management.
It does not replace medical advice, diagnosis, or emergency care.
