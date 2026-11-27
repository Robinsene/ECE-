# Code quality (Team ECE)

KAN-61. The course grades code quality, so the code Team ECE owns goes
through a static analysis on every pull request, next to the build, the
unit tests and the other teams' regression tests.

## What CI checks

| Job | What it does |
| --- | --- |
| `build` | Compiles the game with `-Xlint:all` |
| `test` | Our JUnit tests in `test/` (49 tests) |
| `course-tests` | The other teams' regression programs in `tests/` (TwoHitEnemy, FinalBossAttack, EffectIntegration) |
| `quality` | PMD 7.8 on the files in `.github/quality/ece-files.txt`, with `.github/quality/ece-ruleset.xml`; the job fails on any violation and uploads the report |
| `smoke` | Runs the game for 10 s and checks the log |

Rules (`ece-ruleset.xml`):

- cyclomatic complexity: at most 10 per method and 80 per class;
- cognitive complexity: at most 15 per method;
- NPath complexity, long parameter lists;
- empty catch blocks, unused private fields and local variables.

## Current figures

PMD 7.8, on main after the game modes (Nov 28):

- 18 classes, 163 methods and constructors;
- average cyclomatic complexity 2.0 per method, highest 9
  (`ModeSelectScreen.handleInput`);
- 0 rule violations.

Most complex classes (total cyclomatic complexity / highest method):

| Class | Total | Highest |
| --- | --- | --- |
| ComboTracker | 33 | 4 |
| HudAnimator | 30 | 6 |
| ModeSelectScreen | 28 | 9 |
| BossHud | 22 | 7 |
| HudSettings | 20 | 5 |

The first run flagged `ModeSelectScreen.update()` (cognitive complexity 18,
cyclomatic 10). It was split into `update()` and `handleInput()`.

## Run it locally

```
curl -sSfL -o pmd.zip https://github.com/pmd/pmd/releases/download/pmd_releases%2F7.8.0/pmd-dist-7.8.0-bin.zip
unzip -q pmd.zip
pmd-bin-7.8.0/bin/pmd check -d "$(paste -sd, .github/quality/ece-files.txt)" \
    -R .github/quality/ece-ruleset.xml -f text
```

Add new ECE classes to `ece-files.txt` so they are checked too.
