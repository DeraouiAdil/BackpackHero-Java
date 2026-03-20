package fr.uge.items;

import java.util.Objects;

/**
 * Stores the selection of a weapon and its damage.
 * <p>
 * This record is used during the fight logic to pass the specific instance of a weapon
 * along with the damage it deals in the current context.
 * </p>
 * * @param item   The weapon item being used.
 * @param damage The calculated damage amount.
 */
public record WeaponSelection(Item item, int damage){
	/**
	 * Canonical constructor with validation.
	 * * @param item   The weapon item.
	 * @param damage The damage (must be non-negative).
	 * @throws NullPointerException if item is null.
	 * @throws IllegalArgumentException if damage is negative.
	 */
	public WeaponSelection {
		Objects.requireNonNull(item);
		if(damage < 0) {
			throw new IllegalArgumentException();
		}
	}
}