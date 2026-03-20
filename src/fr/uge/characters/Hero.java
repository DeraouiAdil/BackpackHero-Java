package fr.uge.characters;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.Level;
import fr.uge.characters.enums.HeroType;
import fr.uge.dongeon.Room;
import fr.uge.items.Armor;
import fr.uge.items.Item;
import fr.uge.items.Weapon;
import fr.uge.items.enums.ArmorType;
import fr.uge.items.enums.WeaponName;

@JsonIgnoreProperties(ignoreUnknown = true)
record HeroData(
    HeroType name,
    @JsonAlias("pvmax") int pv,
    int energy,
    int mana,
    int protection,
    String img
) {}

/**
 * Represents the main character played by the user.
 * The hero has a backpack, stats (HP, Mana, Energy), a position in the dungeon,
 * and a score. The initial stats are loaded from a JSON file.
 */
public final class Hero implements Character {
  private final String username;
  private final HeroType heroType;
  private final BackPack backpack;
  private Coordinate position;
  private int pv;
  private int maxPv;
  private int energy;
  private int mana;
  private int protection;
  private Level lvl;
  private int score;
  private final String img;

  private static final Map<HeroType, HeroData> REGISTRY = loadRegistry();

  /**
   * Creates a new Hero instance.
   * * @param username the name of the player.
   * @param heroType the class/type of the hero.
   * @param backpack the inventory of the hero.
   * @param position the initial coordinates in the map.
   * @param lvl the current level object tracking XP.
   */
  public Hero(String username, HeroType heroType, BackPack backpack, Coordinate position, Level lvl) {
    this.username = Objects.requireNonNull(username);
    this.heroType = Objects.requireNonNull(heroType);
    this.backpack = Objects.requireNonNull(backpack);
    this.position = Objects.requireNonNull(position);
    this.lvl = Objects.requireNonNull(lvl);

    this.score = 0; 

    var data = REGISTRY.get(this.heroType);
    if (data == null) {
        throw new IllegalArgumentException();
    }

    this.maxPv = data.pv() + (lvl.level() * 5); 
    this.pv = this.maxPv;
    this.energy = data.energy();
    this.mana = data.mana();
    this.protection = data.protection();
    this.img = data.img();
    backpack.add(new Weapon(WeaponName.DAMAGEDKNIFE, 1), new Coordinate(1, 1));
  }

  /**
   * Creates a default hero (PURSE type) at position (0,0).
   * @param username the name of the player.
   */
  public Hero(String username) {
    this(username, HeroType.PURSE, new BackPack(), new Coordinate(0, 0), new Level());
  }

  /**
   * Loads the hero data from the JSON file.
   * @return a map associating HeroType to its data.
   */
  private static Map<HeroType, HeroData> loadRegistry() {
    ObjectReader reader = new ObjectMapper().reader();
    var file = new File("data/json/heroes.json");

    try (JsonParser parser = reader.createParser(file)) {
      List<HeroData> list = parser.readValueAs(new TypeReference<List<HeroData>>() {});
      return list.stream().collect(Collectors.toMap(HeroData::name, Function.identity()));
    } catch (IOException e) {
      throw new IllegalStateException("Impossible de charger data/json/heroes.json", e);
    }
  }
  
  @Override
  public boolean isAlive() {
  	return pv > 0;
  }

  @Override
  public int pv() {
  	return pv; 
  }
  
  public String username() {
  	return username; 
  }
  
  public HeroType heroType() {
  	return heroType; 
  }
  
  public String img() {
  	return img; 
  }
  
  @Override
  public void setPv(int pv) {
    if (pv < 0) throw new IllegalArgumentException();
    this.pv = pv;
  }

  @Override
  public int protection() {
  	return protection; 
  }

  @Override
  public void setProtection(int protection) {
    if (protection < 0) {
    	throw new IllegalArgumentException();
    }
    this.protection = protection;
  }

  @Override
  public int maxPv() { return maxPv; }
  
  public int energy() { return energy; }
  
  public int mana() { return mana; }
  
  public Level lvl() { return lvl; }
  
  @Override
  public int level() { return lvl.level(); }
  
  public int score() { return score; }
  
  public BackPack backpack() { return backpack; }
  
  public Coordinate position() { return position; }

  /**
   * Updates the maximum PV based on the current level.
   */
  private void updateMaxPv() {
    var data = REGISTRY.get(this.heroType);
    maxPv = data.pv() + (lvl.level() * 5);
  }

