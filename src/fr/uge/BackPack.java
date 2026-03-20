package fr.uge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import fr.uge.items.Armor;
import fr.uge.items.Cost;
import fr.uge.items.Gold;
import fr.uge.items.Item;
import fr.uge.items.ItemEmpty;
import fr.uge.items.Slot;
import fr.uge.items.Weapon;

/**
 * Represents the hero's inventory (Backpack).
 * <p>
 * The backpack is a grid-based storage system. It handles:
 * </p> * <ul>
 * <li>Placement of items with complex shapes (tetromino-like).</li>
 * <li>Adjacency effects (e.g., weapons buffing each other).</li>
 * <li>Management of gold (merging stacks).</li>
 * <li>Cursed items (which cannot be moved).</li>
 * <li>Grid expansion (leveling up).</li>
 * </ul>
 * */
public class BackPack {
  private static final int DEFAULT_WIDTH = 3;
  private static final int DEFAULT_HEIGHT = 3;
  private final HashMap<Coordinate, Item> items;
  private int spaces;
  
  /**
   * Creates a backpack with a specified number of expansion spaces available.
   * @param spaces The number of slots that can be added to the grid later.
   * @throws IllegalArgumentException if spaces is negative.
   */
  public BackPack(int spaces) {
    if(spaces < 0) throw new IllegalArgumentException();
    items = new HashMap<>();
    this.spaces = spaces;
    initBackPack();
  }
  
  /**
   * Creates a default backpack with 2 expansion spaces.
   */
  public BackPack() {
    this(2);
  }

  
  /**
   * Adds gold to the backpack.
   * <p>
   * It first attempts to merge the amount with any existing gold stacks.
   * If no stacks exist or merge is impossible, it places a new pile in the first empty slot.
   * </p>
   * @param amount The amount of gold to add.
   * @return true if successful, false if the bag is full and no merge was possible.
   */
  public boolean addGold(int amount) {
  	if (amount <= 0) {
  		return false;
  	}
  	var newGold = new Gold(amount);
  	if(findAndMergeGold(newGold)) {
  		return true;
  	}
  	for(var entry : items.entrySet()) {
  		if(isSlotEmpty(entry.getValue())){
  			items.put(entry.getKey(), newGold);
  			return true;
  		}
  	}
  	return false;
  }
  
  
  /**
   * Initializes the default 3x3 grid with empty items.
   */
  private void initBackPack() {
    for (int y = 0; y < DEFAULT_HEIGHT; y++) {
      for (int x = 0; x < DEFAULT_WIDTH; x++) {
        items.put(new Coordinate(x, y), new ItemEmpty());
      }
    }
  }
  
  
  /**
   * Finds all grid coordinates occupied by a specific item instance.
   * (Essential for items that take up multiple grid cells).
   */
  private List<Coordinate> getItemCoordinates(Item item) {
    return items.entrySet().stream()
      .filter(entry -> entry.getValue().equals(item))
      .map(Map.Entry::getKey)
      .toList();
  }

  
  /**
   * Returns the list of coordinates for the item at the clicked position.
   * If the slot is empty or invalid, returns just the clicked coordinate.
   *
   * @param click The coordinate clicked.
   * @return A list of coordinates occupied by the item at that location.
   */
  public List<Coordinate> getCoordinates(Coordinate click) {
    Objects.requireNonNull(click);
    var item = items.get(click);
    if (item == null || isSlotEmpty(item)) {
      return List.of(click);
    }
    return getItemCoordinates(item);
  }

  /**
   * Returns a stream of valid adjacent coordinates (Up, Down, Left, Right).
   */
  private Stream<Coordinate> getAdjacentStream(Coordinate c) {
    return Stream.of(
      new Coordinate(c.x() + 1, c.y()), new Coordinate(c.x() - 1, c.y()),
      new Coordinate(c.x(), c.y() + 1), new Coordinate(c.x(), c.y() - 1)
    ).filter(items::containsKey);
  }
  
  /**
   * Checks if a neighboring item is valid for interaction.
   * It ignores the item itself (self-reference), empty slots, and UI slots.
   */
  private boolean isValidNeighbor(Item neighbor, Item source) {
    return neighbor != null 
        && !neighbor.equals(source) 
        && !isSlotEmpty(neighbor) 
        && !isSlot(neighbor);
  }
  
  
  /**
   * Gets distinct neighboring items around a specific item.
   * Handles multi-cell items correctly (prevents counting the same neighbor twice if it borders multiple cells).
   */
  private List<Item> getNeighbors(Item item) {
    Objects.requireNonNull(item);
    return getItemCoordinates(item).stream()
      .flatMap(this::getAdjacentStream)
      .map(items::get)
      .filter(neighbor -> isValidNeighbor(neighbor, item))
      .distinct()
      .toList();
  }

