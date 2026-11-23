# Gameplay overlays — screen map (KAN-34)

Everything drawn on top of the game during a stage, by every team, with the screen area it uses. The game window is 448 x 520 px, and y grows downward. Team ECE positions live in `engine.HudLayout`.

Each row is listed in the order `GameScreen.draw()` paints it, so a later row covers an earlier one.

| # | Overlay | Owner | Area (x, y) | When |
|---|---------|-------|-------------|------|
| 1 | Ship, enemies, boss, bullets, sparkles, coins, meteor, item drops | Gameplay, VFX, Currency, Item | game area, y ≥ 40 | always |
| 2 | Damage dim (red flash) | VFX (Effection) | full screen | player hit |
| 3 | Points popups (+10, +100) | VFX (Effection), Item System (score boost) | at the enemy, y ≥ 40 | enemy killed |
| 4 | Top bar background and separators | ECE (KAN-26) | 0–448, 0–39 | always |
| 5 | SCORE + value | ECE | right, baseline 25 | always |
| 6 | BEST / NEW BEST! | ECE (KAN-24) | right, baseline 11 | always |
| 7 | LIVES + life icons + lost-life frame | ECE | 20–171, 8–28 | always |
| 8 | Extra-life bar | ECE (KAN-25) | 70–166, 31–34 | always |
| 9 | LEVEL N | ECE | centre, baseline 14 | always |
| 10 | Coin / diamond balances and +N popups | ECE (KAN-10) + Currency | centre and right, y 24–38 | always |
| 11 | Separation line | ECE | y 39 | always |
| 12 | Item slots (left) and active effects (right) | Item System (Team CS) | y 43–59 | always |
| 13 | Item notice | Item System (Team CS) | centre, y ≈ 460 | after an item event |
| 14 | Boss health bar | ECE | y 64+ | stages 5 and 10 |
| 15 | Combo text and time bar | ECE (KAN-23) | right, baseline 72 / 88 on boss stages / 106 under an achievement popup | 2+ quick kills |
| 16 | Low-health glitch | VFX (Effection) | full screen | 1 life left |
| 17 | Wave counter | ECE (KAN-29) | centre or right of the item slots, y 46–57 | normal stages |
| 18 | Pause menu and run summary | ECE (KAN-27) | centre | paused |
| 19 | Countdown, item hint, boss warning | Core, Item System, ECE (KAN-31) | centre band, y 139–380 | level start |
| 20 | Stage clear banner | ECE (KAN-30) | centre band, y 208–312 | stage cleared |
| 21 | Game over sequence | VFX (Effection) | centre | out of lives |
| 22 | Achievement popup | Achievements (Mantis) | 224–442, 46–90 | achievement unlocked, 3 s |

## Overlaps found and fixed

1. **The LIVES label touched the first life icon.** The text ended at x = 65 and the icon started at x = 66. `HudLayout.LIVES_LABEL_WIDTH` is now 50 instead of 46, so the icons, extra-life bar, lost-life frame and separator all move 4 px right together.
2. **The wave counter sat on the item slots when there are 5 of them.** The Extra Slots equipped item gives up to 5 slots, which reach past the centre. `DrawManager.getItemSlotsRight` mirrors the Item System slot geometry (read-only), and the wave counter now starts at least 10 px right of the last slot.
3. **The achievement popup hid the combo.** The popup covers x 224–442, y 46–90, and the combo is right-aligned at baseline 72. While a popup is on screen, the combo moves to baseline 106, just under it.

Before and after screenshots are in `docs/screenshots/hud-overlays.png`.

## Checked, no change needed

- **Boss warning, countdown band and item hint:** the warning sits above the countdown band (y 139–203 vs 216–303), and the item hint is below it (y ≥ 346).
- **Stage clear banner and achievement popup:** the banner is at y 208–312 and the popup at y 46–90, so they never overlap.
- **Pause summary and key hints:** the summary box ends above the key hint line.
- **Combo and item effects:** the effects end at y 59, and the combo text starts at y ≈ 62.
- **Achievement popup and wave counter:** the popup (drawn last) covers the right half of the wave counter for its 3 seconds. This is accepted, because the popup is short and the counter is back as soon as it slides out.
