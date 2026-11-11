# Notifii — Product Requirements Document

> **Version:** 1.0  
> **Date:** November 2026  
> **Author:** Notifii Product Team  
> **Status:** Draft

---

## 1. Executive Summary

**Notifii** is an offline-first, privacy-focused Android application that turns any incoming notification into a persistent, impossible-to-miss alarm. Users define rules based on app, keywords, senders, patterns, and schedules; when a notification matches, Notifii plays a loud alarm on the ALARM audio stream — bypassing Do-Not-Disturb and silent mode — and presents a full-screen overlay with message preview, quick-reply chips, and dismiss controls.

The app collects **zero user data**, requires **no internet permission**, and stores everything locally on-device. Monetization is a one-time Pro upgrade ($3.99–$4.99) with an optional tip jar — no subscriptions, no ads, no tracking.

**Core value proposition:** *"Never miss the text that makes you money. 100% Offline. Zero Tracking."*

---

## 2. Problem Statement

### The Problem

Critical notifications from apps like Upwork, DoorDash, Slack, TradingView, and family contacts are easily missed due to:

- **Silent/DND mode** — users silence their phone at night or during focus time, but still need certain alerts to break through.
- **Notification overload** — hundreds of daily notifications cause banner blindness; truly important ones drown in noise.
- **Platform limitations** — Android's native notification channels give per-app control, but no per-keyword, per-sender, or per-pattern filtering.
- **Missed earning opportunities** — freelancers lose contracts, gig workers miss high-paying orders, and traders miss signals because they didn't see a notification in the 30-second window that mattered.
- **Family safety** — emergency messages from aging parents or children go unnoticed for hours.

### The Gap

Existing solutions are either:
- **Too complex** (Tasker, MacroDroid) — powerful but require programming-like configuration.
- **Too limited** (Awake Now!) — basic alarm-on-notification without filtering.
- **Missing key features** (BuzzKill, Alertify) — no full-screen alarm overlay, no inline reply, no notification history vault, no burst detection.
- **Privacy-invasive** — many competitors request internet permission for analytics or ads.

### The Opportunity

Notifii combines the **ease of BuzzKill's natural-language rule builder** with the **power of Tasker's conditional logic**, adds **features no competitor offers** (persistent alarm with full-screen overlay, inline direct reply, notification history vault, TTS, burst detection, value extraction), and wraps it all in a **zero-internet, zero-tracking privacy guarantee**.

---

## 3. Target Users / Personas

### Persona 1: "The Freelancer" — Alex, 28, Upwork Freelancer

| Attribute | Detail |
|---|---|
| **Occupation** | Full-stack developer, freelances on Upwork and Fiverr |
| **Pain Point** | Misses new job invitations; by the time they see the notification, 5 other freelancers have already applied |
| **Behavior** | Keeps phone on silent during deep work; checks Upwork every 30 min manually |
| **Need** | Alarm that fires ONLY for Upwork "invitation" notifications, even in DND |
| **Rule** | When **Upwork** sends containing **"invited you"** → Play alarm |
| **Willingness to Pay** | $4.99 one-time if it lands them even one extra contract |

### Persona 2: "The Gig Worker" — Maria, 34, DoorDash Driver

| Attribute | Detail |
|---|---|
| **Occupation** | DoorDash and UberEats driver, supplements income with Instacart |
| **Pain Point** | Misses high-paying orders while driving; standard notification chime blends with music |
| **Behavior** | Runs 3 delivery apps simultaneously; phone is mounted on dashboard |
| **Need** | Loud alarm ONLY for orders above a threshold (e.g., $20+), not every $4 McDonalds run |
| **Rule** | When **DoorDash** sends matching regex **\$(\d+)** where captured value > 20 → Play alarm |
| **Willingness to Pay** | $3.99, pays for itself with one extra good order |

### Persona 3: "The Day Trader" — Raj, 42, Part-Time Crypto Trader

| Attribute | Detail |
|---|---|
| **Occupation** | Software engineer by day, crypto trader in off-hours |
| **Pain Point** | Misses trading signals in Telegram groups; by the time he checks, the price has moved |
| **Behavior** | Subscribed to 5 Telegram signal channels; gets 200+ messages/day, only 2-3 matter |
| **Need** | Alarm only when specific keywords appear ("BUY", "SELL", "ALERT") from specific Telegram groups |
| **Rule** | When **Telegram** sends containing **"BUY"** OR **"SELL"** OR **"ALERT"** from **"CryptoSignals"** → Play alarm + TTS |
| **Willingness to Pay** | $4.99, would pay more if it helps catch one good trade |

