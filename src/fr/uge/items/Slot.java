package fr.uge.items;

import java.util.List;
import java.util.UUID;
import fr.uge.Coordinate;
import fr.uge.items.enums.Rarity;

/**
 * Represents an empty slot in the inventory grid.
 * <p>
 * This class is used primarily for grid management and acts as a placeholder or structural
 * component within the backpack system.
 * </p>
 * @param id The unique identifier for this slot instance.
 */
public record Slot(UUID id) implements Item{
	/**
	 * Creates a new Slot with a random UUID.
	 */
	public Slot() {
		this(UUID.randomUUID());
	}
	
	@Override
	public List<Coordinate> size(){
		return List.of(new Coordinate(0, 0));
	}
	
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
}