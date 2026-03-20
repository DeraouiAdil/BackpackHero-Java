package fr.uge.dongeon;

import fr.uge.Coordinate;

/**
 * The room that allows the hero to leave the floor.
 * @param coordRoom the position.
 * @param levelHero the level of the hero.
 */
public record ExitRoom(Coordinate coordRoom, int levelHero) implements Room{
	@Override
	public Coordinate coordRoom() {
		return coordRoom;
	}
	@Override
	public String getSymbol() {
		return "E";
	}
	
	@Override
	public TypeRoom getType() {
		return TypeRoom.EXITROOM;
	}
}