### Persona 4: "The On-Call Engineer" — Sarah, 31, SRE at a Startup

| Attribute | Detail |
|---|---|
| **Occupation** | Site Reliability Engineer, on-call rotation every 3 weeks |
| **Pain Point** | PagerDuty notifications don't always cut through DND; has been woken by angry manager calls instead |
| **Behavior** | Uses PagerDuty, Slack (#incidents), and Sentry for alerting |
| **Need** | Guaranteed wake-up alarm for P1 incidents, with escalation if unacknowledged |
| **Rule** | When **PagerDuty** OR **Slack** sends containing **"P1"** OR **"SEV1"** OR **"CRITICAL"** → Play alarm, escalate if unread after 2 min |
| **Willingness to Pay** | $4.99 instantly; job depends on it |

### Persona 5: "The Worried Parent" — Linda, 56, Mother of Two Teenagers

| Attribute | Detail |
|---|---|
| **Occupation** | School administrator |
| **Pain Point** | Keeps phone on silent during meetings; worried about missing emergency texts from kids |
| **Behavior** | Checks phone obsessively between meetings |
| **Need** | Alarm for ANY message from her children's phone numbers, regardless of app |
| **Rule** | When **any app** sends from **"Mom's Phone"** OR **"Dad's Phone"** contacts → Play alarm |
| **Willingness to Pay** | $3.99 for peace of mind |

### Persona 6: "The Power Napper" — Jake, 25, College Student

| Attribute | Detail |
|---|---|
| **Occupation** | Computer science student |
| **Pain Point** | Takes 30-min naps; standard alarms don't adjust to notification-based triggers |
| **Behavior** | Naps between classes; wants to be woken if study group messages or professor posts |
| **Need** | Alarm for specific app notifications during nap window |
| **Rule** | When **GroupMe** sends from **"CS 401 Study Group"** between **1pm–3pm** → Play loud alarm |
| **Willingness to Pay** | Free tier is fine; might upgrade for TTS |

---

## 4. Core Features (MoSCoW Prioritization)

### Must Have (MVP — Phase 1)

| # | Feature | Description |
|---|---|---|
| M1 | **NotificationListenerService** | Intercept all incoming notifications across all apps |
| M2 | **Basic Rule Engine** | Evaluate notifications against rules with: app filter, keyword inclusion/exclusion |
| M3 | **Persistent Alarm Player** | Play alarm on STREAM_ALARM, bypass DND/silent, ring until dismissed |
| M4 | **Rule CRUD** | Create, read, update, delete, enable/disable rules |
| M5 | **Home Dashboard** | Show active rule count, recent alerts, service status |
| M6 | **Rule Editor Screen** | Natural-language rule builder UI |
| M7 | **Settings Screen** | Default alarm sound, vibration toggle, service controls |
| M8 | **Onboarding Flow** | Permission wizard: Notification Access → DND Override → Battery Optimization |
| M9 | **Foreground Service** | Persistent notification indicating Notifii is active |
| M10 | **Room Database** | Local persistence for rules and settings |

### Should Have (Phase 2)

| # | Feature | Description |
|---|---|---|
| S1 | **Notification History Vault** | Log all matched notifications locally, including deleted/recalled |
| S2 | **Burst Detection** | Trigger when N messages arrive in X minutes |
| S3 | **Regex Pattern Matching** | Advanced pattern matching for power users |
| S4 | **Text-to-Speech** | Read notification content aloud |
| S5 | **Sender/Contact Filter** | Filter by notification sender or contact name |
| S6 | **Cooldown Timer** | Don't re-trigger within X seconds of last alarm |
| S7 | **Time Schedule** | Rules only active during configured hours/days |

### Could Have (Phase 3)

| # | Feature | Description |
|---|---|---|
| C1 | **Full-Screen Alarm Overlay** | Lock-screen overlay with message preview, dismiss, snooze, reply |
| C2 | **Inline Direct Reply** | Reply via RemoteInput without opening source app |
| C3 | **Quick Reply Chips** | Preset canned responses for fast replies |
| C4 | **Flip-to-Silence** | Accelerometer detects phone flip to dismiss alarm |
| C5 | **Screen ON/OFF Awareness** | Subtle popup if screen is on; full alarm if screen is off |
| C6 | **Escalating Nag** | If unread for X minutes, alarm volume/frequency escalates |
| C7 | **Group Chat Filter** | Trigger only on @mentions or DMs, ignore group noise |
| C8 | **Number/Amount Extraction** | Regex capture groups for value-based triggering (e.g., order > $20) |

### Won't Have Now (Phase 4+)

| # | Feature | Description |
|---|---|---|
| W1 | **Pro IAP** | One-time in-app purchase for unlimited rules and premium features |
| W2 | **Tip Jar** | Optional donations ($1/$3/$10) |
| W3 | **JSON Backup/Restore** | Export/import rules and settings as local JSON file |
| W4 | **OEM Battery Optimization Wizard** | Guided setup for Samsung/Xiaomi/OnePlus/Huawei |
| W5 | **Custom Alarm Sounds** | Pick from device storage or record custom alarm |

---

## 5. User Stories

| ID | As a... | I want to... | So that... | Priority |
|---|---|---|---|---|
| US-01 | Freelancer | Create a rule that alarms on Upwork "invitation" notifications | I respond first and win the contract | Must |
| US-02 | User | Grant notification access through a guided wizard | I don't have to search through Android settings | Must |
| US-03 | User | See a dashboard showing my active rules and recent alerts | I know Notifii is working at a glance | Must |
| US-04 | User | Enable/disable rules with a toggle | I can quickly pause rules without deleting them | Must |
| US-05 | User | Hear a loud alarm even when my phone is on silent/DND | I never miss a critical notification | Must |
| US-06 | User | Dismiss an alarm from the notification or lock screen | I can stop the noise quickly | Must |
| US-07 | Gig worker | Set a rule with regex to only alarm on orders above $20 | I don't waste time on low-paying deliveries | Should |
| US-08 | Trader | Get alarms only from specific Telegram group senders | I'm alerted only to real trading signals, not chat noise | Should |
| US-09 | On-call engineer | Have the alarm escalate if I don't acknowledge within 2 min | Critical incidents never go unnoticed even if I'm deeply asleep | Could |
| US-10 | Parent | Set rules for any message from my children's contacts | I have peace of mind during meetings | Should |
| US-11 | User | View a history of all notifications that triggered alarms | I can review what I missed and track patterns | Should |
| US-12 | User | Reply to a notification directly from the alarm screen | I can respond instantly without opening the source app | Could |
| US-13 | User | Flip my phone face-down to silence the alarm | I can dismiss it naturally without looking at the screen | Could |
| US-14 | User | Backup my rules to a JSON file on my device | I can restore them after a phone reset or transfer | Won't (now) |
| US-15 | Power user | Use burst detection (5 msgs in 2 min → alarm) | I'm alerted to message storms that indicate something urgent | Should |
| US-16 | User | Set a cooldown so the same rule doesn't fire within 30 seconds | I'm not bombarded by repeated alarms for the same conversation | Should |
| US-17 | User | Restrict rules to specific days and hours | Work rules don't fire on weekends | Should |
| US-18 | User | Have notification content read aloud via TTS | I can hear who texted me while driving/cooking | Should |
| US-19 | User | See a full-screen alarm overlay on my lock screen | I can read the message preview and act without unlocking | Could |
| US-20 | User | Upgrade to Pro with a single one-time payment | I get unlimited rules without a subscription | Won't (now) |

---

## 6. Non-Functional Requirements

### 6.1 Performance

| Requirement | Target |
|---|---|
| Notification processing latency | < 100ms from receipt to rule evaluation complete |
| Alarm trigger latency | < 500ms from rule match to audible alarm |
| App cold start time | < 1.5 seconds to interactive |
| Database query time | < 50ms for rule evaluation queries |
| Memory footprint | < 60MB RAM during idle monitoring |
| Background CPU usage | < 1% average when monitoring (no active alarm) |

### 6.2 Battery

| Requirement | Target |
|---|---|
| Background battery drain | < 2% per 8-hour monitoring session |
| No wake locks beyond alarm playback | Zero unnecessary wake locks |
| Efficient Room queries | Batch inserts for notification logs |
| No polling | Event-driven architecture only |

### 6.3 Privacy & Security

| Requirement | Detail |
|---|---|
| No internet permission | `android.permission.INTERNET` MUST NOT appear in AndroidManifest.xml |
| No analytics | Zero crash reporting, usage tracking, or telemetry SDKs |
| No user accounts | No registration, login, or cloud sync |
| Local-only storage | All data stays on-device in Room DB |
| No third-party SDKs | Only AndroidX and Google Play Billing (for IAP, does not transmit user data) |
| Notification content handling | Content processed in-memory, logged only if rule matches, never transmitted |

### 6.4 Offline Capability

| Requirement | Detail |
|---|---|
| Full functionality offline | 100% of features work without any network connection |
| No network-dependent features | IAP validated via Play Store cache; tip jar requires brief network (handled gracefully) |
| Backup/restore | JSON file saved to local device storage, not cloud |

### 6.5 Reliability

| Requirement | Target |
|---|---|
| Service uptime | 99.9% (listener service must survive all OEM battery optimizations) |
| Crash rate | < 0.1% session crash rate |
| Alarm delivery | 100% alarm delivery for matched notifications when service is running |

### 6.6 Compatibility

| Requirement | Detail |
|---|---|
| Minimum SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 35 |
| Tested OEMs | Samsung, Xiaomi/Redmi, OnePlus, Oppo/Realme, Huawei, Google Pixel |
| Screen sizes | Phone (360dp–440dp width), Tablet (basic layout adaptation) |

---

## 7. Success Metrics

### Launch Metrics (First 90 Days)

| Metric | Target |
|---|---|
| Google Play installs | 5,000+ |
| Play Store rating | ≥ 4.5 stars |
| Daily Active Users (DAU) | 1,500+ |
| Free → Pro conversion rate | 8–12% |
| Uninstall rate (Day 7) | < 40% |

### Growth Metrics (6–12 Months)

| Metric | Target |
|---|---|
| Total installs | 50,000+ |
| Monthly Active Users (MAU) | 15,000+ |
| Pro revenue | $10,000+ cumulative |
| Organic search ranking | Top 5 for "notification alarm android" |
| Average rules per user | ≥ 2.5 |

### Engagement Metrics

| Metric | Target |
|---|---|
| Alarms triggered per active user per day | 3–8 |
| Quick reply usage (Pro users) | 30%+ of alarm dismissals |
| Notification history views per week | 5+ per active user |
| Rules created per user (first week) | ≥ 2 |

---

## 8. Monetization Strategy

### Free Tier
- **2 active rules** (can create more but only 2 enabled at a time)
- Standard built-in ringtones (5 alarm sounds)
- Basic keyword matching (inclusion only)
- Basic app filter
- Notification history (last 100 entries)
- Standard alarm (no full-screen overlay on free)

### Pro Tier — One-Time Purchase ($3.99–$4.99)
- **Unlimited active rules**
- Custom alarm sounds (pick from device or record)
- Text-to-Speech readout
- Regex pattern matching
- Burst detection
- Keyword exclusion
- Number/amount extraction
- Full-screen alarm overlay
- Quick reply chips (custom presets)
- Inline direct reply
- Escalating nag
- Unlimited notification history
- JSON backup/restore
- Priority support (community forum)

### Tip Jar
- Optional appreciation donations: $1 / $3 / $10
- Displayed in Settings → "Buy the Developer a Coffee"
- Purely cosmetic thank-you badge in app

### Pricing Rationale
- **One-time payment** eliminates subscription fatigue — users prefer this for utility apps.
- $3.99–$4.99 is consistent with BuzzKill ($2.49–$3.99) and FilterBox ($3.99), hitting the "impulse buy" threshold.
- **No ads ever** — privacy commitment and clean UX.
- Free tier is genuinely useful (2 rules handles the primary use case for most users), encouraging word-of-mouth.

---

## 9. Competitive Analysis

| Feature | **Notifii** | **BuzzKill** | **Alertify** | **FilterBox** | **Awake Now!** | **Tasker** |
|---|---|---|---|---|---|---|
| **Price** | Free + $3.99–$4.99 one-time | $2.49–$3.99 one-time | Freemium | $3.99 lifetime | Free | $3.49 one-time |
| **Internet Required** | ❌ No | ❌ No | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| **Keyword Filter** | ✅ Include + Exclude | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Regex Matching** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Sender Filter** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **Burst Detection** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ (complex) |
| **Time Schedule** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| **Persistent Alarm (STREAM_ALARM)** | ✅ | ❌ (notification only) | ✅ | ❌ | ✅ (basic) | ✅ |
| **Full-Screen Overlay** | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Inline Reply** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Quick Reply Chips** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **TTS Readout** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Notification History** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Flip-to-Silence** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ (complex) |
| **DND Bypass** | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ |
| **Value Extraction** | ✅ ($amount > threshold) | ❌ | ❌ | ❌ | ❌ | ✅ (complex) |
| **OEM Battery Wizard** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Backup/Restore** | ✅ (local JSON) | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Natural Language UI** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Learning Curve** | Low | Low | Low | Low | Very Low | Very High |
| **Installs** | New | 100K+ | 10K+ | 50K+ | 1K–5K | 1M+ |

### Competitive Advantages
1. **Only app combining persistent alarm + full-screen overlay + inline reply** — no competitor offers all three.
2. **Privacy guarantee** — zero internet permission, auditable in APK manifest.
3. **Value extraction** — unique ability to parse dollar amounts and trigger conditionally.
4. **Burst detection** — catches message storms that individually don't match keywords.
5. **Notification history vault** — captures even deleted/recalled messages.
6. **OEM battery wizard** — solves the #1 support complaint for notification apps.

---

## 10. Out of Scope / Future Roadmap

### Explicitly Out of Scope for V1
- ❌ Cloud sync / cross-device sync
- ❌ iOS version
- ❌ Wear OS companion app
- ❌ Widget (home screen)
- ❌ AI/ML-based notification classification
- ❌ Integration with specific apps via their APIs
- ❌ Multi-language / localization (English only for V1)
- ❌ Notification grouping / threading intelligence
- ❌ Custom alarm sound recording within the app

### Future Roadmap (V2+)
| Timeline | Feature | Rationale |
|---|---|---|
| V2.0 (Month 4) | **Wear OS integration** | Alarm on watch for when phone is in another room |
| V2.0 | **Home screen widget** | Glanceable active rule count + last alert |
| V2.1 (Month 6) | **Localization** | Spanish, Portuguese, Hindi, Japanese — top Android markets |
| V2.2 (Month 8) | **Rule templates marketplace** | Pre-built rules for popular apps (Upwork, DoorDash, etc.) shared via JSON |
| V3.0 (Month 12) | **Automation chains** | If alarm dismissed → auto-reply "On my way" → open app |
| V3.0 | **Smart scheduling** | ML-based learning of when user typically wants alarms |
| V3.x | **Tasker/MacroDroid plugin** | Expose Notifii rules as Tasker conditions/actions |

---

## Appendix A: Glossary

| Term | Definition |
|---|---|
| **Rule** | A user-defined configuration specifying which notifications should trigger an alarm |
| **Notification Listener** | Android system service that allows apps to read all incoming notifications |
| **STREAM_ALARM** | Android audio stream designed for alarms; typically at max volume and bypasses DND |
| **DND** | Do Not Disturb — Android's built-in notification silencing mode |
| **Burst Detection** | Triggering an alarm when N notifications arrive within X minutes |
| **Cooldown** | Minimum time between repeated alarm triggers for the same rule |
| **RemoteInput** | Android API allowing apps to reply to notifications without opening the source app |
| **OEM** | Original Equipment Manufacturer (Samsung, Xiaomi, etc.) — relevant for battery optimization variations |
| **IAP** | In-App Purchase |
| **TTS** | Text-to-Speech |

---

## Appendix B: Privacy Commitment

Notifii's privacy model is a core differentiator and marketing asset:

1. **No `android.permission.INTERNET`** — physically impossible for the app to send data anywhere.
2. **No analytics SDKs** — no Firebase Analytics, no Crashlytics, no Mixpanel, no Amplitude.
3. **No user accounts** — no email, no phone number, no sign-up of any kind.
4. **No advertising SDKs** — no AdMob, no Facebook Audience Network, no mediation.
5. **Auditable** — users and reviewers can decompile the APK and verify the manifest.
6. **Notification data** — processed in volatile memory; only logged to local Room DB for matched notifications with explicit user consent (History Vault feature).
7. **Backup files** — JSON files stored to user-selected local directory, never uploaded.

This is not just a policy — it is an architectural guarantee enforced by the Android permission system.
