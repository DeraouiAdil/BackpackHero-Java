package fr.uge.dongeon;

import fr.uge.Coordinate;

/**
 * A simple room that connects other rooms.
 * Nothing special happens here.
 * @param coordRoom the position.
 * @param levelHero the level of the hero.
 */
public record Corridor(Coordinate coordRoom, int levelHero) implements Room{
	@Override
	public Coordinate coordRoom() {
		return coordRoom;
	}
	@Override
	public String getSymbol() {
		return "C";
	}
	
	@Override
	public TypeRoom getType() {
		return TypeRoom.CORRIDOR;
	}
}