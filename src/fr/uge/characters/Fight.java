package fr.uge.characters;

import java.util.Objects;
import fr.uge.characters.enums.EnemyActionType;
import fr.uge.characters.enums.HeroActionType;

/**
 * Manages the fight logic/state between a Hero and an Enemy.
 * Because Fight is a Record (immutable), methods return a new Fight instance
 * representing the state after an action.
 * * @param hero the hero fighting.
 * @param isHeroTurn true if it is currently the hero's turn.
 * @param lastActions record of what happened in the previous turn (for display).
 */
public record Fight(Hero hero, boolean isHeroTurn, LastActions lastActions) {
    
  public Fight {
    Objects.requireNonNull(hero);
    Objects.requireNonNull(lastActions);
  }

  /**
   * Starts a new fight with the specified hero.
   * It defaults to the Hero's turn.
   * @param hero the hero entering the fight.
   */
  public Fight(Hero hero) {
  	this(hero, true, new LastActions());
  }
    
  /**
   * Executes the enemy's attack turn.
   * Calculates damage, protection, or curse based on the enemy's intent.
   * * @param enemy the enemy performing the action.
   * @return a FightResult containing the new fight state and the updated enemy.
   * @throws IllegalStateException if called during Hero's turn.
   */
  public FightResult enemyAttacks(Enemy enemy) {
    Objects.requireNonNull(enemy);
    if(isHeroTurn){ throw new IllegalStateException(); }
    
    var cleanedEnemy = defaultProtectionEnemy(enemy);
    var action = cleanedEnemy.nextAction();

    var newLastActions = lastActions;
    switch (action.enemyAction()) {
      case ATTACK -> {
        cleanedEnemy.attack(hero);
        newLastActions = new LastActions(lastActions.heroAction(), lastActions.amountHeroAction(), EnemyActionType.ATTACK, action.value(),0);
      }
      case PROTECTION -> {
        cleanedEnemy.addProtection(action.value());
        newLastActions = new LastActions(lastActions.heroAction(),lastActions.amountHeroAction(),EnemyActionType.PROTECTION,action.value(),0);
      }
      case CURSE -> {
      	boolean sucess = hero.backpack().curseRandomItem();
      	int resultVal = sucess ? 1 : 0;
      	newLastActions = new LastActions(lastActions.heroAction(), lastActions.amountHeroAction(), EnemyActionType.CURSE, resultVal, 0);
      }
    }
    cleanedEnemy.updateNextAction();

    var newFight = new Fight(hero, true, newLastActions);
    return new FightResult(newFight, cleanedEnemy);
  }

  /**
   * Executes the hero's attack against an enemy.
   * * @param enemy the enemy to attack.
   * @param action the type of action (must be ATTACK).
   * @param amount the damage amount to deal.
   * @return a FightResult containing the new fight state and the updated enemy.
   * @throws IllegalStateException if called during Enemy's turn.
   */
  public FightResult heroAttacks(Enemy enemy, HeroActionType action, int amount) {
    Objects.requireNonNull(enemy);
    Objects.requireNonNull(action);
    if (amount < 0) { throw new IllegalArgumentException(); }
    if (!isHeroTurn) { throw new IllegalStateException(); }
    
    if (action != HeroActionType.ATTACK) {
        throw new IllegalArgumentException("Action invalide pour heroAttacks.");
    }
    var cleanedHero = defaultProtection();
    enemy.takeDamage(amount);
    
    var newLastActions = new LastActions(HeroActionType.ATTACK, amount, lastActions.enemyAction(), lastActions.amountEnemyAction(), 0);
   
    var newFight = new Fight(cleanedHero, true, newLastActions);

    return new FightResult(newFight, enemy);
  }

  /**
   * The hero uses protection (Shield).
   * Adds armor to the hero for the turn.
   * * @param amount the amount of protection to add.
   * @return a new Fight state with updated hero protection.
   * @throws IllegalStateException if called during Enemy's turn.
   */
  public Fight heroProtects(int amount) {
    if (amount < 0) { throw new IllegalArgumentException(); }
    if (!isHeroTurn) { throw new IllegalStateException(); }


    hero.addProtection(amount);
    //var cleanedHero = defaultProtection();
    //cleanedHero.addProtection(amount);
    var newLastActions = new LastActions(HeroActionType.PROTECTION, amount, lastActions.enemyAction(), lastActions.amountEnemyAction(), 0);
    
    return new Fight(hero, true, newLastActions);

  }
  
  /**
   * Removes temporary protection from the hero at the start of a new turn.
   */
  private Hero defaultProtection() {
    if (lastActions.amountHeroAction() == 0 || lastActions.heroAction() != HeroActionType.PROTECTION) {
      return hero;
    }
    hero.removeProtection(lastActions.amountHeroAction());
    return hero;
  }
    
  /**
   * Removes temporary protection from the enemy at the start of a new turn.
   */
  private Enemy defaultProtectionEnemy(Enemy enemy) {
    if(lastActions.amountEnemyAction() == 0 || lastActions.enemyAction() != EnemyActionType.PROTECTION){
      return enemy;
	  }
	  enemy.removeProtection(lastActions.amountEnemyAction());
	  return enemy;
  }
  
  /**
   * Applies specific damage to the hero regardless of turn.
   * Used for special events or refusal of curses.
   * @param enemy the enemy involved.
   * @param forcedDamage the damage the hero must take.
   * @return the result.
   */
  public FightResult enemyAttacksCustom(Enemy enemy, int forcedDamage) {
  	Objects.requireNonNull(enemy);
  	hero.takeDamage(forcedDamage);
  	return new FightResult(this, enemy);
  }
 
  /**
   * Checks if the game is over (hero is dead).
   * @return true if hero is dead.
   */
  public boolean isGameOver() {
      return !hero.isAlive();
  }
  
  /**
   * Creates a new Fight state with the turn flag changed.
   * @param isHeroTurn true if it becomes hero's turn.
   * @return new Fight instance.
   */
  public Fight withHeroTurn(boolean isHeroTurn) {
  	return new Fight(this.hero,isHeroTurn,this.lastActions);
  }
}