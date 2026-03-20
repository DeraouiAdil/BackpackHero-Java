package fr.uge.characters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fr.uge.characters.enums.EnemyActionType;
import fr.uge.characters.enums.EnemyType;

@JsonIgnoreProperties(ignoreUnknown = true)
record EnemyData(
    @JsonAlias({"name", "key"}) EnemyType type,
    int hp,
    int xp,
    int gold,
    int damage,
    int protection,
    boolean isBoss,
    EnemyType minionOf,
    EnemyType summon,
    int minionsMax,
    String img
) {}

/**
 * Represents an Enemy in the game.
 * It loads enemy stats from a JSON file and manages AI behavior (next action).
 */
public final class Enemy implements Character {
  private final EnemyType type;
  private int pv;
  private final int maxPv;
  private int protection;
  private final int protectionStat;
  private int level;
  private final int baseDamage;
  private final int xp;
  private final boolean isBoss;
  private final EnemyType minionOf;
  private final EnemyType summonType;
  private final int minionsMax;
  private final String img;
  private final int gold;
  private int lastDamageTaken = 0;
  
  private EventEnemy nextAction;
  private static final Map<EnemyType, EnemyData> REGISTRY = loadRegistry();

  /**
   * Creates an enemy with a specific type and level.
   * @param type the type of enemy.
   * @param level the level of the enemy (must be >= 1).
   */
  public Enemy(EnemyType type, int level) {
    this.type = Objects.requireNonNull(type);
    if (level < 1) {
      throw new IllegalArgumentException();
    }
    
    EnemyData data = REGISTRY.get(type);
    if (data == null) {
        throw new IllegalArgumentException();
    }

    this.level = level;
    this.maxPv = data.hp() + ((level - 1) * 5); 
    this.pv = this.maxPv;
    
    protection = 0;
    this.protectionStat = data.protection();
    
    this.baseDamage = data.damage();
    this.xp = data.xp();
    this.isBoss = data.isBoss();
    this.minionOf = data.minionOf();
    this.summonType = data.summon();
    this.minionsMax = data.minionsMax();
    this.img = data.img();
    this.gold = data.gold();
    
    updateNextAction();
  }

  /**
   * Creates a level 1 enemy of the specified type.
   * @param type the type of enemy.
   */
  public Enemy(EnemyType type) {
    this(type, 1);
  }

  private static Map<EnemyType, EnemyData> loadRegistry() {
    ObjectReader reader = new ObjectMapper().reader();
    var file = new File("data/json/enemies.json");

    try (JsonParser parser = reader.createParser(file)) {
      List<EnemyData> list = parser.readValueAs(new TypeReference<List<EnemyData>>() {});
      return list.stream().collect(Collectors.toMap(EnemyData::type, Function.identity()));
    } catch (IOException e) {
      throw new IllegalStateException("Impossible de charger data/json/enemies.json", e);
    }
  }

  @Override
  public void addProtection(int amount) {
    if (amount < 0) {
        throw new IllegalArgumentException();
    }
    protection += amount;
  }

  /**
   * Resets the enemy protection to 0.
   */
  public void resetProtection() {
  	protection = 0;
  }

  public int xp() {return xp;}
  @Override public int pv() { return pv; }
  @Override public int maxPv() { return maxPv; }
  @Override public int level() { return level; }
  @Override public void setPv(int pv) { this.pv = pv; }
  @Override public int protection() { return protection; }
  @Override public void setProtection(int protection) { this.protection = protection; }
  
  public EnemyType type() { return type; }
  
  /**
   * Checks if this enemy is a boss.
   * @return true if boss.
   */
  public boolean isBoss() {
  	return isBoss; 
  }
  
  /**
   * Checks if this enemy is a minion serving a specific boss.
   * @return true if it is a minion.
   */
  public boolean isMinion() {
  	return minionOf != null; 
  }
 
  /**
   * Returns true if this enemy is a summoner (like a Boss with minions).
   * @return true if can summon.
   */
  public boolean canSummon() {
    return summonType != null && minionsMax > 0;
  }
  
