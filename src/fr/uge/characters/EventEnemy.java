package fr.uge.characters;

import java.util.Objects;
import java.util.Random;
import fr.uge.characters.enums.EnemyActionType;

/**
 * Represents an intended action by an enemy for the next turn.
 * It contains the type of action (Attack, Protection, etc.) and its value.
 * * @param enemyAction the type of action.
 * @param value the numeric value associated (damage amount or protection amount).
 */
public record EventEnemy(EnemyActionType enemyAction, int value) {
  private static final Random RANDOM = new Random();

  public EventEnemy {
    Objects.requireNonNull(enemyAction);
    if (value < 0) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Generates a random next action for a standard enemy.
   * It randomly chooses between attacking or protecting based on the provided stats.
   * * @param damage the damage value if it chooses to attack.
   * @param protection the protection value if it chooses to protect.
   * @return a new EventEnemy.
   */
  public static EventEnemy nextAction(int damage, int protection) {
  	if(damage < 0 || protection < 0) {
  		throw new IllegalArgumentException();
  	}
  	boolean isAttack = RANDOM.nextBoolean();
  	if(isAttack) {
  		return new EventEnemy(EnemyActionType.ATTACK, damage);
  	}else {
  		return new EventEnemy(EnemyActionType.PROTECTION,protection);
  	}
  }
}