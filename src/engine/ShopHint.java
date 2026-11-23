package engine;

import java.time.LocalDate;

import shop.Currency;
import shop.ShopCatalog;
import shop.ShopItem;

/**
 * Tells the HUD when the player can afford something in the shop
 * (Requirement 8 - Gameplay HUD, KAN-40).
 *
 * It only reads the Currency System's shop data (ShopCatalog and
 * PurchaseManager) and keeps no copy of prices. The cheapest price is
 * looked up once per stage: the shop cannot be opened during a stage, so
 * prices and ownership do not change until the next one.
 */
public final class ShopHint {

	/** Blink half-period, in ms, of the hint icon. */
	public static final long BLINK_MS = 450;

	/** Cheapest coin price among entries that can still be bought, or -1
	 * when everything is owned. */
	private final int cheapestCoinPrice;

	/** Looks up the cheapest coin price now. */
	public ShopHint() {
		this(findCheapestCoinPrice());
	}

	/**
	 * Creates a hint from a known price, for tests.
	 *
	 * @param cheapestCoinPrice
	 *            Cheapest coin price, or -1 when nothing can be bought.
	 */
	public ShopHint(final int cheapestCoinPrice) {
		this.cheapestCoinPrice = cheapestCoinPrice;
	}

	/**
	 * @return Cheapest coin price of the shop entries that are on sale and
	 *         not owned or maxed yet, or -1 if there is none.
	 */
	public static int findCheapestCoinPrice() {
		PurchaseManager shop = PurchaseManager.getInstance();
		LocalDate today = LocalDate.now();
		int cheapest = -1;
		for (ShopItem item : ShopCatalog.getAll()) {
			if (!ShopCatalog.isOnSale(item, today))
				continue;
			int price = shop.getPrice(item, Currency.COINS);
			if (price > 0 && (cheapest < 0 || price < cheapest))
				cheapest = price;
		}
		return cheapest;
	}

	/**
	 * @param coins
	 *            Current coin balance.
	 * @return True if the balance covers the cheapest entry still for sale.
	 */
	public boolean canAffordSomething(final int coins) {
		return this.cheapestCoinPrice > 0 && coins >= this.cheapestCoinPrice;
	}

	/**
	 * @return Cheapest coin price, or -1 when everything is owned.
	 */
	public int getCheapestCoinPrice() {
		return this.cheapestCoinPrice;
	}

	/**
	 * @param now
	 *            Current time in ms.
	 * @return True on the bright frames of the hint pulse.
	 */
	public static boolean isPulseOn(final long now) {
		return (now / BLINK_MS) % 2 == 0;
	}
}
