package fr.uge.characters;

import java.util.Objects;

/**
 * The result of a round of fighting.
 * It contains the updated fight state (turn change, last actions) 
 * and the updated enemy state (damage taken).
 * * @param fight the new current state of the fight.
 * @param updatedEnemy the enemy after the round (e.g., with reduced PV).
 */
public record FightResult(Fight fight, Enemy updatedEnemy) {
  public FightResult {
    Objects.requireNonNull(fight);
    Objects.requireNonNull(updatedEnemy);
  }
}