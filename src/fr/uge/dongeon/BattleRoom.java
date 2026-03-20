package fr.uge.dongeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import fr.uge.Coordinate;
import fr.uge.characters.Enemy;
import fr.uge.characters.Fight;
import fr.uge.characters.FightResult;
import fr.uge.characters.Hero;
import fr.uge.characters.enums.EnemyType;
import fr.uge.characters.enums.HeroActionType;

/**
 * Represents a room where a fight takes place.
 * It contains enemies or a boss.
 */
public final class BattleRoom implements Room {
  private static final int NB_ENEMY = 3;
  private final Coordinate roomCoordinate;
  private final ArrayList<Enemy> enemies; 
  private final boolean bossRoom;
  
  private Enemy activeBoss;
  
  /**
   * Creates a battle room.
   * @param roomCoordinate the position of the room.
   * @param heroLevel the level of the hero (to set enemy difficulty).
   * @param bossRoom true if this is a boss room.
   */
  public BattleRoom(Coordinate roomCoordinate, int heroLevel, boolean bossRoom) {
    Objects.requireNonNull(roomCoordinate);
    if (heroLevel < 0) {
      throw new IllegalArgumentException();
    }
    this.roomCoordinate = roomCoordinate;
    this.bossRoom = bossRoom;
    enemies = new ArrayList<>();
    populateEnemies(heroLevel);
  }
  
  /**
   * Fills the room with enemies.
   * @param heroLevel the level used to create enemies.
   */
  private void populateEnemies(int heroLevel) {
    if (bossRoom) {
      this.activeBoss = Enemy.random(heroLevel, true);
      enemies.add(activeBoss);
      
      if (activeBoss.canSummon()) {
        spawnMinions(activeBoss, heroLevel);
      }
    } else {
      for (int i = 0; i < NB_ENEMY; i++) {
        enemies.add(Enemy.random(heroLevel, false));
      }
    }
  }
  
  private void spawnMinions(Enemy boss, int level) {
    var summonType = boss.getSummonType();
    var currentMinions = enemies.stream()
        .filter(e -> e.type() == summonType)
        .filter(Enemy::isAlive)
        .count();
    var needed = boss.getMinionsMax() - (int) currentMinions;
    if (needed <= 0) return;
    for (int i = 0; i < enemies.size() && needed > 0; i++) {
      var e = enemies.get(i);
      if (e.type() == summonType && !e.isAlive()) {
        enemies.set(i, new Enemy(summonType, level));
        needed--;
      }
    }
    for (int i = 0; i < needed; i++) {
      enemies.add(new Enemy(summonType, level));
    }
}

  /**
   * Updates the state of the room during the fight.
   * Specifically handles the logic for Bosses that can summon minions.
   */
  public void updateRoomState() {
    if (!bossRoom || activeBoss == null || !activeBoss.isAlive()) {
      return;
    }
    if (activeBoss.canSummon()) {
      spawnMinions(activeBoss, activeBoss.level());
    }
}
  
  /**
   * Gets a specific enemy.
   * @param index the index of the enemy in the list.
   * @return the enemy.
   */
  public Enemy enemy(int index) {
    if (index < 0 || index >= enemies.size()) {
      throw new IllegalArgumentException("Index invalide : " + index);
    }
    return enemies.get(index);
  }
  
  /**
   * Safe way to get an enemy (returns null if not found).
   * @param index the index.
   * @return the enemy or null.
   */
  public Enemy getEnemy(int index) {
    if (index < 0 || index >= enemies.size()) {
      return null;
    }
    return enemy(index);
  }

  /**
   * Returns the list of all enemies in the room.
   * @return list of enemies.
   */
  public List<Enemy> enemies() {
    return List.copyOf(enemies);
  }
  
  /**
   * Checks if the room is safe (no enemies alive).
   * @return true if all enemies are dead.
   */
  public boolean isCleared() {
    return enemies.isEmpty() || enemies.stream().noneMatch(Enemy::isAlive);
  }

  /**
   * Executes the hero's turn in the fight.
   * @param fight the current fight state.
   * @param action the action the hero does.
   * @param amount the damage amount.
   * @param index which enemy to target.
   * @return the result of the turn.
   */
  public FightResult heroTurn(Fight fight, HeroActionType action, int amount, int index) {
    Objects.requireNonNull(fight);
    Objects.requireNonNull(action);

    var enemy = enemy(index);
    int hpBefore = enemy.pv();
    
    var res = fight.heroAttacks(enemy, action, amount);
    
    var updatedEnemy = res.updatedEnemy();
    int damageTaken = hpBefore - updatedEnemy.pv();
    
    int currentStored = updatedEnemy.getLastDamageTaken();
    updatedEnemy.setLastDamageTaken(currentStored + amount);
    
    enemies.set(index, res.updatedEnemy());
    updateRoomState();
    return res;
  }
  

  /**
   * Executes the enemy's turn.
   * @param fight the current fight state.
   * @param index the index of the enemy attacking.
   * @return the result of the turn.
   */
  public FightResult enemyTurn(Fight fight, int index) {
    Objects.requireNonNull(fight);
  
    if (index < 0 || index >= enemies.size()) {
      throw new IllegalArgumentException("Index d'ennemi invalide : " + index);
    }
    var enemy = enemies.get(index);
    if (!enemy.isAlive()) {
      return new FightResult(fight, enemy); 
    }
    enemy.resetProtection();
    
    if(enemy.type() == EnemyType.MIMIC) {
    	int damageToReflect = enemy.getLastDamageTaken();
    	if(damageToReflect <= 0) {
    		damageToReflect = 5 + enemy.level();
    	}
    	return fight.enemyAttacksCustom(enemy, damageToReflect);
    }
    
    
    var res = fight.enemyAttacks(enemy);
    enemies.set(index, res.updatedEnemy());
    return res;
  }

  @Override
  public Coordinate coordRoom() {
    return roomCoordinate;
  }
  
  @Override
  public String getSymbol() {
    return "F";
  }
  
  @Override
  public TypeRoom getType() {
    return (bossRoom?TypeRoom.BOSSROOM:TypeRoom.BATTLEROOM);
  }


  /**
   * Gives XP to the hero when the fight is over.
   * @param hero the hero who won.
   */
  public void claimRewards(Hero hero) {
  	Objects.requireNonNull(hero);
  	var totalXp = enemies.stream().mapToInt( p -> p.xp()).sum();
  	
  	var totalGOld = enemies.stream().mapToInt(p -> p.gold()).sum();
  	
  	hero.gainXp(totalXp);
  	hero.backpack().addGold(totalGOld);
  }

  /**
   * Creates a string to show the current status of the battle.
   * @param hero the hero.
   * @param turn the current turn number.
   * @return the battle info text.
   */
  public String battleInfo(Hero hero, int turn) {
    Objects.requireNonNull(hero);
    if (turn < 0) throw new IllegalArgumentException();
    
    var sb = new StringBuilder();
    sb.append("Tour: ").append(turn).append("\n")
      .append(hero.toString()).append("\n");
    
    for (int i = 0; i < enemies.size(); i++) {
        var e = enemies.get(i);
        if (e.isAlive()) { 
          sb.append("Ennemi #").append(i).append(": ").append(e).append("\n");
        } else {
          sb.append("Ennemi #").append(i).append(": (Mort)\n");
        }
    }
    return sb.toString();
  }
}