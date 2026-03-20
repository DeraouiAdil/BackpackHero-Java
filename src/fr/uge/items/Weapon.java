package fr.uge.items;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fr.uge.Coordinate;
import fr.uge.items.enums.Rarity;
import fr.uge.items.enums.WeaponName;
import fr.uge.items.enums.WeaponType;

@JsonIgnoreProperties(ignoreUnknown = true) 
record WeaponData(
  WeaponName name,
  @JsonProperty("category") WeaponType type,
  Rarity rarity,
  boolean isCursed,
  int damage,
  Cost cost,
  List<Coordinate> shape
) {}


/**
 * Represents a weapon item that allows the hero to deal damage.
 * <p>
 * The base statistics for weapons (damage, rarity, shape) are loaded dynamically
 * from a JSON registry file. Weapons can be rotated, leveled up, and can be
 * subjected to curse mechanics.
 * </p>
 */
public final class Weapon implements Item {
  private final UUID id;
  private final WeaponName weaponName;
  private final WeaponType weaponType;
  private final Rarity rarity;
  private boolean isCursed;
  private final List<Coordinate> shape;
  private final Cost cost;
  
  private int damage;
  private int level;
  private int rotation;

  private static final Map<WeaponName, WeaponData> REGISTRY = loadRegistry();

  /**
   * Creates a weapon using its specific name and initial level.
   * * @param weaponName the name of the weapon to instantiate.
   * @param level the initial level of the weapon.
   * @throws IllegalArgumentException if level is negative or if the weapon name is not in the registry.
   */
  public Weapon(WeaponName weaponName, int level) {
    if (level < 0) {
      throw new IllegalArgumentException("Le niveau doit être positif");
    }
    this.id = UUID.randomUUID();
    this.weaponName = Objects.requireNonNull(weaponName);
    this.level = level;
    this.rotation = 0;
    
    WeaponData data = REGISTRY.get(weaponName);
    if (data == null) {
      throw new IllegalArgumentException("L'arme " + weaponName + " n'est pas dans le fichier JSON !");
    }
    
    this.weaponType = data.type();
    this.rarity = data.rarity();
    this.isCursed = data.isCursed();
    this.damage = data.damage();
    this.cost = data.cost();
    
    if (data.shape() == null) {
        this.shape = List.of(new Coordinate(0, 0));
    } else {
        this.shape = List.copyOf(data.shape());
    }
  }
  
  /**
   * Gets the type of the weapon (e.g., MELEE, RANGED, MAGIC).
   * @return The weapon type.
   */
  public WeaponType weaponType() {
  	return weaponType;
  }
  
  /**
   * Increases damage if two weapons share the same type or rarity.
   * <p>
   * This mechanism is used to apply adjacency bonuses when weapons are placed next to each other
   * in the backpack.
   * </p>
   * * @param w1 the first weapon (will get the bonus damage).
   * @param w2 the second weapon (provides the context for the bonus).
   */
  public static void buff(Weapon w1, Weapon w2) {
    Objects.requireNonNull(w1);
    Objects.requireNonNull(w2);
    if (w1.weaponType.equals(w2.weaponType)) {
      w1.damage += 5;
    }
    if (w1.rarity.equals(w2.rarity)){
      w1.damage += 5;
    }
  }
  
  private static Map<WeaponName, WeaponData> loadRegistry() {
    ObjectReader reader = new ObjectMapper().reader();
    var file = new File("data/json/weapons.json");

    try(JsonParser parser = reader.createParser(file)){
      List<WeaponData> list = parser.readValueAs(new TypeReference<List<WeaponData>>() {});
      var map = list.stream()
                 .collect(Collectors.toMap(WeaponData::name, Function.identity()));
      return Map.copyOf(map);
    }catch(IOException e){
      throw new IllegalStateException("Impossible de charger data/json/weapons.json", e);
    }
  }
  
  /**
   * Rotates the weapon by 90 degrees clockwise.
   * @return the weapon itself (fluent interface).
   */
  @Override
  public Item rotate() {
    rotation = (rotation + 1) % 4;
    return this; 
  }
	
  /**
   * Increases the level of the weapon by 1.
   */
  public void levelUp(){
    level++;
  }

  @Override
  public List<Coordinate> size() {
    return Coordinate.applyRotation(shape, rotation);
  }

  @Override
  public Rarity rarity() {
    return rarity;
  }

  @Override
  public Cost cost() {
    return cost;
  }

  /**
   * Checks if the weapon is currently cursed.
   * @return true if cursed, false otherwise.
   */
  public boolean isCursed() {
    return isCursed;
  }

  @Override
  public String name() {
    return "" + weaponName;
  }

  @Override
  public UUID id() {
    return id;
  }
  
  @Override
  public int rotation() {
  	return rotation;
  }
  
  /**
   * Gets the current base damage of the weapon.
   * @return the damage value.
   */
  public int damage() {
  	return damage;
  }
  
  
  private static List<WeaponData> findCandidatesByType(WeaponType type) {
    var candidates = REGISTRY.values().stream()
        .filter(data -> data.type() == type)
        .toList();
        
    if (candidates.isEmpty()) {
      throw new IllegalArgumentException("Aucune arme trouvée pour le type : " + type);
    }
    return candidates;
  }

  private static WeaponData selectWeightedWeapon(List<WeaponData> candidates) {
    var weaponsByRarity = candidates.stream()
        .collect(Collectors.groupingBy(WeaponData::rarity));
        
    var selectedRarity = selectRarityFromSet(weaponsByRarity.keySet());
    var pool = weaponsByRarity.get(selectedRarity);
    
    return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
  }

  private static Rarity selectRarityFromSet(Set<Rarity> availableRarities) {
    var totalWeight = availableRarities.stream().mapToInt(Item::getWeight).sum();
    if (totalWeight <= 0) {
    	return availableRarities.iterator().next();
    }
    
    var roll = ThreadLocalRandom.current().nextInt(totalWeight);
    var currentSum = 0;
    for (var r : availableRarities) {
      currentSum += Item.getWeight(r);
      if (roll < currentSum) return r;
    }
    return availableRarities.iterator().next();
  }
	
  /**
   * Creates a random weapon using weighted probabilities for rarity.
   * * @param type the type of weapon (Melee, Ranged, etc.) to generate.
   * @param level the level of the weapon.
   * @return a new random Weapon instance.
   * @throws NullPointerException if type is null.
   * @throws IllegalArgumentException if level is negative.
   */
  public static Weapon createRandom(WeaponType type, int level) {
    Objects.requireNonNull(type);
    if (level < 0) {
    	throw new IllegalArgumentException();
    }
    var candidates = findCandidatesByType(type);
    var selectedData = selectWeightedWeapon(candidates);
    return new Weapon(selectedData.name(), level);
  }
  
  @Override
  public String toString() {
  	return "WEAPON nom :" + weaponName + " level: " + level + " type: " + weaponType + 
  			" rarity: " + rarity + " damage: " + damage + " rotation: " + rotation +
  			" shape: " + size();
  }
  
  @Override
  public Item becomeCursed() {
  	this.isCursed = true;
  	return this;
  }
  
  @Override
  public Item removeCursed() {
  	this.isCursed = false;
  	return this;
  }
  
  
  
  public static void main(String[] args) {
		var w = Weapon.createRandom(WeaponType.MELEE, 1);
		IO.println(w);
	}
  
	
}