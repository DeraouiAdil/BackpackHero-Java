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
import fr.uge.Coordinate;
import fr.uge.items.enums.Rarity;
import fr.uge.items.enums.ArmorName;
import fr.uge.items.enums.ArmorType;

@JsonIgnoreProperties(ignoreUnknown = true)
record ArmorData(
    ArmorName name,
    @JsonProperty("category") ArmorType type,
    Rarity rarity,
    int protection,
    Cost cost,
    List<Coordinate> shape
) {}

/**
 * Represents an Armor item that provides protection to the hero.
 * Armor statistics (shape, protection value, cost) are loaded from a JSON registry.
 */
public final class Armor implements Item {
  private final UUID id;
  private final ArmorName armorName;
  private final ArmorType armorType;
  private final Rarity rarity;
  private final List<Coordinate> shape;
  private final Cost cost;
  
  private boolean isCursed;
  private int protection;
  private int level;
  private int rotation;

  private static final Map<ArmorName, ArmorData> REGISTRY = loadRegistry();

  /**
   * Creates an Armor instance.
   * @param armorName the specific name of the armor (key in JSON).
   * @param level the item level.
   * @throws IllegalArgumentException if level is negative or armorName is not found in registry.
   */
  public Armor(ArmorName armorName, int level) {
    if (level < 0) {
      throw new IllegalArgumentException();
    }
    id = UUID.randomUUID();
    this.armorName = Objects.requireNonNull(armorName);
    this.level = level;
    this.isCursed = false;
    rotation = 0;

    var data = REGISTRY.get(armorName);
    if(data == null){
      throw new IllegalArgumentException();
    }

    armorType = data.type();
    rarity = data.rarity();
    protection = data.protection();
    cost = data.cost();
    Objects.requireNonNull(data.shape());
    shape = List.copyOf(data.shape());
    
  }

  /**
   * Gets the type of the armor (Helmet, Glove, etc.).
   * @return the ArmorType.
   */
  public ArmorType armorType() {
  	return armorType;
  }

  /**
   * Applies adjacency bonuses to armor.
   * If two adjacent armors share the same Type or Rarity, the first armor gains +5 protection.
   * @param a1 the armor receiving the buff.
   * @param a2 the adjacent armor providing the buff.
   */
  public static void buff(Armor a1, Armor a2) {
    Objects.requireNonNull(a1);
    Objects.requireNonNull(a2);
    if(a1.armorType.equals(a2.armorType)){
      a1.protection += 5;
    }
    if(a1.rarity.equals(a2.rarity)){
      a1.protection += 5;
    }
  }
  
  /**
   * Loads the armor registry from the JSON file.
   * @return a map of ArmorName to ArmorData.
   */
  private static Map<ArmorName, ArmorData> loadRegistry() {
    var reader = new ObjectMapper().reader();
    var file = new File("data/json/armors.json");

    try (JsonParser parser = reader.createParser(file)) {
      List<ArmorData> list = parser.readValueAs(new TypeReference<List<ArmorData>>() {});
      var map = list.stream()
              .collect(Collectors.toMap(ArmorData::name, Function.identity()));
      return Map.copyOf(map);
    } catch (IOException e) {
      throw new IllegalStateException("Impossible de charger data/json/armors.json", e);
    }
  }

  @Override
  public Item rotate() {
    rotation = (rotation + 1) % 4;
    return this;
  }

  /**
   * Increases the item level by 1.
   */
  public void levelUp() {
    level++;
  }
  
  @Override
  public boolean isCursed() {
      return isCursed;
  }
  
  /**
   * Returns the protection value of the armor.
   * @return protection amount.
   */
  public int getProtection() {
    return protection;
	}
	
	@Override
	public List<Coordinate> size() {
    return Coordinate.applyRotation(shape, rotation);
	}
	
	@Override
	public Rarity rarity() {
    return rarity;
	}
	
	public ArmorType getArmorType() {
		return armorType;
	}
	
	@Override
	public Cost cost() {
    return cost;
	}
	
	@Override
	public String name() {
    return "" + armorName;
	}
	
	@Override
	public UUID id() {
    return id;
	}
	
  @Override
  public int rotation() {
  	return rotation;
  }
	
  private static List<ArmorData> findCandidatesByType(ArmorType type) {
    var candidates = REGISTRY.values().stream()
        .filter(data -> data.type() == type)
        .toList();

    if (candidates.isEmpty()) {
      throw new IllegalArgumentException("Aucune armure trouvée pour le type : " + type);
    }
    return candidates;
  }

  private static ArmorData selectWeightedArmor(List<ArmorData> candidates) {
    var armorsByRarity = candidates.stream()
        .collect(Collectors.groupingBy(ArmorData::rarity));

    var selectedRarity = selectRarityFromSet(armorsByRarity.keySet());
    var pool = armorsByRarity.get(selectedRarity);

    return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
  }

  private static Rarity selectRarityFromSet(Set<Rarity> availableRarities) {
    var totalWeight = availableRarities.stream().mapToInt(Item::getWeight).sum();
    // Sécurité au cas où totalWeight <= 0
    if (totalWeight <= 0) return availableRarities.iterator().next();

    var roll = ThreadLocalRandom.current().nextInt(totalWeight);
    var currentSum = 0;

    for (var r : availableRarities) {
      currentSum += Item.getWeight(r);
      if (roll < currentSum) return r;
    }
    return availableRarities.iterator().next();
  }
  
  /**
   * Factory method to create a random armor of a specific type.
   * Selects an armor based on rarity weights.
   * @param type the type of armor (e.g., GLOVE).
   * @param level the level of the armor.
   * @return a new random Armor instance.
   */
  public static Armor createRandom(ArmorType type, int level) {
    Objects.requireNonNull(type);
    if (level < 0) {
      throw new IllegalArgumentException("Niveau invalide");
    }
    var candidates = findCandidatesByType(type);
    var selectedData = selectWeightedArmor(candidates);

    return new Armor(selectedData.name(), level);
  }
	
	@Override
	public String toString() {
    return "ARMURE = nom :" + armorName + " level: " + level + " type: " + armorType +
            " rarity: " + rarity + " protection: " + protection + 
            " rotation: " + rotation + " shape: " + size();
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
		var a = Armor.createRandom(ArmorType.GLOVE, 1);
		IO.println(a);
  }
}