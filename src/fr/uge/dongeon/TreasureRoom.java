package fr.uge.dongeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import fr.uge.Coordinate;
import fr.uge.items.*;
import fr.uge.items.enums.*;

/**
 * Represents a room full of treasures.
 * It contains a grid of items that the hero can take.
 */
public final class TreasureRoom implements Room {
	private final Coordinate coordRoom;
	private final ArrayList<ArrayList<Item>> items;
//	private final HashMap<Coordinate, Item> items;
	private static final int WIDTH = 3;
  private static final int HEIGHT = 2;
	private final int levelHero;
	private static final ItemEnum[] ITEM_TYPES = ItemEnum.values();
	
	private static final int ITEM_NAME_PAD_WIDTH = 16;
	private static final String OUTER_H_LINE = "════════════════════";
	private static final String INNER_SEP = "╦";
	private static final String FINAL_SEP = "╩";
  
	/**
	 * Creates a treasure room.
	 * @param coordRoom the position on the map.
	 * @param levelHero the level used to generate items.
	 */
	public TreasureRoom(Coordinate coordRoom, int levelHero) {
		Objects.requireNonNull(coordRoom);
		if(levelHero < 0) {
			throw new IllegalArgumentException();
		}
		this.levelHero = levelHero;
		this.coordRoom = coordRoom;
		items = new ArrayList<>(HEIGHT);
		initializeTreasure();
	}
	
	private boolean isValidCoordinate(Coordinate coord) {
    return coord.x() >= 0 && coord.x() < WIDTH && coord.y() >= 0 && coord.y() < HEIGHT;
	}
  
	/**
	 * Gets an item from the chest at a specific position.
	 * @param coord the coordinate inside the chest.
	 * @return the item found.
	 */
	public Item get(Coordinate coord) {
    Objects.requireNonNull(coord);
    if (!isValidCoordinate(coord)) {
        return null; 
    }
    return items.get(coord.y()).get(coord.x());
	}
  
	/**
	 * Adds an item to the chest manually.
	 * @param item the item to add.
	 * @param coord the position.
	 * @return true if added successfully.
	 */
	public boolean add(Item item, Coordinate coord) {
		Objects.requireNonNull(item);
		Objects.requireNonNull(coord);
		if(!isValidCoordinate(coord) ||!isEmpty(get(coord))){
			return false;
		}
		items.get(coord.y()).set(coord.x(), item);
		return true;
	}
	
	private static boolean isEmpty(Item item) {
		return switch (item) {
			case ItemEmpty _ -> true;
			default -> false;
		};
	}

	/**
	 * Removes an item from the chest.
	 * This happens when the hero takes it.
	 * @param coordinate the position of the item.
	 * @return the item taken.
	 */
	public Item remove(Coordinate coordinate) {
    Objects.requireNonNull(coordinate);
    if (!isValidCoordinate(coordinate)) {
      return null;
    }

    var row = items.get(coordinate.y());
    var item = row.get(coordinate.x());
    if (isEmpty(item)) {
      return item;
    }
    row.set(coordinate.x(), new ItemEmpty());
    return item;
	}
	
	private void initializeTreasure() {
    for (int y = 0; y < HEIGHT; y++) {
      var row = new ArrayList<Item>(WIDTH);
      for (int x = 0; x < WIDTH; x++) {
        var randomItemType = ITEM_TYPES[ThreadLocalRandom.current().nextInt(ITEM_TYPES.length)];
        row.add(Item.getRandom(randomItemType, levelHero));
      }
      items.add(row);
    }
}
	
	/**
	 * Counts how many items are left in the chest.
	 * @return the number of items.
	 */
	public long itemCount() {
    return items.stream()
		            .flatMap(List::stream)
		            .filter(item -> !isEmpty(item))
		            .count();
	}
	
	@Override
	public Coordinate coordRoom() {
		return coordRoom;
	}
	
	@Override
	public String getSymbol() {
		return "T";
	}
	
	@Override
	public String toString() {
		var sb = new StringBuilder();
		buildHeader(sb);
		buildColumnHeaders(sb);
		buildGrid(sb);
		buildFooter(sb);
		return sb.toString();
	}
	
	////////////////////////////////////////////
	private void buildHeader(StringBuilder sb) {
    final int TOTAL_CONTENT_WIDTH = (OUTER_H_LINE.length() * 3) + 4;
    sb.append("╔").append(String.join(INNER_SEP, OUTER_H_LINE, OUTER_H_LINE, OUTER_H_LINE)).append("╗\n");
    sb.append(String.format("║ COFFRE AU TRÉSOR (Niveau: %d) %-" + (TOTAL_CONTENT_WIDTH - 30) + "s ║\n", levelHero, ""));
	}
	
	private void buildColumnHeaders(StringBuilder sb) {
    sb.append("╠").append(String.join("╬", OUTER_H_LINE, OUTER_H_LINE, OUTER_H_LINE)).append("╣\n");
    sb.append(String.format("║ 0 (x) %-" + (OUTER_H_LINE.length() - 6) + "s", "")).append("║");
    sb.append(String.format(" 1 (x) %-" + (OUTER_H_LINE.length() - 6) + "s", "")).append("║");
    sb.append(String.format(" 2 (x) %-" + (OUTER_H_LINE.length() - 6) + "s", "")).append("║\n");
    sb.append("╠").append(String.join("╬", OUTER_H_LINE, OUTER_H_LINE, OUTER_H_LINE)).append("╣\n");
	}
	
	private void buildGrid(StringBuilder sb) {
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        var slot = items.get(y).get(x);
        sb.append("║ ").append(formatSlotContent(slot, y));
      }
      sb.append("║\n");
      if (y < HEIGHT - 1) {
        sb.append("╠").append(String.join("╬", OUTER_H_LINE, OUTER_H_LINE, OUTER_H_LINE)).append("╣\n");
      }
    }
	}
	
	private void buildFooter(StringBuilder sb) {
    sb.append("╚").append(String.join(FINAL_SEP, OUTER_H_LINE, OUTER_H_LINE, OUTER_H_LINE)).append("╝\n");
    sb.append("Items restants : ").append(itemCount()).append("/").append(WIDTH * HEIGHT).append("\n");
    sb.append("Pour prendre un objet, entrez sa coordonnée X Y (ex: 1 0)\n");
	}
	
	private String formatSlotContent(Item slot, int y) {
    int rowNum = y;
    String name = "";

    if (isEmpty(slot)) {
      name = "[EMPTY]";
    } else {
      name = slot.name();
      if (name.length() > ITEM_NAME_PAD_WIDTH) {
        name = name.substring(0, ITEM_NAME_PAD_WIDTH - 3) + "...";
      }
    }
    return String.format("%d | %-" + ITEM_NAME_PAD_WIDTH + "s", rowNum, name);
	}
	
	@Override
	public TypeRoom getType() {
    return TypeRoom.TREASUREROOM;
	}
	

}