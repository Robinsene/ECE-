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
| `BossMusicTest` | boss track, last-phase track, silence when the boss or the player dies |
| `GameModeTest` | stage order of every mode, Survival loops and difficulty, boss stages past 10 |
| `SpaceOperaTest` | sectors, boss names, hyperspace timing, briefing typewriter |
| `RunSummaryTest` | end-of-run lines, records, best combo and bosses, Boss Rush run start |
| `AttackPatternTest` | fan and aimed shots per stage, Survival tiers, aimed speed clamp |
| `ModeScoresTest` | one score file per mode, old files still load, Survival stage |
| `KeyBindingsTest` | default keys, swap on conflict, Esc refused, key names |

The other teams' regression programs in `tests/` and the PMD quality gate
also run in CI (see `docs/code-quality.md`).

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
| 16 | Modes menu | Title > Modes | 4 modes with descriptions; arrows / mouse choose, esc back; Play still starts Classic with the tutorial | `game-modes.png` |
| 17 | Boss Rush | Modes > Boss Rush | Starts on stage 5 (mid boss, no tutorial), then stage 10, then the score screen | `game-modes.png` |
| 18 | Survival | Modes > Survival, clear stage 10 (or start with `-Dgame.startLevel=10`) | Stage 11 starts, enemies faster, 6 columns; boss again on stage 15 | - |
| 19 | Space Opera | Modes > Space Opera | Briefing types out over the fanfare (space skips, then launches); hyperspace jump, sector card, planet, green / red laser bolts, blaster sound; stage 5 boss named "WARDEN FRIGATE" | `space-opera.png` |
| 20 | Boss music | Reach a boss stage | Boss music, faster track after the boss's first phase, silence when it dies | - |
| 21 | Attack patterns | Play stage 4 and stage 7 | Fan of 3 bullets from stage 4, shots aimed at the ship from stage 7; stages 1-3 unchanged | `attack-patterns.png` |
| 22 | Run stats | Lose a run | Score screen shows mode, stages, time, kills, best combo, bosses, coins; "NEW RECORD" when a record is beaten | `run-summary.png` |
| 23 | Scores per mode | High scores, left / right | One list per mode; Survival shows the stage reached; old Classic scores still there | `mode-scores.png` |
| 24 | Key remapping | Settings > Shoot > space, press Z | Row shows "press a key", then "Shoot: Z"; Z shoots in game, space no longer does; the choice is kept after a restart | `key-bindings.png` |

## 3. Last run

KAN-62, on the branch with every feature of Nov 28 (fanfare, CI, run stats,
attack patterns, scores per mode, key remapping), Ubuntu 24.04, Java 21:

- 65 unit tests pass; TwoHitEnemy 169, FinalBossAttack 125 and
  EffectIntegration 71 checks pass; PMD: 0 violations.
- Steps 16, 17, 19, 21, 22, 23 and 24 run in the game with a scripted
  player (screenshots above). In step 21, about 3 enemy shots out of 4 on
  stage 8 went sideways (fan or aimed).
- Steps 1 to 15 were checked when each HUD feature was merged.
- Still to do by hand before the final demo (December 8): one full pass of
  steps 1 to 24 on Windows, and step 18 played for real up to stage 15.

Bugs found and fixed:

- KAN-48: restarting a stage kept the old damage dim listener (fixed in #15;
  the Lightning Chain listener had the same leak, fixed in #16).
- The run timer and the run stats were not reset when a Boss Rush run
  started, because a run was only reset on stage 1. A run now starts on the
  first stage of its mode (fixed with KAN-58).

KAN-22 (item and power-up status indicators): the item team's HUD already
shows the item slots, the active effects and their time left
(`DrawManager.drawItemHud`, keys 1-3), so KAN-22 duplicates it. Proposed:
close it as a duplicate.
