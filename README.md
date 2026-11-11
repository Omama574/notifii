# Notifii — every notification at the right volume

Offline Android app (Kotlin, Jetpack Compose, Material 3). Every app or person sits on one rung of a ladder:

| Rung | What happens |
|---|---|
| **Siren** | Rings through silent and Do Not Disturb until handled: rising alarm, read aloud, flip to silence, reply from the alarm screen |
| **Ring** | A normal notification (the default for every app) |
| **Later** | Held quietly, delivered in a digest at 12:00 and 18:00 (and when Night ends) |
| **Hide** | Never shown |

Everything is kept in the **Vault**, so nothing is ever lost. **Exceptions** are made from a real notification ("Upwork is Later, except 'invited you' → Siren"), previewed against the last week of the Vault before saving, and every decision answers **Why?**. **Night** holds Ring until morning; Siren still rings.

Tabs: **Now** (what's waiting, triage cards for your loudest unsorted apps, today's feed) · **Ladder** (the whole setup on one screen) · **Vault** (search everything).

**Privacy:** no `INTERNET` permission. The build fails if any library adds it back. No account, no analytics, and the database is excluded from cloud backup.

**Money:** free covers 1 Siren and 1 exception; **Pro** is a one-time Google Play purchase (`notifii_pro`) that removes both limits. No subscription, no ads.

## Build
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
JDK 17, Android SDK 36. Status and next steps: [PROGRESS.md](PROGRESS.md).

> The `docs/` folder describes the earlier rules-builder concept and is kept for reference; this README and PROGRESS.md describe what's built.
