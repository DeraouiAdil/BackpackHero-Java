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
import fr.uge.items.enums.Rarity;
import fr.uge.items.enums.RelicEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
record RelicData(
    RelicEnum name,
    Cost cost,
    List<Coordinate> shape,
    String description
) {}

/**
 * Represents a Relic item.
 * Relics are powerful, unique items usually obtained from bosses.
 * Their properties are immutable and loaded from a JSON file.
 * @param id Unique identifier.
 * @param relicName The type name of the relic.
 * @param rotation Current rotation.
 * @param cost Resource cost to use.
 * @param shape Grid shape.
 * @param description Description text.
 */
public record Relic(UUID id, RelicEnum relicName, int rotation, Cost cost, List<Coordinate> shape, String description) implements Item {
  private static final Map<RelicEnum, RelicData> REGISTRY = loadRegistry();
	
  public Relic {
	  Objects.requireNonNull(id);
	  Objects.requireNonNull(relicName);
	  Objects.requireNonNull(cost);
	  Objects.requireNonNull(shape);
	  Objects.requireNonNull(description);
	}

  private static Map<RelicEnum, RelicData> loadRegistry() {
    ObjectReader reader = new ObjectMapper().reader();
    var file = new File("data/json/relics.json");

    try (JsonParser parser = reader.createParser(file)) {
      List<RelicData> list = parser.readValueAs(new TypeReference<List<RelicData>>() {});
      return list.stream()
        				.collect(Collectors.toMap(RelicData::name, Function.identity()));
    } catch (IOException e) {
      throw new IllegalStateException("Impossible de charger data/json/relics.json", e);
    }
  }

  /**
   * Creates a new Relic based on its enum type.
   * Data is fetched from the loaded registry.
   * @param type the type of the relic.
   */
  public Relic(RelicEnum type) {
    this(type, 0);
  }

  private Relic(RelicEnum type, int rotation) {
    this(
      UUID.randomUUID(),
      type,
      rotation,
      getData(type).cost(),
      (getData(type).shape() == null || getData(type).shape().isEmpty()) 
          ? List.of(new Coordinate(0, 0)) 
          : List.copyOf(getData(type).shape()),
      getData(type).description()
    );
  }

  private static RelicData getData(RelicEnum type) {
  	Objects.requireNonNull(type);
    RelicData data = REGISTRY.get(type);
    return data;
  }

  @Override
  public String name() {
    return "" + relicName;
  }

  /**
   * Rotates the relic by 90 degrees.
   * Returns a new instance since Record is immutable.
   * @return a new Relic with updated rotation.
   */
  @Override
  public Item rotate() {
    return new Relic(id, relicName, (rotation + 1) % 4, cost, shape, description);
  }

  @Override
  public List<Coordinate> size() {
    return Coordinate.applyRotation(shape, rotation);
  }

  @Override
  public Rarity rarity() {
    return Rarity.LEGENDARY;
  }

  @Override
  public String toString() {
    return "Relic[" + relicName + ", rot=" + rotation + "]";
  }
}