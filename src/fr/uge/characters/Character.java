package fr.uge.characters;

/**
 * Interface representing a character in the game (Hero or Enemy).
 * It defines the common behavior for any entity that has health (PV),
 * protection, and a level.
 */
public sealed interface Character permits Hero, Enemy {
  /**
   * Returns the current health points (PV) of the character.
   * @return the current PV.
   */
  int pv();

  /**
   * Sets the health points of the character.
   * @param pv the new amount of PV.
   */
  void setPv(int pv);

  /**
   * Returns the current protection (armor) value.
   * Protection absorbs damage before health is reduced.
   * @return the protection value.
   */
  int protection();

  /**
   * Sets the protection value.
   * @param protection the new protection value.
   */
  void setProtection(int protection);

  /**
   * Returns the current level of the character.
   * @return the level.
   */
  int level();

  /**
   * Returns the maximum health points the character can have.
   * @return the max PV.
   */
  int maxPv();
  
  /**
   * Makes the character take damage.
   * The logic is:
   * 1. Damage is applied to protection first.
   * 2. Remaining damage is applied to health (PV).
   * * @param damage the amount of damage to take.
   * @throws IllegalArgumentException if damage is negative.
   */
  default void takeDamage(int damage) {
      if (damage < 0) {
          throw new IllegalArgumentException("Damage cannot be negative");
      }
      int damageToProtection = Math.min(damage, protection());
      setProtection(protection() - damageToProtection);
      
      int remainingDamage = damage - damageToProtection;
      setPv(Math.max(0, pv() - remainingDamage));
  }

  /**
   * Adds protection (shield) to the character.
   * @param amount the amount of protection to add.
   * @throws IllegalArgumentException if amount is negative.
   */
  default void addProtection(int amount) {
      if (amount < 0) {
          throw new IllegalArgumentException();
      }
      setProtection(protection() + amount);
  }

  /**
   * Removes protection from the character.
   * The protection cannot go below zero.
   * @param amount the amount to remove.
   * @throws IllegalArgumentException if amount is negative.
   */
  default void removeProtection(int amount) {
    if (amount < 0) {
        throw new IllegalArgumentException();
    }
    setProtection(Math.max(0, protection() - amount));
  }
  
  /**
   * Checks if the character is alive.
   * @return true if pv > 0, false otherwise.
   */
  default boolean isAlive() {
    return pv() > 0;
  }
  
  /**
   * Creates a string representing a visual progress bar.
   * Useful for displaying health or XP in the terminal.
   * * @param current the current value (e.g., current HP).
   * @param max the maximum value (e.g., max HP).
   * @return a String representing the bar (e.g., "[████░░] 60 / 100").
   * @throws IllegalArgumentException if current or max are negative.
   */
  static String progressBar(int current, int max) {
		if(current < 0 || max < 0) {
			 throw new IllegalArgumentException();
		}
		var length = 10;
		if (max <= 0) {
		  return "[" + "░".repeat(length) + "] " + current + " / " + max;
		}
		var filled = Math.min(length, (current * length / max));
		return "[" + "█".repeat(filled) + "░".repeat(length - filled) + "] " + current + " / " + max;
  }
}