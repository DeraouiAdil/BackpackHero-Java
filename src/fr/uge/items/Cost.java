package fr.uge.items;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.uge.items.enums.TypeCost;

/**
 * Represents the cost required to use an item.
 * Costs can be of different types (Energy, Mana, Gold).
 * @param type the type of the cost (e.g., ENERGY).
 * @param amount the numeric value of the cost.
 */
public record Cost(TypeCost type, @JsonProperty("val") long amount){
	public Cost{
		Objects.requireNonNull(type);
		if(amount < 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public String toString(){
		return "Type :" + type + " amount :" + amount;
	}
	
	/**
	 * Creates a default cost instance (0 Energy).
	 * Used for items that are free to use.
	 */
	public Cost() {
		this(TypeCost.ENERGY, 0);
	}
}