package fr.uge.dongeon;

import fr.uge.Coordinate;
import java.util.concurrent.ThreadLocalRandom;

public record SurpriseRoom(Coordinate coordRoom, int levelHero, Room randRoom) implements Room {
	
	/**
   * Constructs a SurpriseRoom by generating a random internal room.
   * The specific type of the inner room is determined randomly among:
   * Treasure, Exit, Battle (Normal), Shop, Heal, or Battle (Boss).
   *
   * @param coordRoom The coordinate of the room.
   * @param levelHero The level of the hero.
   */
  public SurpriseRoom(Coordinate coordRoom, int levelHero) {
    this(coordRoom, levelHero, generateRandomRoom(coordRoom, levelHero));
  }

  private static Room generateRandomRoom(Coordinate coordRoom, int levelHero) {
    var rng = ThreadLocalRandom.current();
    var choice = rng.nextInt(6); 

    return switch (choice) {
      case 0 -> new TreasureRoom(coordRoom, levelHero);
      case 1 -> new ExitRoom(coordRoom, levelHero);
      case 2 -> new BattleRoom(coordRoom, levelHero, false);
      case 3 -> createInitializedShop(coordRoom, levelHero);
      case 4 -> new HealRoom(coordRoom, levelHero);
      case 5 -> new BattleRoom(coordRoom, levelHero, true);
      default -> throw new IllegalStateException("Unexpected value: " + choice);
    };
  }

  private static ShopRoom createInitializedShop(Coordinate coord, int level) {
    var shop = new ShopRoom(coord, level);
    shop.generateMerchantStock();
    return shop;
  }

  @Override
  public String getSymbol() {
    return "S";
  }

  @Override
  public TypeRoom getType() {
    return TypeRoom.SURPRISEROOM;
  }
}