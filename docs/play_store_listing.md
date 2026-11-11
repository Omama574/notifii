# Notifii — Google Play Store Listing Specification

---

## 1. Store Metadata

### 1.1 App Title
```text
Notifii - Notification Alarms
```
*(29 characters / 30 max)*

---

### 1.2 Short Description
```text
Turn critical notifications into loud persistent alarms. 100% offline & private.
```
*(78 characters / 80 max)*

---

### 1.3 Full Description
*(Rich text formatted for Google Play Store, ~3,200 characters / 4,000 max)*

```text
Never miss the notification that makes you money. 

Notifii turns high-priority notifications from any app into persistent, impossible-to-miss alarms that ring until you acknowledge them — even when your phone is locked, on silent, or in Do Not Disturb (DND) mode.

100% OFFLINE. ZERO TRACKING. ZERO ADS.
Notifii contains NO internet permission in its code. Your messages, notifications, and personal data never leave your physical device. No account sign-ups, no tracking analytics, and no cloud servers.

WHO NEEDS NOTIFII?
• Freelancers (Upwork, Fiverr): Be the first to respond to high-paying client invitations and job offers before your competitors do.
• Gig & Delivery Drivers (DoorDash, UberEats, Instacart, Amazon Flex): Catch high-dollar orders instantly with custom dollar-amount filters.
• Day Traders & Crypto Investors: Catch urgent Telegram, TradingView, or Binance breakout signals the second they arrive.
• On-Call SREs & Engineers (PagerDuty, Slack, Opsgenie, Sentry): Guarantee that Sev-1 server outages wake you from deep sleep.
• Family & Loved Ones: Make sure emergency messages from your kids or aging parents always break through silent mode.

POWERFUL, INTELLIGENT RULE BUILDER
Create intuitive notification triggers in plain English:
• App Whitelist: Choose specific apps (Slack, Upwork, WhatsApp, SMS, DoorDash, etc.) or monitor all apps.
• Keyword Inclusions & Exclusions: Alarm when text includes "urgent", "invitation", or "offer", while ignoring spam keywords.
• Regex Pattern Matching: Build advanced patterns for power-user needs.
• Value & Amount Extraction: Trigger alarms ONLY if an order total exceeds a set dollar amount (e.g., DoorDash orders > $20).
• Sender & Contact Filters: Fire alarms exclusively for messages from specific clients, managers, or family members.
• Burst Detection: Trigger alarms when someone floods you with messages (e.g., 4 messages within 2 minutes).
• Time Schedules: Limit rules to working hours or specific days of the week.
• Cooldown Timers: Prevent repetitive alarms for active chat conversations.

DESIGNED FOR RAPID RESPONSE
• Persistent Alarm Audio: Plays over Android's ALARM audio stream at your desired volume, cutting through system DND.
• Full-Screen Lock Overlay: View who messaged you and read the full text directly over your lock screen.
• Instant Inline Reply: Reply to messages immediately via quick-reply chips without opening the source app.
• Flip-to-Silence: Simply turn your phone face-down to silence the alarm naturally.
• Text-to-Speech (TTS): Hear the sender and message read aloud when your phone is across the room or mounted in your car.
• Notification History Vault: A private on-device log of captured notifications, even if the sender later deletes or recalls them.

BATTERY-FRIENDLY & OEM READY
Background services shouldn't drain your battery. Notifii uses pure event-driven architecture with zero background polling. Includes a built-in OEM Battery Wizard for Samsung, Xiaomi, OnePlus, and Pixel devices to prevent aggressive task killers from stopping your alerts.

FAIR, HONEST PRICING
• Free Tier: 2 active rules with standard alarm sounds, completely free forever.
• Pro Upgrade: A single, modest one-time purchase ($3.99 - $4.99) unlocks unlimited rules, regex filters, TTS readout, custom audio, and full-screen overlays. No recurring subscriptions. No ads. Ever.

Take back control of your urgent notifications. Install Notifii today and never miss an opportunity again!
```

