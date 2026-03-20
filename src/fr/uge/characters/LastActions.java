package fr.uge.characters;

import java.util.Objects;

import fr.uge.characters.enums.EnemyActionType;
import fr.uge.characters.enums.HeroActionType;

/**
 * Stores the details of the last turn in a fight.
 * This record is primarily used by the View to display what happened
 * (e.g., "Hero attacked for 5 dmg", "Enemy protected").
 * * @param heroAction what the hero did.
 * @param amountHeroAction value of the hero's action.
 * @param enemyAction what the enemy did.
 * @param amountEnemyAction value of the enemy's action.
 * @param indexEnemy index of the enemy who acted (if multiple).
 */
public record LastActions(HeroActionType heroAction, int amountHeroAction, EnemyActionType enemyAction , int amountEnemyAction,int indexEnemy) {
	public LastActions{
		Objects.requireNonNull(heroAction);
		Objects.requireNonNull(enemyAction);
		if(amountEnemyAction < 0 || amountHeroAction < 0 || indexEnemy < 0) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Creates a default LastActions with 0 values.
	 * Used to initialize a fight before any action has occurred.
	 */
	public LastActions() {
		this(HeroActionType.ATTACK,0,EnemyActionType.ATTACK,0,0); // On peut mettre un attaque a 0 comme initialisation car l'information d'une attaque ici ne nous interesse pas
	}
}