  /**
   * Transforms the item at the given coordinate into its cursed version.
   * @param c The coordinate of the item.
   */
  public void becomeCursed(Coordinate c) {
  	Objects.requireNonNull(c);
  	var item = get(c);
  	
  	if(item == null || isSlot(item) || isSlotEmpty(item)) {
  		return;
  	}
  	if(item.isCursed()) {
  		return;
  	}
  	var cursedVersion = item.becomeCursed();
  	var coord = getItemCoordinates(item);
  	coord.forEach(coo -> items.put(coo, cursedVersion));
  }
  
  /**
   * Curses a random valid item in the backpack.
   * @return true if an item was cursed, false if no valid targets were found.
   */
  public boolean curseRandomItem() {
  	var Candidatesitem = items.entrySet().stream()
  			.filter(e -> !isSlotEmpty(e.getValue()))
  			.filter(e -> !isSlot(e.getValue()))
  			.filter(e -> !e.getValue().isCursed()).filter(e -> switch(e.getValue()) {
  			case Weapon _ -> true;
  			case Armor  _ -> true;
  			default -> false;
  			}).map(p -> p.getKey()).toList();
  	if(Candidatesitem.isEmpty()) {
  		return false;
  	}
  	var rdm = new Random();
  	var randomCoord = Candidatesitem.get(rdm.nextInt(Candidatesitem.size()));
  	
  	becomeCursed(randomCoord);
  	return true;
  }
  
  /**
   * Removes a curse from a random cursed item (purification event).
   * @return true if an item was cleansed, false if no cursed items existed.
   */
  public boolean removeCurseRandomly() {
  	var cursedItems = items.entrySet().stream().filter(e -> e.getValue().isCursed()).map(p -> p.getKey()).toList();
  	if(cursedItems.isEmpty()) {
  		return false;
  	}
  	var coord = cursedItems.get(new Random().nextInt(cursedItems.size()));
  	var item  = items.get(coord);
  	item.removeCursed();
  	return true;
  }

  /**
   * Triggers the application of passive effects for all items.
   * <p>
   * Example: An armor might gain extra protection if placed next to other armor or shields.
   * Weapons might gain damage if adjacent to same-type weapons.
   * </p>
   */
  public void applyEffect() {
    items.values().stream().distinct().forEach(this::applyItemEffect);
  }

  private void applyItemEffect(Item item) {
    var neighbors = getNeighbors(item);
    switch (item) {
      case Weapon w -> buffWeapons(w, neighbors);
      case Armor a -> buffArmors(a, neighbors);
      default -> {}
    }
  }

  private void buffWeapons(Weapon source, List<Item> neighbors) {
    neighbors.forEach(n -> {
      switch (n) {
        case Weapon target -> Weapon.buff(source, target);
        default -> {}
      }
    });
  }

  private void buffArmors(Armor source, List<Item> neighbors) {
    neighbors.forEach(n -> {
      switch (n) {
        case Armor target -> Armor.buff(source, target);
        default -> {}
      }
    });
  }

  /**
   * Removes an item from the backpack at the given coordinate.
   * <p>
   * <strong>Note:</strong> Cursed items cannot be moved or removed.
   * </p>
   * @param c The coordinate of the item to take.
   * @return The removed item, or null if the slot was empty/invalid or the item was cursed.
   */
  public Item take(Coordinate c) {
    Objects.requireNonNull(c);
    var item = get(c);
    if (item == null || isSlotEmpty(item) || isSlot(item)) return null;
    
    getItemCoordinates(item).forEach(coord -> items.put(coord, new ItemEmpty()));
    return item;
  }
  
  /**
   * Attempts to add an item to the backpack at the specified origin.
   * <p>
   * Checks for space availability, boundary collisions, and handles gold merging automatically.
   * </p>
   * @param item  The item to add.
   * @param click The top-left coordinate where the user attempts to place the item.
   * @return true if added successfully, false otherwise (e.g., overlaps or out of bounds).
   */
  public boolean add(Item item, Coordinate click) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(click);
    
    if (tryMergeGold(item)) return true;

    var targetCells = calculateTargetCells(item, click);
    
    if (!canPlaceItem(targetCells)) return false;

