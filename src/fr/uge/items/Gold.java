package fr.uge.items;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import fr.uge.Coordinate;
import fr.uge.items.enums.Rarity;

/**
 * Represents Gold (currency) in the game.
 * Gold items take up a 1x1 space but usually stack conceptually (represented by amount).
 * @param id the unique ID of this gold pile.
 * @param amount the quantity of gold in this pile.
 */
public record Gold(UUID id, long amount) implements Item{
  
  /**
   * Creates a new pile of Gold.
   * @param amount the value of gold (must be non-negative).
   */
	public Gold(long amount){
		this(UUID.randomUUID(), amount);
		if(amount < 0) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Merges two piles of gold into a new one.
	 * @param other the other gold pile to add.
	 * @return a new Gold object containing the sum of amounts.
	 */
	public Gold add(Gold other) {
		Objects.requireNonNull(other);
		return new Gold(this.amount + other.amount);
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
		return Rarity.COMMON;
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
		return "GOLD";
	}
}