  public EnemyType getSummonType() { return summonType; }
  public int getMinionsMax() { return minionsMax; }
  
  public int gold() {
  	return gold;
  }
  
  public String img(){ return img; }
  public int damage() { return baseDamage; }
  
  private static List<Enemy> getBosses(int level){
    return REGISTRY.values().stream()
                            .filter(EnemyData::isBoss)
                            .map(e -> new Enemy(e.type(), level))
                            .toList();
  }

  
  /**
   * Generates a random normal enemy (excluding bosses and minions).
   */
  private static Enemy randomNormal(int level) {
    var types = REGISTRY.values().stream()
        .filter(data -> !data.isBoss())
        .filter(data -> data.minionOf() == null)
        .map(EnemyData::type)
        .toList();
        
    var randomType = types.get(ThreadLocalRandom.current().nextInt(types.size()));
    return new Enemy(randomType, level);
  }

  private static Enemy randomBoss(int level) {
    var bosses = getBosses(level);
    if (bosses.isEmpty()) {
      return new Enemy(EnemyType.RATWOLF, level);
    }
    return bosses.get(ThreadLocalRandom.current().nextInt(bosses.size()));
  }
  
  /**
   * Creates a random enemy based on hero level.
   * @param heroLevel the level of the hero.
   * @param boss true if we want to generate a boss.
   * @return a new random Enemy.
   */
  public static Enemy random(int heroLevel, boolean boss) {
    if (heroLevel < 1) throw new IllegalArgumentException();
    return boss ? randomBoss(heroLevel) : randomNormal(heroLevel);
  }
  
  /**
   * Creates a random normal enemy based on hero level.
   * @param heroLevel the level of the hero.
   * @return a new random Enemy.
   */
  public static Enemy random(int heroLevel) {
    return random(heroLevel, false);
  }
  
  /**
   * Gets the next action the enemy has planned to do.
   * @return the EventEnemy (Attack, Protection, Curse).
   */
  public EventEnemy nextAction() { return nextAction; }

  /**
   * Calculates a new random action for the enemy for the next turn.
   * Some enemies (like FrogWizard) have special actions like Curse.
   */
  public void updateNextAction() {
  	if(this.type == EnemyType.FROGWIZARD) {
  		if(new Random().nextInt(10) == 0) {
  			this.nextAction = new EventEnemy(EnemyActionType.CURSE, 1);
  			return;
  		}
  	}

    this.nextAction = EventEnemy.nextAction(baseDamage, protectionStat);
  }

  /**
   * Attacks a hero using the enemy's base damage.
   * @param hero the hero to attack.
   */
  public void attack(Hero hero) {
    Objects.requireNonNull(hero);
    hero.takeDamage(baseDamage);
  }
  
  /**
   * Attacks a hero with specific damage amount.
   * @param hero the hero to attack.
   * @param damage the amount of damage.
   */
  public void attack(Hero hero, int damage) {
    Objects.requireNonNull(hero);
    if (damage < 0) throw new IllegalArgumentException();
    hero.takeDamage(damage);
  }
  
  /**
   * Sets the value of the last damage taken (for display purposes).
   * @param amount the damage amount.
   */
  public void setLastDamageTaken(int amount) {
  	this.lastDamageTaken = amount;
  }
  
  @Override
  public void takeDamage(int amount) {
  	this.pv -= amount;
  	if(this.pv < 0) {
  		this.pv = 0;
  	}
  }
  
  public int getLastDamageTaken() {
  	return this.lastDamageTaken;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("+----------------------------------+\n");
    sb.append(isBoss ? "| BOSS                             |\n" : "| ENEMY                            |\n");
    sb.append("+----------------------------------+\n");
    sb.append(String.format("| Type       : %-14s      |\n", type.name()));
    sb.append(String.format("| Niveau     : %-14d      |\n", level));
    sb.append(String.format("| PV         : %-14s |\n", Character.progressBar(pv, maxPv)));
    sb.append(String.format("| Dégâts     : %-14d      |\n", baseDamage));
    sb.append(String.format("| Protection : %-14d      |\n", protection));
    sb.append("+-----------------------------------+\n");
    return sb.toString();
  }
}