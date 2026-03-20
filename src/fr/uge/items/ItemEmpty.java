package fr.uge.items;

import java.util.List;
import java.util.UUID;
import fr.uge.Coordinate;
import fr.uge.items.enums.Rarity;

/**
 * Represents an empty space in a grid that acts as an item placeholder.
 * It has no cost, no rarity, and a size of 1x1.
 * Useful for logic that requires an Item object even when a slot is technically "empty".
 * @param id Unique identifier.
 */
public record ItemEmpty(UUID id) implements Item{
	public ItemEmpty() {
		this(UUID.randomUUID());
	}
	
	@Override
	public List<Coordinate> size(){
		return List.of(new Coordinate(0, 0));
	}
	
	/**
	 * Rotates the item (has no effect on an empty item).
	 * @return this instance.
	 */
	@Override
	public Item rotate() {
		return this;
	}

	@Override
	public Rarity rarity() {
		return Rarity.NONE;
	}

	@Override
	public Cost cost() {
		return new Cost();
	}

  @Override
  public int rotation() {
  	return 0;
  }
	
	@Override
	public String name() {
		return "";
	}
	
	@Override
	public String toString() {
		return "ItemEmpty";
	}
}