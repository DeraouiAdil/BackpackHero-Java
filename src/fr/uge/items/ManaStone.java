package fr.uge.items;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fr.uge.Coordinate;
import fr.uge.items.enums.ItemEnum;
import fr.uge.items.enums.ManaStones;
import fr.uge.items.enums.Rarity;

@JsonIgnoreProperties(ignoreUnknown = true)
record ManaStoneData(
    ManaStones name,
    ItemEnum category,
    Rarity rarity,
    Cost cost,
    List<Coordinate> shape,
    String image,
    String description
) {}

/**
 * Represents a Mana Stone item.
 * Mana stones are used to generate or store mana and usually conduct energy.
 * Data is loaded from a JSON file.
 */
public final class ManaStone implements Item {

  private final UUID id;
  private final ManaStones manaStones;
  private final Cost cost;
  private final List<Coordinate> shape;
  private final Rarity rarity;
  private final String imageName;
  private final String description;
  
  private int rotation; 

  private static final Map<ManaStones, ManaStoneData> REGISTRY = loadRegistry();

  /**
   * Full constructor for internal use.
   */
  public ManaStone(UUID id, ManaStones manaStones, int rotation, Cost cost, List<Coordinate> shape, Rarity rarity, String imageName, String description) {
    this.id = Objects.requireNonNull(id);
    this.manaStones = Objects.requireNonNull(manaStones);
    this.rotation = rotation;
    this.cost = Objects.requireNonNull(cost);
    this.shape = Objects.requireNonNull(shape);
    this.rarity = Objects.requireNonNull(rarity);
    this.imageName = imageName;
    this.description = description;
  }

  /**
   * Creates a ManaStone from its type enum.
   * @param manaStones the type of stone (e.g., MANASTONE, SMALLMANASTONE).
   */
  public ManaStone(ManaStones manaStones) {
    this(manaStones, 0);
  }

  public ManaStone(ManaStones manaStones, int rotation) {
    this(manaStones, rotation, getData(manaStones));
  }

  private ManaStone(ManaStones manaStones, int rotation, ManaStoneData data) {
    this(
      UUID.randomUUID(),
      manaStones,
      rotation,
      data.cost(),
      (data.shape() == null || data.shape().isEmpty()) ? List.of(new Coordinate(0, 0)) : List.copyOf(data.shape()),
      data.rarity(),
      (data.image() != null) ? data.image().replace(".png", "").toUpperCase() : manaStones.toString(),
      data.description()
    );
  }

  private static Map<ManaStones, ManaStoneData> loadRegistry() {
    ObjectReader reader = new ObjectMapper().reader();
    var file = new File("data/json/manastones.json");

    try (JsonParser parser = reader.createParser(file)) {
      List<ManaStoneData> list = parser.readValueAs(new TypeReference<List<ManaStoneData>>() {});
      return list.stream()
              .collect(Collectors.toMap(ManaStoneData::name, Function.identity()));
    } catch (IOException e) {
      throw new IllegalStateException("Impossible de charger data/json/manastones.json", e);
    }
  }

  private static ManaStoneData getData(ManaStones name) {
    Objects.requireNonNull(name);
    ManaStoneData data = REGISTRY.get(name);
    if (data == null) {
      throw new IllegalArgumentException("La ManaStone " + name + " n'existe pas dans le fichier JSON.");
    }
    return data;
  }

  @Override
  public Item rotate() {
    this.rotation = (rotation + 1) % 4;
    return this;
  }

  @Override
  public List<Coordinate> size() {
    return Coordinate.applyRotation(shape, rotation);
  }

  @Override
  public String name() {
    return "" + manaStones;
  }
  
  @Override
  public UUID id() { return id; }
  
  @Override
  public Rarity rarity() { return rarity; }
  
  @Override
  public Cost cost() { return cost; }
  
  @Override
  public int rotation() { return rotation; }
  
  public String imageName() { return imageName; }
  
  public String description() { return description; }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append(manaStones).append("\n")
      .append("ID : ").append(id.toString().substring(0, 8)).append("...\n")
      .append("Rarity : ").append(rarity).append("\n")
      .append("Rotation : ").append(rotation).append("\n");
    return sb.toString(); 
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ManaStone that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}