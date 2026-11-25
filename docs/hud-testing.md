# Gameplay HUD testing (Team ECE)

Test plan for Requirement 8 - Gameplay HUD (KAN-36). It has two parts:
automated unit tests that run in CI on every pull request, and a manual
checklist to run in the game before a release or a demo.

## 1. Automated tests (KAN-35)

The HUD state classes have no drawing code and take the current time as a
parameter, so they are tested with a fake clock. The tests live in `test/`
(JUnit 4) and run in the `test` job of `.github/workflows/ci.yml`.

| Test class | What it checks |
| --- | --- |
| `HudAnimatorTest` | score count-up and flash, lost/gained life blink, low-life pulse, level intro pulse, coin and diamond flash |
| `CurrencyHudTest` | rolling balance, +N/-N popup merging, spending popup, animations off, compact numbers (12.3K) |
| `ComboTrackerTest` | multiplier tiers, combo timeout, fade-out, pause keeps the combo window |
| `ExtraLifeTrackerTest` | one life per 1000 points, max lives cap, progress bar value |
| `StageHudTest` | wave progress, boss levels |
| `BossHudTest` | boss phase count and current phase |
| `RunStatsTest` | run timer, pauses and long gaps ignored, m:ss format |
| `PickupPopupsTest` | "+N" popup path, fade, expiry, cap |
| `HudEventsTest` | combo break on PLAYER_HIT, kill flash, run reset at stage 1, detach |

### Run them locally

macOS / Linux (JUnit 4.13.2 and Hamcrest 1.3 jars in `lib/`):

```
javac -d bin $(find src -name '*.java')
javac -d test-bin -cp bin:lib/junit.jar $(find test -name '*.java')
java -cp test-bin:bin:res:lib/junit.jar:lib/hamcrest.jar org.junit.runner.JUnitCore engine.HudAnimatorTest engine.CurrencyHudTest engine.ComboTrackerTest
```

IntelliJ IDEA: right-click `test` > Mark Directory as > Test Sources Root,
open a test class, use the quick fix "Add JUnit 4 to classpath", then
right-click `test` > Run 'All Tests'.

## 2. Manual checklist

Start the game with `java -cp bin:res engine.Core`. Delete the `hud-settings`
file first to start from the default HUD options.

| # | Feature | Steps | Expected result | Screenshot |
| --- | --- | --- | --- | --- |
| 1 | Score | Shoot enemies in stage 1 | Score rolls up to the new value and flashes yellow; best score to beat shown above it | `score-hud.png` |
| 2 | Combo | Kill 5 enemies in a row within 1.5 s each | "x2" combo text under the item HUD, bar empties, fades out after the combo ends | `score-hud.png` |
| 3 | Lives | Get hit once | Lost life icon blinks then disappears; on the last life the label pulses red | `hud-animations.png` |
| 4 | Extra life | Reach 1000 points | Bar under the lives fills, flashes, one life is added (up to the max) | `score-hud.png` |
| 5 | Level and wave | Start any stage | LEVEL pulses during the countdown; "ENEMIES n/m" bar goes down with each kill | `stage-hud.png` |
| 6 | Stage clear | Clear a stage | Banner slides in with the stage points, then the next line | `stage-hud.png` |
| 7 | Boss warning | Reach stage 5 or 10 | Blinking boss warning above the countdown | `stage5.png` |
| 8 | Boss bar | Hit the boss | "BOSS PHASE n/m", pink trail, white hit flash, colour changes per phase | `boss-phases.png` |
| 9 | Currency | Pick up coins | Coin balance rolls up, "+N" popup next to it, icon pulses | `currency-hud.png` |
| 10 | Coin popups | Pick up a falling coin | "+N" flies from the coin to the balance and fades | `run-stats-popups.png` |
| 11 | Run stats | Play 10 s, press P | TIME m:ss and KILLS n bottom left; timer stops while paused | `run-stats-popups.png` |
| 12 | Pause | Press P | Pause menu with the summary panel (score, stage, kills, stage time, coins, ship) | `hud-ship-shop.png` |
| 13 | Settings | Settings > HUD opacity / size / animations | HUD follows the chosen opacity and size; animations off shows final values at once | `hud-animations-toggle.png` |
| 14 | Shop hint and slots | Have enough coins for a shop item | "$" pulses next to the coins; empty life slots shown up to the max | `hud-ship-shop.png` |
| 15 | Layout | Play a boss stage with an item and an achievement popup | No HUD element overlaps another (see `docs/hud-overlays.md`) | `hud-layout.png` |

## 3. Last run

`main` at 3327f9a (after PR #12), Ubuntu 24.04, Java 21:

- 34 unit tests pass.
- Steps 1 to 15 were checked in the game when each feature was merged; the
  screenshots above come from those runs.
- To do before the final demo (December 8): one full pass of the checklist
  on Windows with the latest `main`.

Bug found during the run, logged in Jira as KAN-48:

- Restarting a stage from the pause menu subscribes the damage dim effect to
  `PLAYER_HIT` again without removing the old one, so each restart adds one
  more listener for the rest of the stage. Visible effect: none yet, the
  listeners do the same thing, but it leaks. `initialize()` should
  unsubscribe the previous `damageDim` first, like `HudEvents` does.