---

## 2. Graphic Asset Concepts

### 2.1 Feature Graphic (1024 x 500 px)
- **Visual Composition:** Deep charcoal `#0B0B0B` background with subtle cyan lighting.
- **Left Side:** A sleek modern Android smartphone mockup showing the glowing high-contrast Full-Screen Alarm overlay with an Upwork invitation: *"New Contract Invitation ($120/hr)"* and the bright crimson *"DISMISS"* button.
- **Center / Right Typography:**
  - Headline: **"NEVER MISS THE TEXT THAT MAKES YOU MONEY."**
  - Subhead: *"Persistent Alarms for Critical Notifications."*
  - Badge Elements:
    - 🛡️ **100% Offline (No Internet Permission)**
    - ⚡ **Bypasses DND & Silent Mode**
    - 🔒 **Zero Data Collection**

---

### 2.2 Screenshot Concept Suite (8 Screenshots — Phone 1080 x 2400)

| Screenshot # | Headline Banner | Visual Content Shown |
|---|---|---|
| **Screenshot 1** | **Turn Notifications Into Loud Alarms** | Full-Screen Alarm ringing over lock screen with an Upwork job invitation and count-up ringing timer. |
| **Screenshot 2** | **100% Offline. Zero Data Tracking.** | Clean graphic breakdown of the App Manifest showing zero internet permission and complete device privacy. |
| **Screenshot 3** | **Natural Language Rule Builder** | The Rule Editor screen displaying the sentence builder: *"When Upwork sends containing 'invitation' then Play Siren Alarm"*. |
| **Screenshot 4** | **Filter by Amount & Keywords** | DoorDash rule demo highlighting dollar amount extraction: *"Only alarm when order exceeds $20.00"*. |
| **Screenshot 5** | **Instant Inline Direct Reply** | User tapping a quick-reply chip (`"I'm available now!"`) directly from the alarm overlay. |
| **Screenshot 6** | **Burst Detection for Emergencies** | Graphic showing: *"Trigger alarm when 3+ messages arrive within 2 minutes"*. |
| **Screenshot 7** | **Notification History Vault** | Dark timeline feed showing logged notifications with search bar and filter chips. |
| **Screenshot 8** | **Flip Your Phone to Silence** | Minimalist illustration of placing the smartphone face-down on a desk to dismiss the siren. |

---

## 3. Store Categorization & Compliance Details

| Parameter | Specification |
|---|---|
| **Category** | Tools / Productivity |
| **Content Rating** | Everyone (PEGI 3, ESRB Everyone) |
| **Contains Ads** | **No** (Checkbox marked "No" in Play Console) |
| **In-App Purchases** | **Yes** (One-time Pro Unlock and optional Tip Jar) |
| **Target Audience** | 18+ (Professionals, Gig Workers, Freelancers) |
| **Developer Contact** | `support@notifii.app` (or offline contact portal) |

---

## 4. Privacy Policy & Store Declaration Highlights

For submission in Google Play Console's Data Safety section:

1. **Data Collection:**
   - Does this app collect or share user data? **No**.
   - Are notifications transferred to a server? **No, processed strictly in-memory and stored in local Room DB**.
   - Does the app request network access? **No, `android.permission.INTERNET` is not declared**.

2. **Special Permissions Justification:**
   - **`BIND_NOTIFICATION_LISTENER_SERVICE`:** Core app utility. Reads incoming status bar notifications to evaluate user-defined alert rules.
   - **`ACCESS_NOTIFICATION_POLICY`:** Enables Notifii to sound persistent alarms on `STREAM_ALARM` even when the user has system Do Not Disturb active.
   - **`USE_FULL_SCREEN_INTENT`:** Allows displaying the emergency alarm takeover overlay over the lock screen so the user can immediately see and respond to the alert.