    targetCells.forEach(cell -> items.put(cell, item));
    return true;
  }
  

  private boolean tryMergeGold(Item item) {
    return switch (item) {
      case Gold newGold -> findAndMergeGold(newGold);
      default -> false;
    };
  }

  private boolean findAndMergeGold(Gold newGold) {
    for (var entry : items.entrySet()) {
      switch (entry.getValue()) {
        case Gold existing -> {
          items.put(entry.getKey(), existing.add(newGold));
          return true;
        }
        default -> {}
      }
    }
    return false;
  }

  private List<Coordinate> calculateTargetCells(Item item, Coordinate origin) {
    return item.size().stream()
      .map(c -> new Coordinate(c.x() + origin.x(), c.y() + origin.y()))
      .toList();
  }

  private boolean canPlaceItem(List<Coordinate> cells) {
    return cells.stream().allMatch(items::containsKey) && 
           cells.stream().map(items::get).allMatch(BackPack::isSlotEmpty);
  }

  /**
   * Attempts to rotate an item at the given coordinate.
   * <p>
   * Logic: Temporarily remove item -> Rotate -> Try to add back -> If fail (collision), revert to original state.
   * Cursed items cannot be rotated.
   * </p>
   * @param click The coordinate of the item.
   * @return true if rotation was successful.
   */
  public boolean rotate(Coordinate click) {
    Objects.requireNonNull(click);
    var item = items.get(click);
    if (item == null || isSlotEmpty(item) || isSlot(item)) return false;

    if(item.isCursed()) {
    	return false;
    }

    var coords = getItemCoordinates(item);
    var origin = getTopLeft(coords);
    
    coords.forEach(c -> items.put(c, new ItemEmpty()));
    
    if (!add(item.rotate(), origin)) {
      coords.forEach(c -> items.put(c, item));
      return false;
    }
    return true;
  }

  /**
   * Adds a specific "Curse" item (debuff object) to the backpack.
   * <p>
   * Unlike normal items, curses are aggressive: they might overwrite existing items 
   * (except structural slots) if they land on them.
   * </p>
   * @param curse The curse item to add.
   * @param click The target coordinate.
   * @return true if the curse was applied.
   */
  public boolean addCurse(Item curse, Coordinate click) {
  	Objects.requireNonNull(curse);
  	Objects.requireNonNull(click);
  	
  	var targetCells = calculateTargetCells(curse, click);
  	// Check boundaries
  	if(!targetCells.stream().allMatch(items::containsKey)) {
  		return false;
  	}
  	
  	// Remove any items in the way (unless they are system slots)
  	for(var cell : targetCells) {
  		var existingItem = items.get(cell);
  		
  		if(!isSlot(existingItem)) {
  			take(cell);
  		}
  	}
  	targetCells.forEach(cell -> items.put(cell, curse));
  	return true;
  }
  

  private Coordinate getTopLeft(List<Coordinate> coords) {
    int minX = coords.stream().mapToInt(Coordinate::x).min().orElse(0);
    int minY = coords.stream().mapToInt(Coordinate::y).min().orElse(0);
    return new Coordinate(minX, minY);
  }

  /**
   * Spends gold from the backpack.
   * @param amount The amount to spend.
   * @return true if successful, false if not enough gold available.
   */
  public boolean useGold(long amount) {
    if (amount < 0) return false;
    var goldEntry = items.entrySet().stream()
        .filter(e -> isGold(e.getValue()))
        .findFirst();

    if (goldEntry.isEmpty()) return false;
    return updateGoldAmount(goldEntry.get(), amount);
  }

  private boolean updateGoldAmount(Map.Entry<Coordinate, Item> entry, long cost) {
    return switch (entry.getValue()) {
        case Gold g when g.amount() >= cost -> {
            long remaining = g.amount() - cost;
            // If gold reaches 0, replace with EmptyItem
            items.put(entry.getKey(), remaining > 0 ? new Gold(remaining) : new ItemEmpty());
            yield true;
        }
        default -> false;
    };
  }

  /**
   * Calculates total gold in the backpack.
   * @return Total gold amount.
   */
  public long getGold() {
    return items.values().stream()
      .mapToLong(item -> switch(item) {
         case Gold g -> g.amount();
         default -> 0;
      })
      .sum();
  }

  /**
   * Expands the backpack grid by unlocking a new slot at the specified coordinate.
   * The new slot must be adjacent to an existing slot.
   *
   * @param temp A temporary backpack reference (unused in logic but kept for signature consistency).
   * @param coordinate The coordinate to unlock.
   * @return true if the slot was successfully added.
   */
  public boolean addSlot(BackPack temp, Coordinate coordinate) {
    Objects.requireNonNull(temp);
    Objects.requireNonNull(coordinate);

    if (items.containsKey(coordinate) || spaces <= 0) {
    	return false;
    }
    
    boolean existWall = Stream.of(
    		new Coordinate(coordinate.x() + 1, coordinate.y()),
    		new Coordinate(coordinate.x() -1, coordinate.y()),
    		new Coordinate(coordinate.x(), coordinate.y() + 1),
    		new Coordinate(coordinate.x(), coordinate.y() - 1)
    		).anyMatch(items::containsKey);
    		
    if(!existWall) {
    	return false;
    }

    
    items.put(coordinate, new ItemEmpty());
    spaces--;
    return true;
  }
  
  
  /**
   * Returns a list of coordinates where a new slot can be added.
   * (Coordinates adjacent to the current grid boundaries).
   * @return List of valid expansion coordinates.
   */
  public List<Coordinate> getPossibleExpansion() {
    var candidates = new HashSet<Coordinate>();
    for (var current : items.keySet()) {
        Stream.of(
            new Coordinate(current.x() + 1, current.y()), 
            new Coordinate(current.x() - 1, current.y()),
            new Coordinate(current.x(), current.y() + 1), 
            new Coordinate(current.x(), current.y() - 1)

        ).filter(c -> !items.containsKey(c)).forEach(candidates::add);
    }
    return List.copyOf(candidates);
  }

  
  /**
   * Calculates the total value of items in the backpack.
   * Primarily used for scoring purposes.
   * @return The sum of the buying price of all items.
   */
  public long price() {//pour le score
    return items.values().stream()
      .map(Item::buyingPrice)
      .mapToLong(Cost::amount)
      .sum();
  }
  
  /**
   * Increases the number of available spaces for expansion by 1.
   */
  public void spaceUp() { spaces++; }
  
  /**
   * Gets the number of available spaces for expansion.
   * * @return The count of expansion slots available.
   */
  public int getSpace() { return spaces; }
  
  /**
   * Retrieves the item located at the specified coordinate.
   * * @param c The coordinate to look up.
   * @return The item at this location, or null if coordinate is invalid (though HashMap returns null only if key missing).
   */
  public Item get(Coordinate c) {
    Objects.requireNonNull(c);
    return items.get(c);
  }
  
  /**
   * Returns an unmodifiable view of the items in the backpack.
   * * @return A map of coordinates to items.
   */
  public Map<Coordinate, Item> items() { return Map.copyOf(items); }

  private static boolean isGold(Item item) {
    return switch (item) { case Gold _ -> true; default -> false; };
  }

  private static boolean isSlot(Item item) {
    return switch (item) { case Slot _ -> true; default -> false; };
  }
  
  private static boolean isSlotEmpty(Item item) {
    return switch (item) { case ItemEmpty _ -> true; default -> false; };
  }
  
  /**
   * Checks if the backpack contains any item that can be cursed.
   * Valid targets are Weapons or Armors that are not yet cursed.
   * * @return true if a curseable item exists.
   */
  public boolean hasCurseableItem() {
  	return items.values().stream()
  			.anyMatch( items -> switch(items) {
  			case Weapon w -> !w.isCursed();
  			case Armor a -> !a.isCursed();
  			default -> false;
  			});
  }
  
  private static String itemSymbol(Item item) {
    return switch (item) {
      case ItemEmpty _ -> ".";
      case Slot _      -> "?";
      case Weapon _    -> "W";
      case Gold _      -> "G";
      default          -> "X";
    };
  }

  @Override
  public String toString() {
    if (items.isEmpty()) return "(BackPack vide)";
    int minX = items.keySet().stream().mapToInt(Coordinate::x).min().orElse(0);
    int maxX = items.keySet().stream().mapToInt(Coordinate::x).max().orElse(0);
    int minY = items.keySet().stream().mapToInt(Coordinate::y).min().orElse(0);
    int maxY = items.keySet().stream().mapToInt(Coordinate::y).max().orElse(0);

    var sb = new StringBuilder("    ");
    for (int x = minX; x <= maxX; x++) sb.append(String.format("%-5d", x));
    sb.append("\n  +").append("-".repeat((maxX - minX + 1) * 5)).append("+\n");

    for (int y = minY; y <= maxY; y++) {
      sb.append(String.format("%-2d|", y));
      for (int x = minX; x <= maxX; x++) {
        var item = get(new Coordinate(x, y));
        sb.append(String.format(" %-3s ", item != null ? itemSymbol(item) : ""));
      }
      sb.append("|\n");
    }
    return sb.toString();
  }
}