  /**
   * Heals the hero by a specified amount.
   * Cannot exceed maxPv.
   * @param amount the amount of PV to restore.
   */
  public void heal(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    pv = Math.min(pv + amount, maxPv);
    return;
  }

  /**
   * Gives XP to the hero and levels up if the threshold is reached.
   * Also increases backpack space and stats on level up.
   * @param amount the amount of XP to gain.
   */
  public void gainXp(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    var oldLevel = lvl.level();
    lvl = lvl.addXp(amount);
    if (lvl.level() > oldLevel) {
//      var oldMaxPv = maxPv;
      updateMaxPv();
      backpack.spaceUp();
      
//      if (maxPv > oldMaxPv) {
//        pv += (maxPv - oldMaxPv);
//      }
    }
  }

  /**
   * Adds points to the hero's current score.
   * @param amount the score to add.
   */
  public void addScore(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    score += amount;
  }
  
  /**
   * Calculates the total protection provided by items in the backpack
   * (excluding shields which are active items).
   * @return total passive protection.
   */
  public int getTotalProtection() {
  int total = 0;
  for(var item : this.backpack.items().values()) {
  	switch(item) {
  	case Armor a when a.getArmorType() != ArmorType.SHIELD -> total += a.getProtection();
  	default -> {}
  		}
  	}
  return total;
  }

  /**
   * Calculates the final score for the leaderboard.
   * Combines score, max PV, and the value of items in the backpack.
   * @return the computed final score.
   */
  public long computeFinalScore() {
    return score + (maxPv * 2) + backpack.price();
  }

  /**
   * Consumes energy.
   * @param amount the amount of energy to use.
   * @throws IllegalArgumentException if amount is negative.
   */
  public void useEnergy(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    if (energy - amount < 0) {
      return;
    }
    energy -= amount;
  }

  /**
   * Adds energy to the hero, capped at 3.
   * @param amount the amount of energy to add.
   */
  public void addEnergy(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    energy += amount;
    if(energy > 3) {
    	energy = 3;
    }
  }

  /**
   * Consumes mana.
   * @param amount the amount of mana to use.
   */
  public void useMana(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    if (mana - amount < 0) {
    	return;
    }
    mana -= amount;
  }

  /**
   * Adds mana to the hero.
   * @param amount the amount of mana to add.
   */
  public void addMana(int amount) {
    if (amount < 0) {
    	throw new IllegalArgumentException();
    }
    mana += amount;
  }

  /**
   * Adds an item to the backpack at a specific location.
   * @param item the item to add.
   * @param click the coordinate in the bag.
   * @return true if added, false if there is no space.
   */
  public boolean addItem(Item item, Coordinate click) {
    return backpack.add(item, click);
  }

  /**
   * Attacks an enemy directly (outside of the Fight record logic).
   * @param enemy the enemy to attack.
   * @param damage the amount of damage.
   */
  public void attack(Enemy enemy, int damage) {
    Objects.requireNonNull(enemy);
    if (damage < 0) {
    	throw new IllegalArgumentException();
    }
    enemy.takeDamage(damage);
  }

  /**
   * Moves the hero along a path of rooms.
   * Updates the hero's position to the next room in the path.
   * * @param path the list of rooms constituting the path.
   * @return a MoveResult containing the updated hero and the remaining path.
   */
  public MoveResult move(LinkedList<Room> path) {
    Objects.requireNonNull(path);
    if (path.isEmpty()) {
      return new MoveResult(this, path);
    }
    if (path.getFirst().coordRoom().equals(position)) {
      path.poll();
      if (path.isEmpty()) {
          return new MoveResult(this, path);
      }
    }
    
    var nextRoom = path.getFirst();
    this.position = nextRoom.coordRoom();
    path.poll();
    return new MoveResult(this, path);
  }
  
  @Override
  public void addProtection(int amount) {
  	if(amount < 0) {
  		return;
  	}
  	this.protection += amount;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("+------------------------------------------------+\n");
    sb.append("| HERO : " + username + " (" + heroType + ")       \n");
    sb.append("+------------------------------------------------+\n");
    sb.append(String.format("| LVL : %-3d                                      |\n", lvl.level()));
    sb.append(String.format("| PV     : %-20s                 |\n", Character.progressBar(pv, maxPv)));
    sb.append(String.format("| Énergie: %-3d                                  |\n", energy));
    sb.append(String.format("| Mana   : %-3d                                  |\n", mana));
    sb.append(String.format("| Protection : %-3d  Or :                  |\n", protection));
    sb.append(String.format("| Image : %-30s         |\n", img));
    sb.append("+------------------------------------------------+\n");
    return sb.toString();
  }
}