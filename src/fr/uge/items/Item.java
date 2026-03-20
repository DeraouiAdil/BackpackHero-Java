package fr.uge.items;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import fr.uge.Coordinate;
import fr.uge.items.enums.ArmorType;
import fr.uge.items.enums.ItemEnum;
import fr.uge.items.enums.ManaStones;
import fr.uge.items.enums.Rarity;
import fr.uge.items.enums.RelicEnum;
import fr.uge.items.enums.TypeCost;
import fr.uge.items.enums.WeaponType;


/**
 * Sealed interface defining the common behavior of all items in the game.
 * Items can be Weapons, Armors, Gold, Relics, ManaStones, or even Empty Slots.
 * Each item has properties like size (shape), rarity, cost, and rotation.
 */
public sealed interface Item permits Armor, Weapon, Gold, Slot, ItemEmpty, Relic, ManaStone {
  
  /**
   * Returns the list of coordinates (shape) the item occupies in the inventory grid.
   * This shape changes based on the item's rotation.
   * @return a list of coordinates relative to the item's anchor (0,0).
   */
  List<Coordinate> size();

  /**
   * Returns the rarity of the item (Common, Uncommon, Rare, Legendary, Relic).
   * @return the item's rarity.
   */
  Rarity rarity();

  /**
   * Returns the resource cost to use or equip the item.
   * This can be Energy, Mana, or Gold.
   * @return the cost object.
   */
  Cost cost();

  /**
   * Rotates the item 90 degrees clockwise.
   * @return the item itself (if mutable) or a new rotated instance (if immutable).
   */
  Item rotate();

  /**
   * Returns the name of the item.
   * @return the name as a String.
   */
  String name();

  /**
   * Returns the unique identifier of the item instance.
   * Useful for distinguishing identical items.
   * @return the UUID.
   */
  UUID id();

  /**
   * Returns the current rotation state index (0, 1, 2, or 3).
   * 0 = 0°, 1 = 90°, 2 = 180°, 3 = 270°.
   * @return the rotation index.
   */
  int rotation();
  
  /**
   * Utility method to pick a random value from an array of enum constants.
   * @param <T> the type of the enum.
   * @param values the array of enum values.
   * @return a random value from the array.
   */
  private static <T> T randomEnum(T[] values) {
  return values[ThreadLocalRandom.current().nextInt(values.length)];
  }

  /**
   * Returns the weighted probability for a given rarity to appear in loot generation.
   * Higher weight means higher probability.
   * @param rarity the rarity to check.
   * @return the weight value.
   */
  public static int getWeight(Rarity rarity) {
  	Objects.requireNonNull(rarity);
		return switch (rarity) {
			case COMMON -> 50;
			case UNCOMMON -> 30;
			case RARE -> 15;
			case LEGENDARY -> 4;
			case RELIC -> 1;
			default -> 0; 
		};
	}
  
  /**
   * Factory method to create a random item based on a category and level.
   * Handles creation logic for different item types (ManaStones, Relics, Armors, Weapons).
   * @param ie the category of item to create (e.g., HELMET, SWORD).
   * @param level the level of the item to generate.
   * @return a new random Item instance.
   * @throws IllegalArgumentException if level is negative or item type is unhandled.
   */
  public static Item getRandom(ItemEnum ie, int level) {
    Objects.requireNonNull(ie);
    if (level < 0) {
      throw new IllegalArgumentException("Niveau invalide");
    }
    
    return switch (ie) {
      case MANASTONE -> new ManaStone(randomEnum(ManaStones.values()));
      case RELIC -> new Relic(randomEnum(RelicEnum.values()));
      
      case CLOTHING -> Armor.createRandom(ArmorType.CLOTHINGARMOR, level);
      case GLOVE -> Armor.createRandom(ArmorType.GLOVE, level);
      case HELMET -> Armor.createRandom(ArmorType.HELMETARMOR, level);
      case FOOTWEAR -> Armor.createRandom(ArmorType.FOOTARMOR, level);
      case SHIELD -> Armor.createRandom(ArmorType.SHIELD, level);
      
      case MAGICWEAPON -> Weapon.createRandom(WeaponType.MAGIC, level);
      case MELEEWEAPON -> Weapon.createRandom(WeaponType.MELEE, level);
      case RANGEDWEAPON, BULLETBOW -> Weapon.createRandom(WeaponType.RANGED, level);
      
      default -> throw new IllegalArgumentException("Type non géré : " + ie);
    };
  }
  
  /**
   * Checks if the item currently has a curse.
   * @return true if cursed, false otherwise (default false).
   */
  default boolean isCursed() {
  	return false;
  }
  
  /**
   * Calculates the buying price of an item in a shop based on its rarity.
   * @param item the item to value.
   * @return the cost to buy the item.
   */
  public static Cost buyingPrice(Item item) {
    Objects.requireNonNull(item);
    return switch (item.rarity()) {
      case COMMON -> new Cost(TypeCost.GOLD, 6);
      case UNCOMMON -> new Cost(TypeCost.GOLD, 12);
      case RARE -> new Cost(TypeCost.GOLD, 20);
      case LEGENDARY -> new Cost(TypeCost.GOLD, 35);
      case RELIC -> new Cost(TypeCost.GOLD, 35);
      default -> new Cost(TypeCost.GOLD, 0);
    };
  }
  
  /**
   * Calculates the selling price of an item (how much the merchant pays you).
   * Usually lower than the buying price.
   * @param item the item to sell.
   * @return the value in Gold.
   */
  public static Cost sellingPrice(Item item) {
    Objects.requireNonNull(item);
    return switch (item.rarity()) {
      case COMMON -> new Cost(TypeCost.GOLD, 3);
      case UNCOMMON -> new Cost(TypeCost.GOLD, 6);
      case RARE -> new Cost(TypeCost.GOLD, 10);
      case LEGENDARY -> new Cost(TypeCost.GOLD, 17);
      default -> new Cost(TypeCost.GOLD, 0);
    };
  }
  
  /**
   * Applies a curse to the item.
   * @return the item itself (modified) or a new cursed instance.
   */
  default Item becomeCursed() {
  	return this;
  }
  
  /**
   * Removes the curse from the item.
   * @return the item itself (modified) or a new uncursed instance.
   */
  default Item removeCursed() {
  	return this;
  }
  
  /**
   * Unwraps the item if it's decorated or wrapped (e.g., CursedItem wrapper).
   * By default, returns the item itself.
   * @return the base item.
   */
  default Item unwrap() {
  	return this;
  }

}