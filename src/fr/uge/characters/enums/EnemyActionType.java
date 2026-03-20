package fr.uge.characters.enums;

/**
 * Enum representing the possible types of actions an enemy can take.
 */
public enum EnemyActionType {
    /** The enemy will increase its armor. */
	PROTECTION,
	/** The enemy will deal damage to the hero. */
	ATTACK,
	/** The enemy will attempt to curse an item in the hero's backpack. */
	CURSE
}