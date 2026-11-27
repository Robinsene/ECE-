package screen;


import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;
import engine.CoinDropManager;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;
import engine.CurrencyManager;
import engine.GameSettings;
import engine.GameState;
import engine.Achievement;
import engine.DamageDimEffect;
import engine.LightningChain;
import engine.GameEvents;
import engine.GlitchEffect;
import engine.HudAnimator;
import engine.PurchaseManager;
import engine.MeteorManager;
import engine.ScreenShake;
import engine.PointsEffect;
import engine.PlayerSounds;
import engine.soundEngine.ItemSounds;
import entity.Bullet;
import entity.BulletPool;
import entity.Coin;
import entity.CoinPool;
import entity.EnemyShip;
import entity.Boss;
import entity.MidBoss;
import entity.FinalBoss;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Particle;
import entity.Ship;
import item.ItemSystem;
import item.ItemAPI.EventType;
import item.ItemAPI.ItemEvent;

/**
 * Implements the game screen, where the action happens.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameScreen extends Screen {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	// Game over animation timings. AUTHORED BY: VFX TEAM (Effection)
	/** Pause after the player ship explodes, before enemies disappear. */
	private static final int GAME_OVER_PAUSE = 500;
	/** Time between the first enemy rows starting to shrink on game over. */
	private static final int GAME_OVER_ROW_INTERVAL_START = 400;
	/** Fastest time between enemy rows starting to shrink on game over. */
	private static final int GAME_OVER_ROW_INTERVAL_MIN = 250;
	/** Reduction of the row interval after each enemy row starts. */
	private static final int GAME_OVER_ROW_INTERVAL_STEP = 50;
	/** Time an enemy takes to shrink and fade out on game over. */
	private static final int GAME_OVER_SHRINK_DURATION = 450;
	/** Time after the last enemy disappears before the banner appears. */
	private static final int GAME_OVER_LAST_ROW_HOLD = 400;
	/** Text of the game over banner. */
	private static final String GAME_OVER_TEXT = "GAME OVER";
	/** Time between each letter of the game over banner appearing. */
	private static final int GAME_OVER_TYPE_INTERVAL = 100;
	/** Time the game over banner stays on or off while blinking. */
	private static final int GAME_OVER_BLINK_INTERVAL = 200;
	/** Number of times the game over banner blinks. */
	private static final int GAME_OVER_BLINK_COUNT = 3;
	/** Time the game over banner stays steady before fading out. */
	private static final int GAME_OVER_TEXT_HOLD = 600;
	/** Time the fade to black takes before the game over menu. */
	private static final int GAME_OVER_FADE_DURATION = 800;
	/** How long an achievement unlock popup remains visible. */
	private static final int ACHIEVEMENT_POPUP_INTERVAL = 3000;
	/** Time used for the popup to slide in. */
	private static final int ACHIEVEMENT_POPUP_SLIDE_IN = 250;
	/** Time used for the popup to slide out. */
	private static final int ACHIEVEMENT_POPUP_SLIDE_OUT = 350;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40;
	/** Lives at or below this value start the glitch. */
	private static final int LOW_HEALTH_LIVES = 1;
	/** Low-life effect duration (ms). */
	private static final int LOW_HEALTH_EFFECT_DURATION = 5000;
	/** Coins awarded when a regular enemy's drop chance succeeds. */
	private static final int COIN_VALUE = 1;
	/** Coins guaranteed when the special bonus ship is destroyed. */
	private static final int BONUS_COIN_VALUE = 5;
	/** Delay before the extra score boost popup follows the normal one (ms, Team CS). */
	private static final int SCORE_BOOST_POPUP_DELAY = 250;
	/** The extra score boost popup starts this much lower, so the two can be told apart (px, Team CS). */
	private static final int SCORE_BOOST_POPUP_OFFSET_Y = 14;
	/** Minimum time between two pause-key toggles, to debounce the key. */
	private static final int PAUSE_KEY_DEBOUNCE = 200;
	/** Minimum time between two pause-menu cursor moves. */
	private static final int PAUSE_SELECTION_DEBOUNCE = 200;

	/**
	 * Options listed in the pause menu, in the order they are drawn.
	 */
	private enum PauseOption {
		/** Closes the pause menu and continues the level. */
		RESUME("Resume"),
		/** Restarts the current level from the beginning. */
		RESTART("Restart"),
		/** Ends the run and returns to the score / main menu flow. */
		QUIT("Quit to menu");

		/** Text drawn for this option. */
		private final String label;

		PauseOption(final String label) {
			this.label = label;
		}

		/**
		 * @return Text drawn for this option.
		 */
		private String getLabel() {
			return this.label;
		}

		/**
		 * @return Option below this one, wrapping around to the top.
		 */
		private PauseOption next() {
			PauseOption[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}

		/**
		 * @return Option above this one, wrapping around to the bottom.
		 */
		private PauseOption previous() {
			PauseOption[] values = values();
			return values[(this.ordinal() - 1 + values.length) % values.length];
		}
	}

	/** Current game difficulty settings. */
	private GameSettings gameSettings;
	/** Current difficulty level number. */
	private int level;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Player's ship. */
	private Ship ship;
	/** Warp In effect, null if not bought. AUTHORED BY: VFX TEAM (Effection) */
	private engine.WarpIn warpIn;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;

	// Game over animation state. AUTHORED BY: VFX TEAM (Effection)
	/** Time until the next enemy row explodes on game over. */
	private Cooldown gameOverRowCooldown;
	/** Current time between enemy rows starting to shrink on game over. */
	private int gameOverRowInterval;
	/** Enemies shrinking on game over, with the moment each one started. */
	private Map<EnemyShip, Long> shrinkingEnemies;
	/** Moment the last enemy disappeared, 0 while enemies remain. */
	private long gameOverLastEnemyGone;
	/** Moment the game over banner starts appearing. */
	private long gameOverBannerStart;
	/** Checks if the game over sequence is playing. */
	private boolean gameOverActive;
	/** Checks if the game over banner is shown. */
	private boolean showGameOverText;

	/** Time until the achievement unlock popup closes. */
	private Cooldown achievementPopupCooldown;
	/** Achievement currently shown in the unlock popup. */
	private Achievement unlockedAchievement;
	/** Achievements waiting to be shown in the unlock popup. */
	private Queue<Achievement> achievementPopupQueue;
	/** Time when the current achievement popup started. */
	private long achievementPopupStartedAt;
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets;
	/** Set of coins currently dropped and falling on screen. */
	private Set<Coin> coins;
	/** Coins picked up since this level (or its last restart) began. They
	 * are already banked in CurrencyManager, so a restart has to take them
	 * back, or restarting would be a free coin farm. */
	private int coinsCollectedThisLevel;
	/** Decides, with a low configurable chance, whether a kill drops a
	 * coin, so currency income doesn't scale 1:1 with kills. */
	private CoinDropManager coinDropManager;
	/** Current score. */
	private int score;
	/** Player lives left. */
	private int lives;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
    /** Total coins collected by the player. */
    private int coinsCollected;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Sparkles from destroyed ships. AUTHORED BY: VFX TEAM (Effection) */
	private List<Particle> particles = new ArrayList<Particle>();
	/** Colors the sparkles can have. AUTHORED BY: VFX TEAM (Effection) */
	private static final Color[] SPARKLE_COLORS = { Color.WHITE,
			Color.YELLOW, Color.ORANGE, Color.CYAN };
	/** Checks if a bonus life is received. */
	private boolean bonusLife;
	/** Timer for the low-life effect. */
	private Cooldown lowHealthTimer;
	/** Lives in the last frame. */
	private int prevLives;
	/** True while the low-life effect is on. */
	private boolean lowHealthActive;

	/** Whether the player has taken actual damage during this run. */
	private boolean tookDamage;
	/** Dims the screen when the player is hit. */
	private DamageDimEffect damageDim;
	/** Lightning Chain kill effect. AUTHORED BY: VFX TEAM (Effection) */
	private LightningChain lightningChain;
	/** Test switch: java -Dvfx.lightning=true turns it on without buying. */
	private static final boolean LIGHTNING_ON = Boolean.getBoolean("vfx.lightning");
	/** True when Lightning Chain is bought and switched on (or the test switch is on). */
	private boolean lightningOn;
	/** Glitch effect for low health. */
	private GlitchEffect glitch;
	/** HUD animations: score count-up, life blink, level pulse, etc. */
	private HudAnimator hudAnimator;
	/** Time the game was paused, or -1 (Team ECE - KAN-27). */
	private long pausedAt = -1;
	/** Total paused time this stage, in ms (Team ECE - KAN-27). */
	private long pausedTotal;
	/** Animated coin / diamond balances (Team ECE - KAN-10). */
	private engine.CurrencyHud currencyHud;
	/** HUD listeners on the GameEvents bus (Team ECE - KAN-38). */
	private engine.HudEvents hudEvents;
	/** "+N" coin popups flying to the coin balance (Team ECE - KAN-39). */
	private engine.PickupPopups pickupPopups;
	/** Kill combo and score multiplier (Team ECE - KAN-23). */
	private engine.ComboTracker comboTracker;
	/** Score to beat and "NEW BEST!" flash (Team ECE - KAN-24). */
	private engine.BestScoreTracker bestScoreTracker;
	/** Extra life every few points (Team ECE - KAN-25). */
	private engine.ExtraLifeTracker extraLifeTracker;
	/** Screen shake when an enemy is destroyed. */
	private ScreenShake screenShake;
	/** Wave progress, stage clear banner and boss warning (Team ECE - KAN-29/30/31). */
	private engine.StageHud stageHud;
	/** Shop affordability hint (Team ECE - KAN-40). */
	private engine.ShopHint shopHint;
	/** Boss bar phases, damage trail and shield (Team ECE - KAN-20). */
	private engine.BossHud bossHud;
	/** Deep-space stars of the Space Opera mode, null otherwise (KAN-55). */
	private Starfield spaceStars;
	/** True if Comet Trail is bought and switched on. AUTHORED BY: VFX TEAM (Effection) */
	private boolean cometTrailOn;
	/** Bonus meteor and its diamonds. AUTHORED BY: VFX TEAM (Effection) */
	private MeteorManager meteors;
	/** Points shown when enemies are destroyed. AUTHORED BY: VFX TEAM (Effection) */
	private PointsEffect pointsEffect;
	/** Ghost Trail ship effect, null if not bought or switched off. AUTHORED BY: VFX TEAM (Effection) */
	private engine.GhostTrail ghostTrail;
	/** Diamonds earned this run but not yet cashed out; lost on death,
	 * banked into DiamondManager only when the player cashes out. */
	private int pendingDiamonds;
	/** Levels cleared alive during the current run. */
	private int levelsCompletedRun;
	/** Score boost popups waiting to be shown (Team CS): {due time, x, y, points, bonus ship 1/0}. */
	private List<long[]> scoreBoostPopups;
	/** Item system of this run (Team CS). Kept across levels by ItemSystem. */
	private ItemSystem items;
	/** Item slot keys (1-9) held last frame, so holding a key uses it once. */
	private boolean[] itemSlotKeysHeld = new boolean[9];
	/** Skin chosen in Customize; animated skins change colour each frame (GoG). */
	private shop.Skin skin;
	/** Whether gameplay is currently paused. */
	private boolean paused;
	/** Wall-clock time when the current pause began. */
	private long pauseStartedAt;
	/** Debounces the pause key so holding it doesn't toggle every frame. */
	private Cooldown pauseKeyCooldown;
	/** Debounces cursor moves in the pause menu. */
	private Cooldown pauseSelectionCooldown;
	/** Option the cursor is on in the pause menu. */
	private PauseOption pauseSelection;
	/** Score the level started with, restored by "Restart". */
	private final int initialScore;
	/** Lives the level started with (bonus life included), restored by
	 * "Restart". */
	private final int initialLives;
	/** Bullets-shot count the level started with, restored by "Restart". */
	private final int initialBulletsShot;
	/** Ships-destroyed count the level started with, restored by
	 * "Restart". */
	private final int initialShipsDestroyed;
	/** Pending diamonds the level started with, restored by "Restart". */
	private final int initialPendingDiamonds;

	/** Boss encounter; normal stages use enemyShipFormation instead. */
	private Boss midBoss;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
					  final GameSettings gameSettings, final boolean bonusLife,
					  final int width, final int height, final int fps) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.lives = gameState.getLivesRemaining();
		if (this.bonusLife)
			this.lives++;
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
		this.pendingDiamonds = gameState.getPendingDiamonds();
		this.levelsCompletedRun = gameState.getLevelsCompletedRun();
        this.coinsCollected = gameState.getCoinsCollected();
		this.tookDamage = false;



		// Snapshot of how the level started, so "Restart" has something to
		// come back to.
		this.initialScore = this.score;
		this.initialLives = this.lives;
		this.initialBulletsShot = this.bulletsShot;
		this.initialShipsDestroyed = this.shipsDestroyed;
		this.initialPendingDiamonds = this.pendingDiamonds;
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();
		// Space Opera theme: music, lasers, boss names, deep space
		// (Team ECE - KAN-55). Reset to the normal look otherwise.
		boolean opera = engine.SpaceOpera.isActive();
		SoundManager.getInstance().playBgm(
				opera ? Sound.BGM_SPACE_OPERA : Sound.BGM_GAME);
		drawManager.setLaserColors(opera ? engine.SpaceOpera.PLAYER_LASER : null,
				opera ? engine.SpaceOpera.ENEMY_LASER : null);
		drawManager.setBossName(opera ? engine.SpaceOpera.bossName(this.level)
				: null);
		this.spaceStars = opera ? new Starfield(this.width, this.height) : null;

		// Initialize the player in both boss and normal stages.
		this.ship = new Ship(this.width / 2, this.height - 30,
				Core.getSelectedShipType());

		// Distinct mid-boss and final boss encounters.
		if (engine.StageHud.isBossLevel(this.level)) {
			this.midBoss = engine.GameMode.baseLevel(this.level) == 5
					? new MidBoss(this.width / 2 - 16, 60, DrawManager.SpriteType.MidBoss1)
					: new FinalBoss(this.width / 2 - 24, 60, DrawManager.SpriteType.FinalBoss1);
		} else {
			this.enemyShipFormation = new EnemyShipFormation(this.gameSettings);
			this.enemyShipFormation.attach(this);
		}
		// Skin chosen in Customize (GoG - Currency System).
		this.skin = PurchaseManager.getInstance().getSelectedSkin();
		this.ship.setSkinColor(this.skin.getColor());
		// Ship look chosen in Customize (A1): only the sprite changes.
		shop.ShapeSkin shape = PurchaseManager.getInstance()
				.getSelectedShape(this.ship.getType());
		this.ship.setShapeSprite(shape == null ? null : shape.getSprite());
		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.lowHealthTimer = Core.getCooldown(LOW_HEALTH_EFFECT_DURATION);
		this.prevLives = this.lives;
		this.lowHealthActive = false;
		this.bullets = new HashSet<Bullet>();
		this.cometTrailOn = PurchaseManager.getInstance().isEffectOn("comet_trail"); // AUTHORED BY: VFX TEAM (Effection)
		// A restart calls initialize() again: drop the old listeners first so
		// they don't pile up on PLAYER_HIT (Team ECE - KAN-48).
		if (this.damageDim != null)
			GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, this.damageDim);
		if (this.lightningChain != null)
			GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, this.lightningChain);
		this.damageDim = new DamageDimEffect(900, 0.75f,
				new java.awt.Color(150, 0, 0));  //new update dim effect
		GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, this.damageDim);
		this.lightningChain = new LightningChain(); // AUTHORED BY: VFX TEAM (Effection)
		this.lightningOn = LIGHTNING_ON || PurchaseManager.getInstance().isEffectOn("lightning_chain");   // Lightning Chain, on unless switched off in Customize.
		GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, this.lightningChain);
		this.glitch = new GlitchEffect();
		this.hudAnimator = new HudAnimator();
		this.currencyHud = new engine.CurrencyHud();
		this.comboTracker = new engine.ComboTracker();
		// HUD reacts to game events; a restart replaces the old listener
		// (Team ECE - KAN-38).
		if (this.hudEvents != null)
			this.hudEvents.detach();
		this.hudEvents = new engine.HudEvents(this.comboTracker);
		this.hudEvents.attach();
		this.pickupPopups = new engine.PickupPopups(); // KAN-39
		this.bestScoreTracker = engine.BestScoreTracker.fromHighScores(
				this.score, this.logger);
		this.extraLifeTracker = new engine.ExtraLifeTracker(this.score);
		this.screenShake = new ScreenShake();
		this.stageHud = new engine.StageHud();
		this.shopHint = new engine.ShopHint();
		this.bossHud = new engine.BossHud();
		this.stageHud.startWave(countAliveEnemies());
		this.meteors = new MeteorManager(this.level, this.width, this.height,SEPARATION_LINE_HEIGHT);
		this.pointsEffect = new PointsEffect(SEPARATION_LINE_HEIGHT);
		this.scoreBoostPopups = new ArrayList<long[]>(); // Item System (Team CS)
		// Warp In effect, null if not bought or switched off. AUTHORED BY: VFX TEAM (Effection) */
		if (PurchaseManager.getInstance().isEffectOn("warp_in")) {
			this.warpIn = new engine.WarpIn();
			this.warpIn.startLevel(INPUT_DELAY);
		}
		if (PurchaseManager.getInstance().isEffectOn("ghost_trail"))		// Ghost Trail, on unless switched off in Customize. AUTHORED BY: VFX TEAM (Effection)
			this.ghostTrail = new engine.GhostTrail();
		this.coins = new HashSet<Coin>();
		this.coinsCollectedThisLevel = 0;
		this.achievementPopupQueue = new LinkedList<Achievement>();
		this.coinDropManager = new CoinDropManager();
		this.pauseKeyCooldown = Core.getCooldown(PAUSE_KEY_DEBOUNCE);
		this.pauseSelectionCooldown = Core.getCooldown(PAUSE_SELECTION_DEBOUNCE);
		this.pauseSelection = PauseOption.RESUME;
		this.pauseStartedAt = 0;

		// Item System (Team CS): level 1 starts a new run, later levels keep it.
		this.items = ItemSystem.forLevel(this.level);
		this.items.beginLevel(this.level, this.width, this.ship,
				new ItemSystem.GameHooks() {
					public int getLives() { return lives; }
					public void addLife() { lives++; }
					public void addScore(final int points) { score += points; }
				});

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.pausedAt = -1; // stage time restarts with the level (KAN-27)
		this.pausedTotal = 0;
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
		// Stage 1 starts a new run timer (Team ECE - KAN-37, KAN-38).
		engine.RunStats.hold();
		GameEvents.emit(GameEvents.Type.LEVEL_START, this.level);
	}

	/**
	 * Starts the 5 s low-life effect when lives become 1. Stops on timeout
	 * or life gain.
	 * AUTHORED BY: VFX TEAM (Effection)
	 */
	private void updateLowHealthEffect() {
		boolean lowHealth = this.lives > 0 && this.lives <= LOW_HEALTH_LIVES;
		if (lowHealth && this.lives != this.prevLives)
			this.lowHealthTimer.reset();
		this.prevLives = this.lives;
		this.lowHealthActive = lowHealth && !this.lowHealthTimer.checkFinished();
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	public final int run() {
		try {
			super.run();
		} finally {
			GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, this.damageDim);
			GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, this.lightningChain);
			if (this.hudEvents != null)
				this.hudEvents.detach(); // Team ECE - KAN-38
			engine.BossMusic.apply(null); // no boss music outside the stage
			// Other screens and modes draw with the normal colours (KAN-55).
			drawManager.setLaserColors(null, null);
			drawManager.setBossName(null);
		}

		this.items.endLevel(); // Item System (Team CS)


		this.score += LIFE_SCORE * (this.lives - 1);
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();
		// Item System (Team CS): item bonuses on top of the base fire rate,
		// bullet speed and movement speed (stacks and the boost item).
		this.ship.setItemBonuses(
				this.items.fireRateBonus(this.ship.getBaseFireRate()),
				this.items.bulletSpeedBonus(),
				this.items.movementSpeedBonus(this.ship.getBaseSpeed()));
		// Animated skins (GoG); plain skins return the same colour.
		this.ship.setSkinColor(this.skin.colorAt(System.currentTimeMillis()));
		updateBossMusic();

		if (inputManager.isKeyDown(KeyEvent.VK_P)
				&& this.pauseKeyCooldown.checkFinished()) {
			if (this.paused)
				resumeGameplay();
			else {
				this.paused = true;
				this.pauseStartedAt = System.currentTimeMillis();
				trackPauseTime(); // Team ECE - KAN-27
			}
			this.pauseKeyCooldown.reset();
			// Ghost Trail starts fresh after a pause. AUTHORED BY: VFX TEAM (Effection)
			if (this.ghostTrail != null)
				this.ghostTrail.clear();
			this.pauseSelection = PauseOption.RESUME;
			this.logger.info(this.paused ? "Game paused" : "Game resumed");
		}

		if (this.paused) {
			this.comboTracker.holdDuringPause(System.currentTimeMillis()); // KAN-23
			engine.RunStats.hold(); // the run timer stops too (KAN-37)
			updatePauseMenu();
			draw();
			return;
		}

		if (this.inputDelay.checkFinished() && !this.levelFinished) {
			engine.RunStats.tick(System.currentTimeMillis()); // KAN-37

			if (!this.ship.isDestroyed()) {
				boolean moveRight = inputManager.isKeyDown(KeyEvent.VK_RIGHT)
						|| inputManager.isKeyDown(KeyEvent.VK_D);
				boolean moveLeft = inputManager.isKeyDown(KeyEvent.VK_LEFT)
						|| inputManager.isKeyDown(KeyEvent.VK_A);

				boolean isRightBorder = this.ship.getPositionX()
						+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;
				boolean isLeftBorder = this.ship.getPositionX()
						- this.ship.getSpeed() < 1;

				if (moveRight && !isRightBorder) {
					this.ship.moveRight();
				}
				if (moveLeft && !isLeftBorder) {
					this.ship.moveLeft();
				}
				if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
					// Count bullets, not trigger pulls: Two Way fires two at
					// once, and accuracy is kills per bullet.
					int bulletsBefore = this.bullets.size();
					if (this.ship.shoot(this.bullets)) {
						this.bulletsShot += this.bullets.size() - bulletsBefore;
						// Space Opera: blaster sound instead (KAN-57).
						SoundManager.getInstance().playSfx(this.spaceStars != null
								? Sound.LASER : Sound.SHOOT);
					}
				}
				useItemSlots(); // Item System (Team CS)
			}

			if (this.enemyShipSpecial != null) {
				if (!this.enemyShipSpecial.isDestroyed())
					this.enemyShipSpecial.move(this.items.enemiesFrozen() ? 0 : 2, 0);
				else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
					this.enemyShipSpecial = null;

			}
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkFinished()) {
				this.enemyShipSpecial = new EnemyShip();
				this.enemyShipSpecialCooldown.reset();
				this.logger.info("A special ship appears");
			}
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.width) {
				this.enemyShipSpecial = null;
				this.logger.info("The special ship has escaped");
			}

			this.ship.update();
			if (this.midBoss != null) {
				if (!this.items.enemiesFrozen()) {
					this.midBoss.update();
					this.midBoss.moveBoss(this.width, this.height, this.gameSettings);
				}
				this.midBoss.setTarget(
						this.ship.getPositionX() + this.ship.getWidth() / 2,
						this.ship.getPositionY() + this.ship.getHeight() / 2);
				this.midBoss.shoot(this.bullets, this.gameSettings);
			} else {
				if (!this.items.enemiesFrozen())
					this.enemyShipFormation.update();
				this.enemyShipFormation.shoot(this.bullets);
				this.meteors.trySpawn(this.enemyShipFormation, this.ship); // VFX meteor needs a normal formation.
			}
			updateLowHealthEffect();
			this.ship.setBlinking(this.lowHealthActive);
		}

		// Item System (Team CS): drops fall/expire, pickups, effect timers.
		this.items.update(this.ship, this.inputDelay.checkFinished()
				&& !this.levelFinished && this.lives > 0
				&& !this.ship.isDestroyed());
		manageCollisions();
		updateScoreHud(); // Team ECE - KAN-23/24/25
		showScoreBoostPopups(); // Item System (Team CS)
		cleanBullets();
		updateParticles(); // AUTHORED BY: VFX TEAM (Effection)
		this.lightningChain.update();
		updateCoins();
		int diamondsPicked = this.meteors.update(this.ship, this.lives > 0);
		if (diamondsPicked > 0)
			SoundManager.getInstance().playSfx(Sound.DIAMOND_PICKUP);
		this.pendingDiamonds += diamondsPicked; // VFX TEAM (Effection): updates the meteor and its diamonds, returns the diamonds to be added to pending
		handleItemEvents();
		updateAchievementPopup();
		draw();

		// Game over sequence, only when the player runs out of lives. AUTHORED BY: VFX TEAM (Effection)
		if (this.lives == 0 && !this.levelFinished)
			startGameOverSequence();
		if (this.gameOverActive) {
			updateGameOverSequence();
			return;
		}

		boolean isCleared = this.midBoss != null
				? this.midBoss.isDestroyed() : this.enemyShipFormation.isEmpty();
		if ((isCleared || this.lives == 0)
				&& !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();

			// Level cleared alive: level N is worth N diamonds, kept pending
			// until cashed out (see engine.DiamondManager), and coins still
			// falling are collected so the last kills' drops aren't lost.
			if (isCleared && this.lives > 0) {
				// "STAGE X CLEAR" banner (Team ECE - KAN-30).
				this.stageHud.stageCleared(System.currentTimeMillis(),
						this.score - this.initialScore);
				this.levelsCompletedRun++; // Sharpshooter tiers (Mantis)
				SoundManager.getInstance().stopBgm();
				SoundManager.getInstance().playSfx(Sound.VICTORY);
				this.pendingDiamonds += this.level;
				collectRemainingCoins();
				this.pendingDiamonds += this.meteors.collectRemaining(); // VFX TEAM (Effection): collects remaining diamonds from the meteor and adds them to pending
				showUnlockedAchievements(Core.getAchievementManager()
						.recordFlawlessLevelCompleted(this.tookDamage));
				showUnlockedAchievement(Core.getAchievementManager()
						.recordLevelCompleted(this.level));
                showUnlockedAchievement(Core.getAchievementManager().recordRunCoins(this.coinsCollected));

				showUnlockedAchievement(Core.getAchievementManager()
						.recordLevelCompleted(
								this.level,
								Core.getShipId(this.ship.getType()),
								true));

				// Last level cleared alive: the game is beaten.
				// Boss Rush and Survival skip stages: not a full run.
				if (this.level == Core.getNumLevels()
						&& engine.GameMode.current().isFullRun())
					showUnlockedAchievement(Core.getAchievementManager()
							.recordGameWon(Core.getSelectedShipId()));
			}
		}

		if (this.levelFinished && this.screenFinishedCooldown.checkFinished()
				&& this.unlockedAchievement == null
				&& this.achievementPopupQueue.isEmpty())
			this.isRunning = false;

	}

	/**
	 * Ship line of the pause summary, read from the Player Ship Variety
	 * stats (Team ECE - KAN-33): name, bullets per shot and shots per
	 * second, e.g. "Two-Way Shot 2x 1.0/s".
	 *
	 * @return Ship summary.
	 */
	private String shipSummary() {
		Ship.ShipType type = this.ship.getType();
		double perSecond = 1000.0 / type.getShootingInterval();
		return shop.ShopCatalog.shipName(type) + " "
				+ type.getBulletsPerShot() + "x "
				+ String.format(java.util.Locale.ROOT, "%.1f/s", perSecond);
	}

	/**
	 * Baseline of the combo text: under the item HUD, under the boss bar on
	 * boss stages, or under the achievement popup while one is shown, so
	 * the popup never hides the combo (Team ECE - KAN-23, KAN-34).
	 *
	 * @return Baseline Y of the combo text.
	 */
	private int comboBaseline() {
		if (this.unlockedAchievement != null)
			return engine.HudLayout.COMBO_BASELINE_UNDER_POPUP;
		return this.midBoss != null ? engine.HudLayout.COMBO_BASELINE_BOSS
				: engine.HudLayout.COMBO_BASELINE;
	}

	/**
	 * Counts the enemies of the formation still alive (Team ECE - KAN-29).
	 *
	 * @return Enemies alive, 0 on boss stages.
	 */
	private int countAliveEnemies() {
		if (this.enemyShipFormation == null)
			return 0;
		int alive = 0;
		for (EnemyShip enemyShip : this.enemyShipFormation)
			if (!enemyShip.isDestroyed())
				alive++;
		return alive;
	}

	/**
	 * Starts the game over sequence: the player ship explodes, then after a
	 * pause the remaining enemies shrink away row by row.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 */
	private void startGameOverSequence() {
		SoundManager.getInstance().stopBgm();
		SoundManager.getInstance().playSfx(Sound.GAME_OVER);
		this.levelFinished = true;
		this.gameOverActive = true;

		// Shows the player ship explosion; it stays since the ship is no
		// longer updated.
		this.ship.update();

		BulletPool.recycle(this.bullets);
		this.bullets.clear();
		this.meteors.clear(); // VFX TEAM (Effection):

		// Clears explosions left from enemies shot just before.
		if (this.enemyShipFormation != null)
			this.enemyShipFormation.removeDestroyed();
		this.shrinkingEnemies = new LinkedHashMap<EnemyShip, Long>();
		if (this.midBoss != null)
			this.midBoss.destroy();

		this.gameOverRowInterval = GAME_OVER_ROW_INTERVAL_START;
		this.gameOverRowCooldown = Core.getCooldown(GAME_OVER_PAUSE);
		this.gameOverRowCooldown.reset();
		this.logger.info("Game over, starting disappear sequence.");
	}

	/**
	 * Makes the next enemy row start shrinking, speeding up after each row,
	 * shows the game over banner once no enemies are left, then ends the
	 * screen.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 */
	private void updateGameOverSequence() {
		long now = System.currentTimeMillis();
		if (this.showGameOverText) {
			if (now - this.gameOverBannerStart
					>= getGameOverFadeStart() + GAME_OVER_FADE_DURATION)
				this.isRunning = false;
			return;
		}

		// Clears a bonus ship explosion (shot just before game over) after
		// its usual time, as gameplay does, since that update no longer runs.
		if (this.enemyShipSpecial != null
				&& this.enemyShipSpecial.isDestroyed()
				&& this.enemyShipSpecialExplosionCooldown.checkFinished())
			this.enemyShipSpecial = null;

		// Removes enemies that finished shrinking.
		Iterator<Long> starts = this.shrinkingEnemies.values().iterator();
		while (starts.hasNext())
			if (now - starts.next() >= GAME_OVER_SHRINK_DURATION)
				starts.remove();

		if (!this.gameOverRowCooldown.checkFinished())
			return;

		List<EnemyShip> row = this.enemyShipFormation == null
				? java.util.Collections.<EnemyShip>emptyList()
				: this.enemyShipFormation.removeBottomRow();
		if (!row.isEmpty()) {
			for (EnemyShip enemyShip : row)
				this.shrinkingEnemies.put(enemyShip, now);
			this.gameOverRowCooldown = Core.getCooldown(
					this.gameOverRowInterval);
			this.gameOverRowCooldown.reset();
			this.gameOverRowInterval = Math.max(GAME_OVER_ROW_INTERVAL_MIN,
					this.gameOverRowInterval - GAME_OVER_ROW_INTERVAL_STEP);
		} else if (this.enemyShipSpecial != null
				&& !this.enemyShipSpecial.isDestroyed()) {
			// The bonus ship sits above the formation, so it goes last.
			this.shrinkingEnemies.put(this.enemyShipSpecial, now);
			this.enemyShipSpecial = null;
		} else if (this.shrinkingEnemies.isEmpty()) {
			// Short beat on the empty screen before the banner.
			if (this.gameOverLastEnemyGone == 0)
				this.gameOverLastEnemyGone = now;
			if (now - this.gameOverLastEnemyGone >= GAME_OVER_LAST_ROW_HOLD) {
				this.enemyShipSpecial = null;
				this.showGameOverText = true;
				this.gameOverBannerStart = now;
			}
		}
	}

	/**
	 * Draws the enemies shrinking and fading away on game over.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 */
	private void drawShrinkingEnemies() {
		long now = System.currentTimeMillis();
		for (Map.Entry<EnemyShip, Long> entry
				: this.shrinkingEnemies.entrySet()) {
			double progress = (double) (now - entry.getValue())
					/ GAME_OVER_SHRINK_DURATION;
			drawManager.drawEntityShrunk(entry.getKey(), 1 - progress);
		}
	}

	/**
	 * Gets the time, from the banner start, when the fade to black begins.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 *
	 * @return Milliseconds after the banner starts.
	 */
	private int getGameOverFadeStart() {
		return GAME_OVER_TEXT.length() * GAME_OVER_TYPE_INTERVAL
				+ GAME_OVER_BLINK_COUNT * GAME_OVER_BLINK_INTERVAL * 2
				+ GAME_OVER_TEXT_HOLD;
	}

	/**
	 * Draws the game over banner: typed out letter by letter, then blinking,
	 * then the screen fades to black.
	 * AUTHORED BY: VFX TEAM (Effection)
	 * Any further inquiries please contact us.
	 */
	private void drawGameOverSequence() {
		long elapsed = System.currentTimeMillis() - this.gameOverBannerStart;
		int typeTime = GAME_OVER_TEXT.length() * GAME_OVER_TYPE_INTERVAL;
		int blinkTime = GAME_OVER_BLINK_COUNT * GAME_OVER_BLINK_INTERVAL * 2;

		if (elapsed < typeTime) {
			drawManager.drawGameOverBanner(this, GAME_OVER_TEXT,
					(int) (elapsed / GAME_OVER_TYPE_INTERVAL) + 1);
		} else {
			long blinkElapsed = elapsed - typeTime;
			boolean visible = blinkElapsed >= blinkTime
					|| (blinkElapsed / GAME_OVER_BLINK_INTERVAL) % 2 == 1;
			if (visible)
				drawManager.drawGameOverBanner(this, GAME_OVER_TEXT,
						GAME_OVER_TEXT.length());
		}

		long fadeElapsed = elapsed - getGameOverFadeStart();
		if (fadeElapsed > 0)
			drawManager.drawFadeOverlay(this, (int) (fadeElapsed * 255
					/ GAME_OVER_FADE_DURATION));
	}

	/**
	 * Handles cursor movement and selection while the pause menu is open.
	 */
	private void updatePauseMenu() {
		if (!this.pauseSelectionCooldown.checkFinished())
			return;

		if (inputManager.isKeyDown(KeyEvent.VK_UP)
				|| inputManager.isKeyDown(KeyEvent.VK_W)) {
			this.pauseSelection = this.pauseSelection.previous();
			this.pauseSelectionCooldown.reset();
		} else if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
				|| inputManager.isKeyDown(KeyEvent.VK_S)) {
			this.pauseSelection = this.pauseSelection.next();
			this.pauseSelectionCooldown.reset();
		} else if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
			this.pauseSelectionCooldown.reset();
			confirmPauseSelection();
		}
	}

	/**
	 * Carries out whichever pause menu option the cursor was on.
	 */
	private void confirmPauseSelection() {
		engine.soundEngine.MenuSounds.playSelect();
		switch (this.pauseSelection) {
			case RESUME:
				resumeGameplay();
				break;
			case RESTART:
				restartLevel();
				break;
			case QUIT:
				// No dedicated "abandon" path exists in the screen flow,
				// but ending the run the same way running out of lives
				// does takes us straight to the existing score screen,
				// which already offers "back to menu" / "play again".
				this.lives = 0;
				this.paused = false;
				break;
			default:
				break;
		}
	}

	/** Resumes the level without consuming the boss's warning time. */
	private void resumeGameplay() {
		if (this.paused && this.midBoss instanceof FinalBoss) {
			((FinalBoss) this.midBoss).resumeAfterPause(
					System.currentTimeMillis() - this.pauseStartedAt);
		}
		this.paused = false;
		this.pauseStartedAt = 0;
		trackPauseTime(); // stage time stops counting the pause (KAN-27)
	}

	/**
	 * Adds the points of a destroyed enemy, multiplied by the kill combo
	 * (Team ECE - KAN-23). The points popup keeps showing the base value;
	 * the combo text shows the multiplier.
	 *
	 * @param points
	 *            Points of the kill, item score boost included.
	 */
	private void addKillScore(final int points) {
		int multiplier = this.comboTracker
				.registerKill(System.currentTimeMillis());
		this.score += points * multiplier;
		GameEvents.emit(GameEvents.Type.ENEMY_KILLED, points); // KAN-38
	}

	/**
	 * Per-frame update of the score HUD logic (Team ECE): combo timeout
	 * (KAN-23), record check (KAN-24) and extra lives (KAN-25).
	 */
	private void updateScoreHud() {
		long now = System.currentTimeMillis();
		this.comboTracker.update(now);
		engine.RunStats.recordCombo(this.comboTracker.getBestCombo()); // KAN-58
		this.bestScoreTracker.update(now, this.score);
		if (this.lives > 0 && !this.levelFinished) {
			int extraLives = this.extraLifeTracker.update(now, this.score,
					this.lives, this.items.getMaxLives());
			if (extraLives > 0)
				SoundManager.getInstance().playSfx(Sound.LEVEL_UP);
			this.lives += extraLives;
		}
	}

	/**
	 * Adds up the time spent paused, so the stage time shown in the pause
	 * menu only counts play (Team ECE - KAN-27). Called on each pause
	 * toggle.
	 */
	private void trackPauseTime() {
		long now = System.currentTimeMillis();
		if (this.paused) {
			this.pausedAt = now;
		} else if (this.pausedAt >= 0) {
			this.pausedTotal += now - this.pausedAt;
			this.pausedAt = -1;
		}
	}

	/**
	 * @return Play time of this stage as m:ss, from the end of the
	 *         countdown and without pauses (Team ECE - KAN-27).
	 */
	private String formatStageTime() {
		long end = this.pausedAt >= 0 ? this.pausedAt
				: System.currentTimeMillis();
		long played = Math.max(0, end - this.gameStartTime - INPUT_DELAY
				- this.pausedTotal);
		long seconds = played / 1000;
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}

	/**
	 * Restarts the current level: score, lives, bullets shot, ships
	 * destroyed and pending diamonds go back to what they were when the
	 * level began, and every on-screen element is rebuilt from scratch.
	 */
	private void restartLevel() {
		this.score = this.initialScore;
		this.lives = this.initialLives;
		this.bulletsShot = this.initialBulletsShot;
		this.shipsDestroyed = this.initialShipsDestroyed;
		this.pendingDiamonds = this.initialPendingDiamonds;
		refundCoinsCollectedThisLevel();
		this.tookDamage = false; // a restarted level can still be flawless
		this.levelFinished = false;
		initialize();
		this.paused = false;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		// Space Opera: deep space behind everything, hyperspace jump at the
		// start of the stage (Team ECE - KAN-55).
		if (this.spaceStars != null) {
			float streak = engine.SpaceOpera.streakLength(engine.SpaceOpera
					.jumpProgress(System.currentTimeMillis()
							- this.gameStartTime));
			if (!this.paused)
				for (int i = 0; i < 1 + Math.round(streak * 12); i++)
					this.spaceStars.update();
			// Planet and nebula once out of hyperspace (KAN-57).
			if (streak <= 0f)
				drawManager.drawSpaceScenery(this, System.currentTimeMillis());
			drawManager.drawSpaceBackground(this, this.spaceStars, streak);
		}

		if (!this.paused) {
			// Screen shake: move game world only. AUTHORED BY: VFX TEAM (Effection)
			this.screenShake.update();
			drawManager.setWorldOffset(this.screenShake.getOffsetX(),
					this.screenShake.getOffsetY());

			// Ship, enemies, bullets and falling coins are hidden while
			// paused, so the pause menu isn't cluttered by the frozen
			// battlefield behind it - only the top HUD stays visible.
			// Warp In draws the ship itself while it beams in. AUTHORED BY: VFX TEAM (Effection)
			boolean warping = this.warpIn != null
					&& this.warpIn.update(this.ship, this.lives > 0);
			// Ghost Trail behind the ship, gone once the level ends (game over or level clear).
			// AUTHORED BY: VFX TEAM (Effection)
			if (this.ghostTrail != null && (this.levelFinished || warping))
				this.ghostTrail.clear();
			else if (this.ghostTrail != null)
				this.ghostTrail.draw(drawManager, this.ship,
						this.skin.colorAt(System.currentTimeMillis()),
						engine.GhostTrail.isBoosting(this.items));
			if (!warping)
				drawManager.drawEntity(this.ship, this.ship.getPositionX(),
						this.ship.getPositionY());
			drawManager.drawWarpIn(this.warpIn, this.ship,
					this.skin.colorAt(System.currentTimeMillis()),
					SEPARATION_LINE_HEIGHT);
			if (this.warpIn != null && this.warpIn.takeImpact())
				this.screenShake.trigger();
			drawManager.drawItemShield(this.ship, this.items); // Item System (Team CS)
			if (this.enemyShipSpecial != null)
				drawManager.drawEntity(this.enemyShipSpecial,
						this.enemyShipSpecial.getPositionX(),
						this.enemyShipSpecial.getPositionY());

			if (this.midBoss != null) {
				if (this.midBoss.isVisible())
					drawManager.drawEntity((Entity) this.midBoss,
							this.midBoss.getPositionX(), this.midBoss.getPositionY());
				this.midBoss.drawWarnings(drawManager);
			} else {
				this.enemyShipFormation.draw();
			}

			// Hit sparkles move with the game world.
			drawManager.drawParticles(this, this.particles,
					SEPARATION_LINE_HEIGHT);
			drawManager.drawLightningChain(this.lightningChain);

			for (Bullet bullet : this.bullets) {
				// Comet trail, player bullets only. AUTHORED BY: VFX TEAM (Effection)
				if (this.cometTrailOn && bullet.getSpeed() < 0)
					drawManager.drawBulletTrail(bullet);
				drawManager.drawEntity(bullet, bullet.getPositionX(),
						bullet.getPositionY());
			}
			// Stop shake offset before full-screen dim.
			drawManager.setWorldOffset(0, 0);
			// Damage dim (under HUD, so score/lives stay bright). AUTHORED BY: VFX TEAM (Effection)
			drawManager.drawDamageDim(this, this.damageDim);

			// Coins are part of the game world, so they shake too.
			drawManager.setWorldOffset(this.screenShake.getOffsetX(),
					this.screenShake.getOffsetY());
			for (Coin coin : this.coins)
				drawManager.drawCoin(coin, coin.getPositionX(),
						coin.getPositionY());
			this.meteors.draw(drawManager); // VFX TEAM (Effection): draws the meteor and its diamonds
			drawManager.drawItemDrops(this.items); // Item System (Team CS)
			// HUD must stay still: remove shake offset.
			drawManager.setWorldOffset(0, 0);
		}

		// Points popups (above the dim, under the HUD). AUTHORED BY: VFX TEAM (Effection)
		drawManager.drawPointsEffect(this, this.pointsEffect);

		// Interface, with HUD animations (Requirement 8 - Gameplay HUD).
		long now = System.currentTimeMillis();
		int coins = CurrencyManager.getInstance().getCoins();
		int diamonds = engine.DiamondManager.getInstance().getDiamonds();
		this.hudAnimator.update(now, this.score, this.lives, coins, diamonds);
		if (this.inputDelay.checkFinished())
			this.hudAnimator.startLevel(now);

		// Player's HUD opacity and size, and the top bar background
		// (Team ECE - KAN-26, KAN-28).
		engine.HudSettings hudSettings = engine.HudSettings.getInstance();
		// HUD animations can be turned off in the settings (KAN-41).
		boolean animated = hudSettings.isAnimated();
		drawManager.beginHud(hudSettings.getOpacity());
		drawManager.drawHudPanel(this, SEPARATION_LINE_HEIGHT - 1);
		drawManager.beginHudScale(this, hudSettings.getSize().getScale());
		drawManager.drawScore(this, animated
				? this.hudAnimator.getDisplayedScore() : this.score,
				animated && this.hudAnimator.isScoreFlashing(now)
						? Color.YELLOW : Color.GREEN);
		// Score to beat, above the score (Team ECE - KAN-24).
		drawManager.drawBestScore(this,
				this.bestScoreTracker.getShownBest(this.score),
				this.bestScoreTracker.isBeaten(),
				animated && this.bestScoreTracker.isNewBestShowing(now),
				this.bestScoreTracker.isNewBestFlashOn(now));
		drawManager.drawLives(this, this.lives,
				animated && this.hudAnimator.isLowLifePulseOn(now)
						? Color.RED : Color.WHITE,
				animated ? this.hudAnimator.getGainedSlotToHide(now) : -1,
				animated ? this.hudAnimator.getLostSlotToDraw(now) : -1);
		// Progress toward the next extra life (Team ECE - KAN-25).
		drawManager.drawExtraLifeBar(this,
				this.extraLifeTracker.getProgress(this.score),
				animated && this.extraLifeTracker.isFullFlashOn(now));
		drawManager.drawLevel(this, this.level,
				animated && this.hudAnimator.isLevelPulseOn(now)
						? Color.YELLOW : Color.GREEN);
		// Coin and diamond balances: rolling counter, +N/-N popup and icon
		// pulse (Team ECE - KAN-10). Sits on its own row at y=36, below
		// the level indicator.
		this.currencyHud.setAnimated(animated);
		this.currencyHud.update(coins, diamonds, now);
		drawManager.drawCurrencyHud(this, this.currencyHud);
		// Empty slots up to the run's max lives (Team ECE - KAN-33).
		drawManager.drawEmptyLifeSlots(this, this.lives,
				this.items.getMaxLives());
		// "$" next to the coins when the shop has something affordable
		// (Team ECE - KAN-40).
		if (this.shopHint.canAffordSomething(coins))
			drawManager.drawShopHint(this,
					engine.ShopHint.isPulseOn(now));
		drawManager.endHudScale();
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
		drawManager.drawItemHud(this, this.items); // Item System (Team CS)
		// Run timer and kill counter, bottom left (Team ECE - KAN-37), and
		// "+N" coin popups on their way to the balance (KAN-39).
		drawManager.drawRunStats(this, engine.RunStats.getPlayMillis(),
				this.shipsDestroyed,
				animated && this.hudEvents.isKillFlashOn(now));
		drawManager.drawPickupPopups(this.pickupPopups, now);
		// Boss health bar: fixed overlay a row below the item HUD slots
		// (y 43-59, Team CS), so the two never collide.
		// Boss bar split into phases, with damage trail (KAN-20).
		this.bossHud.update(this.midBoss, now);
		drawManager.drawBossHud(this, this.midBoss,
				engine.HudLayout.BOSS_BAR_Y, this.bossHud, now);
		// Kill combo, right under the item HUD, or under the boss bar on
		// boss stages (Team ECE - KAN-23).
		if (!this.paused)
			drawManager.drawCombo(this,
					this.comboTracker.getShownCombo(now),
					this.comboTracker.getShownMultiplier(now),
					animated ? this.comboTracker.getAlpha(now)
							: (this.comboTracker.getCombo() > 0 ? 1f : 0f),
					animated ? this.comboTracker.getScale(now) : 1f,
					this.comboTracker.getTimeLeft(now),
					comboBaseline());
		drawManager.endHud();
		// Low-health glitch (covers game + HUD). AUTHORED BY: VFX TEAM (Effection)
		this.glitch.setEnabled(this.lowHealthActive && !this.levelFinished);
		drawManager.drawGlitch(this, this.glitch);
		// Wave progress in the middle of the item row, normal stages only
		// (the boss health bar covers boss stages) (Team ECE - KAN-29).
		if (this.enemyShipFormation != null && !this.paused) {
			this.stageHud.updateWave(countAliveEnemies());
			drawManager.drawWaveProgress(this,
					this.stageHud.getWaveRemaining(),
					this.stageHud.getWaveTotal(),
					this.stageHud.getWaveProgress(),
					drawManager.getItemSlotsRight(this.items)); // KAN-34
		}

		if (this.paused) {
			String[] options = { PauseOption.RESUME.getLabel(),
					PauseOption.RESTART.getLabel(),
					PauseOption.QUIT.getLabel() };
			drawManager.drawPauseMenu(this, options,
					this.pauseSelection.ordinal());
			// Run summary under the menu (Team ECE - KAN-27).
			drawManager.drawPauseSummary(this,
					new String[] { "SCORE", "STAGE", "ENEMIES KILLED",
							"STAGE TIME", "COINS THIS RUN", "SHIP" },
					new String[] { Integer.toString(this.score),
							Integer.toString(this.level),
							Integer.toString(this.shipsDestroyed),
							formatStageTime(),
							Integer.toString(this.coinsCollected),
							shipSummary() },
					options.length);
			drawManager.drawKeyHints(this,
					"w+s / arrows to move, space to select, P to resume");
		}

		// Countdown to game start (skipped while paused, so the pause menu
		// stays visible instead of being painted over).
		if (!this.paused && !this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
					- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
			drawManager.drawItemHint(this, this.items); // Item System (Team CS)
			drawManager.drawKeyHints(this, "P to pause");
			// Space Opera: sector name, and the two sides on the first
			// stage (Team ECE - KAN-55).
			if (this.spaceStars != null)
				drawManager.drawSectorCard(this,
						engine.SpaceOpera.sectorTitle(this.level),
						this.level == engine.GameMode.SPACE_OPERA.firstLevel()
								? engine.SpaceOpera.PLAYER_FACTION + "  VS  "
										+ engine.SpaceOpera.ENEMY_FACTION
								: null);
			// Boss stages: warning above the countdown (Team ECE - KAN-31).
			if (engine.StageHud.isBossLevel(this.level))
				drawManager.drawBossWarning(this, engine.StageHud
						.isWarningBlinkOn(System.currentTimeMillis()));
		}

		// End-of-stage banner while the next screen loads (Team ECE - KAN-30).
		if (this.stageHud.isStageCleared() && !this.paused) {
			long bannerNow = System.currentTimeMillis();
			drawManager.drawStageClear(this, this.level,
					this.level == Core.getNumLevels(),
					this.stageHud.getStagePoints(),
					this.stageHud.getBannerSlide(bannerNow),
					this.stageHud.isNextLineShown(bannerNow));
		}


		// Game over animation. AUTHORED BY: VFX TEAM (Effection)
		if (this.shrinkingEnemies != null)
			drawShrinkingEnemies();
		if (this.showGameOverText)
			drawGameOverSequence();

		// Draw the notification after every gameplay and HUD element.
		if (this.unlockedAchievement != null)
			drawManager.drawAchievementUnlocked(this, this.unlockedAchievement,
					System.currentTimeMillis() - this.achievementPopupStartedAt,
					ACHIEVEMENT_POPUP_INTERVAL, ACHIEVEMENT_POPUP_SLIDE_IN,
					ACHIEVEMENT_POPUP_SLIDE_OUT);

		drawManager.completeDrawing(this);
	}

	/**
	 * Scatters sparkles from the center of a destroyed ship.
	 * Must be called BEFORE the ship is destroyed, while its position and
	 * size are still valid.
	 * AUTHORED BY: VFX TEAM (Effection)
	 *
	 * @param ship
	 *            Ship that was destroyed.
	 */
	private void spawnSparkles(final EnemyShip ship) {
		float centerX = ship.getPositionX() + ship.getWidth() / 2f;
		float centerY = ship.getPositionY() + ship.getHeight() / 2f;
		for (int i = 0; i < 14; i++) {
			Color color = SPARKLE_COLORS[(int) (Math.random()
					* SPARKLE_COLORS.length)];
			this.particles.add(new Particle(centerX, centerY, color));
		}
	}

	/**
	 * Moves the sparkles and removes the ones that faded away or left the
	 * play area. Called every frame, outside the levelFinished gate, so
	 * sparkles keep fading after a level ends instead of piling up.
	 * Removal happens after the update loop, never during iteration.
	 * AUTHORED BY: VFX TEAM (Effection)
	 */
	private void updateParticles() {
		for (Particle particle : this.particles)
			particle.update();
		this.particles.removeIf(particle -> particle.isDead()
				|| !particle.isInside(0, SEPARATION_LINE_HEIGHT,
				this.width, this.height));
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height
					|| bullet.getPositionX() + bullet.getWidth() < 0
					|| bullet.getPositionX() > this.width)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.ship) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.ship.isDestroyed()
							&& !this.items.tryBlockHit()) { // Shield item (Team CS)
						this.ship.destroy();
						this.lives--;
                        PlayerSounds.playHit(this.lives); // <-- Ajout du son
						this.tookDamage = true; // Survivor tiers (Mantis)
						GameEvents.emit(GameEvents.Type.PLAYER_HIT, this.lives); // AUTHORED BY: VFX TEAM (Effection)
						this.logger.info("Hit on player ship, " + this.lives
								+ " lives remaining.");
					}
				}
			} else {
				if (engine.StageHud.isBossLevel(this.level)) {
					if (this.midBoss != null
							&& !this.midBoss.isDestroyed()
							&& checkCollision(bullet, (Entity) this.midBoss)) {
						this.score += this.midBoss.manageCollisionsBoss(bullet);
						if (this.midBoss.isDestroyed()) {
							this.shipsDestroyed++;
							GameEvents.emit(GameEvents.Type.ENEMY_KILLED, 0); // KAN-38
							engine.RunStats.recordBossBeaten(); // KAN-58
							this.screenShake.trigger();
							showUnlockedAchievement(Core.getAchievementManager()
									.recordBossDefeated());
							showUnlockedAchievement(Core.getAchievementManager()
									.recordEnemyDefeated());
						}
						recyclable.add(bullet);
					}
				} else {
					for (EnemyShip enemyShip : this.enemyShipFormation) {
						if (!enemyShip.isDestroyed()
								&& checkCollision(bullet, enemyShip)) {
							recyclable.add(bullet);
							// Surviving hits consume the bullet without awarding kill rewards.
							if (!enemyShip.takeDamage(1)) {
								SoundManager.getInstance().playSfx(Sound.ENEMY_HIT);
								continue;
							}
							// Score boost (Team CS) times the kill combo (Team ECE - KAN-23).
							addKillScore(this.items.scoreFor(enemyShip.getPointValue()));
							this.shipsDestroyed++;
							spawnSparkles(enemyShip);
							this.enemyShipFormation.destroy(enemyShip);
							if (this.lightningOn) // Lightning Chain. AUTHORED BY: VFX TEAM (Effection)
								this.lightningChain.onKill(
										enemyShip.getPositionX() + enemyShip.getWidth() / 2,
										enemyShip.getPositionY() + enemyShip.getHeight() / 2,
										aliveEnemyCenters());
							this.screenShake.trigger();
							this.pointsEffect.spawn(
									enemyShip.getPositionX() + enemyShip.getWidth() / 2,
									enemyShip.getPositionY() + enemyShip.getHeight() / 2,
									enemyShip.getPointValue());
							queueScoreBoostPopup(enemyShip, false); // Item System (Team CS)
							maybeDropCoin(enemyShip);
							this.items.onEnemyDefeated(enemyShip, false);
							showUnlockedAchievement(Core.getAchievementManager()
									.recordEnemyDefeated());
							recyclable.add(bullet);
						}
					}
				}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					addKillScore(this.items.scoreFor(this.enemyShipSpecial.getPointValue())); // score boost (Team CS), combo (Team ECE)
					this.shipsDestroyed++;
					spawnSparkles(this.enemyShipSpecial); // AUTHORED BY: VFX TEAM (Effection)
					this.enemyShipSpecial.destroy();
					this.screenShake.trigger();
					// Bonus points popup. AUTHORED BY: VFX TEAM (Effection)
					this.pointsEffect.spawnBonus(
							this.enemyShipSpecial.getPositionX() + this.enemyShipSpecial.getWidth() / 2,
							this.enemyShipSpecial.getPositionY() + this.enemyShipSpecial.getHeight() / 2,
							this.enemyShipSpecial.getPointValue());
					queueScoreBoostPopup(this.enemyShipSpecial, true); // Item System (Team CS)
					dropCoin(this.enemyShipSpecial, BONUS_COIN_VALUE);
					this.items.onEnemyDefeated(this.enemyShipSpecial, true);
					showUnlockedAchievement(Core.getAchievementManager()
							.recordEnemyDefeated());
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
				// Bonus meteor: only player bullets hit it. VFX TEAM (Effection)
				if (!recyclable.contains(bullet) && this.meteors.tryHit(bullet))
					recyclable.add(bullet);
			}
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * @return Centers of all enemies that are still alive.
	 *         AUTHORED BY: VFX TEAM (Effection)
	 */
	private java.util.List<java.awt.Point> aliveEnemyCenters() {
		java.util.List<java.awt.Point> centers = new java.util.ArrayList<java.awt.Point>();
		for (EnemyShip e : this.enemyShipFormation)
			if (!e.isDestroyed())
				centers.add(new java.awt.Point(e.getPositionX() + e.getWidth() / 2,
						e.getPositionY() + e.getHeight() / 2));
		return centers;
	}

	/**
	 * Rolls the coin-drop chance for a just-destroyed regular enemy and, if
	 * it succeeds, drops a coin at its position. A coin does not drop on
	 * every kill on purpose: see {@link CoinDropManager} for why.
	 *
	 * @param destroyedEnemy
	 *            Enemy ship that was just destroyed.
	 */
	private void maybeDropCoin(final EnemyShip destroyedEnemy) {
		if (this.coinDropManager.rollForDrop())
			dropCoin(destroyedEnemy, COIN_VALUE);
	}

	/**
	 * Drops a coin worth the given value from the center of a destroyed
	 * enemy. Used directly for the special ship, which always pays out.
	 *
	 * @param destroyedEnemy
	 *            Enemy ship that was just destroyed.
	 * @param value
	 *            Coins awarded when the coin is collected.
	 */
	private void dropCoin(final EnemyShip destroyedEnemy, final int value) {
		this.coins.add(CoinPool.getCoin(
				destroyedEnemy.getPositionX() + destroyedEnemy.getWidth() / 2,
				destroyedEnemy.getPositionY() + destroyedEnemy.getHeight() / 2,
				value));
	}

	/**
	 * Moves falling coins, hands coins touched by the ship to the
	 * CurrencyManager, updates coins collected in the current run, and recycles coins that leave the screen.
	 */
	private void updateCoins() {
		Set<Coin> recyclable = new HashSet<Coin>();
		for (Coin coin : this.coins) {
			coin.update();
			if (this.lives > 0 && !this.ship.isDestroyed()
					&& checkCollision(coin, this.ship)) {
				CurrencyManager.getInstance().addCoins(coin.getValue());
				coinGained(coin); // Team ECE - KAN-38, KAN-39
				this.coinsCollectedThisLevel += coin.getValue();
				this.coinsCollected += coin.getValue();
				recyclable.add(coin);
				this.logger.info("Coin collected, balance: "
						+ CurrencyManager.getInstance().getCoins());
			} else if (coin.getPositionY() > this.height) {
				recyclable.add(coin);
			}
		}
		this.coins.removeAll(recyclable);
		CoinPool.recycle(recyclable);
	}

	/**
	 * Undoes this attempt's coin income when the level is restarted, the
	 * same way score and pending diamonds go back to their starting values,
	 * and returns coins still falling to the pool instead of dropping them.
	 * Never takes more than the current balance, so it can't go negative.
	 */
	private void refundCoinsCollectedThisLevel() {
		CurrencyManager wallet = CurrencyManager.getInstance();
		int refund = Math.min(this.coinsCollectedThisLevel, wallet.getCoins());
		if (refund > 0) {
			wallet.trySpend(refund);
			this.logger.info("Level restarted, took back " + refund
					+ " coins, balance: " + wallet.getCoins());
		}
		// The restarted level's coins no longer count toward the run total.
		this.coinsCollected = Math.max(0,
				this.coinsCollected - this.coinsCollectedThisLevel);
		this.coinsCollectedThisLevel = 0;
		CoinPool.recycle(this.coins);
		this.coins.clear();
	}

	/**
	 * Plays the tense boss music while a boss fights, the faster track in
	 * its last phase, and stops it when the boss or the player dies
	 * (Team ECE - KAN-47).
	 */
	private void updateBossMusic() {
		if (this.midBoss == null)
			return;
		engine.BossMusic.apply(engine.BossMusic.trackFor(
				!this.midBoss.isDestroyed(), this.lives > 0,
				this.bossHud.getPhase(), this.bossHud.getPhaseCount()));
	}

	/**
	 * Tells the HUD a coin was picked up: COIN_GAINED event and a "+N"
	 * popup flying from the coin to the coin balance (Team ECE - KAN-38,
	 * KAN-39).
	 *
	 * @param coin
	 *            Coin picked up.
	 */
	private void coinGained(final Coin coin) {
		GameEvents.emit(GameEvents.Type.COIN_GAINED, coin.getValue());
		SoundManager.getInstance().playSfx(Sound.COIN_PICKUP);
		if (engine.HudSettings.getInstance().isAnimated())
			this.pickupPopups.add(coin.getValue(), Color.YELLOW,
					coin.getPositionX() + coin.getWidth() / 2,
					coin.getPositionY(), this.width / 2,
					engine.HudLayout.PICKUP_POPUP_TARGET_Y,
					System.currentTimeMillis());
	}

	/**
	 * Collects every coin still on screen at once, used when the level is
	 * cleared so drops from the last enemies aren't lost.
	 */
	private void collectRemainingCoins() {
		if (this.coins.isEmpty())
			return;
		int total = 0;
		for (Coin coin : this.coins) {
			total += coin.getValue();
			coinGained(coin); // Team ECE - KAN-38, KAN-39
		}
		CurrencyManager.getInstance().addCoins(total);
		this.coinsCollectedThisLevel += total;
		this.coinsCollected += total;
		this.logger.info("Level cleared, collected " + total
				+ " remaining coins, balance: "
				+ CurrencyManager.getInstance().getCoins());
		CoinPool.recycle(this.coins);
		this.coins.clear();
	}

	/**
	 * Uses an item slot when its number key is pressed (Team CS). Key 1 is
	 * the first slot; holding a key uses the slot only once.
	 */
	private void useItemSlots() {
		int slots = Math.min(this.itemSlotKeysHeld.length,
				this.items.api().getView().slots.size());
		for (int slot = 0; slot < slots; slot++) {
			boolean down = inputManager.isKeyDown(KeyEvent.VK_1 + slot);
			if (down && !this.itemSlotKeysHeld[slot])
				this.items.useSlot(slot);
			this.itemSlotKeysHeld[slot] = down;
		}
	}

	/**
	 * While a score boost is on, the normal points popup shows the base
	 * points and a second popup with the extra points follows a moment
	 * later, a little lower (Team CS). The score itself already includes
	 * the extra points.
	 *
	 * @param enemy
	 *            Enemy that was just destroyed.
	 * @param bonusShip
	 *            Whether it is the bonus ship (uses its own popup style).
	 */
	private void queueScoreBoostPopup(final EnemyShip enemy,
									  final boolean bonusShip) {
		int extra = this.items.scoreBonus(enemy.getPointValue());
		if (extra <= 0)
			return;
		this.scoreBoostPopups.add(new long[] {
				System.currentTimeMillis() + SCORE_BOOST_POPUP_DELAY,
				enemy.getPositionX() + enemy.getWidth() / 2,
				enemy.getPositionY() + enemy.getHeight() / 2
						+ SCORE_BOOST_POPUP_OFFSET_Y, extra,
				bonusShip ? 1 : 0 });
	}

	/** Shows the queued score boost popups whose time has come (Team CS). */
	private void showScoreBoostPopups() {
		long now = System.currentTimeMillis();
		Iterator<long[]> iterator = this.scoreBoostPopups.iterator();
		while (iterator.hasNext()) {
			long[] popup = iterator.next();
			if (popup[0] > now)
				continue;
			if (popup[4] == 1)
				this.pointsEffect.spawnBonus((int) popup[1], (int) popup[2],
						(int) popup[3]);
			else
				this.pointsEffect.spawn((int) popup[1], (int) popup[2],
						(int) popup[3]);
			iterator.remove();
		}
	}

	/**
	 * Takes this frame's item events (Team CS) so they don't pile up. The
	 * item system logs them itself; sound and effects can hook in here.
	 */
	private void handleItemEvents() {
		for (ItemEvent event : this.items.drainEvents()) {
			if (event.type == EventType.ITEM_COLLECTED
					|| event.type == EventType.ITEM_USED)
				ItemSounds.play(event.itemId);
		}
	}

	/**
	 * Displays a popup when an enemy defeat unlocks an achievement.
	 *
	 * @param achievement Newly unlocked achievement, if any.
	 */
	private void showUnlockedAchievement(final Achievement achievement) {
		if (achievement != null)
			this.achievementPopupQueue.add(achievement);
	}

	/** Queues every achievement unlocked by one completed level. */
	private void showUnlockedAchievements(
			final List<Achievement> unlockedAchievements) {
		this.achievementPopupQueue.addAll(unlockedAchievements);
	}

	/** Advances the unlock-popup queue without interrupting gameplay. */
	private void updateAchievementPopup() {
		if (this.unlockedAchievement == null) {
			startNextAchievementPopup();
			return;
		}

		if (System.currentTimeMillis() - this.achievementPopupStartedAt
				>= ACHIEVEMENT_POPUP_INTERVAL) {
			this.unlockedAchievement = null;
			startNextAchievementPopup();
		}
	}

	/** Starts the next queued unlock notification, if there is one. */
	private void startNextAchievementPopup() {
		if (!this.achievementPopupQueue.isEmpty()) {
			this.unlockedAchievement = this.achievementPopupQueue.remove();
			this.achievementPopupStartedAt = System.currentTimeMillis();
			SoundManager.getInstance().playSfx(Sound.ACHIEVEMENT);
		}
	}

	/**
	 * Checks if two entities are colliding.
	 *
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 *
	 * @return Current game state.
	 */
	public final GameState getGameState() {
		return new GameState(this.level, this.score, this.lives,
				this.bulletsShot, this.shipsDestroyed, this.pendingDiamonds,
				this.levelsCompletedRun, this.coinsCollected);
	}
}
