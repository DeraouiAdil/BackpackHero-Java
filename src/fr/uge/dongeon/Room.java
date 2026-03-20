package fr.uge.dongeon;

//import java.util.Objects;
//import java.util.concurrent.ThreadLocalRandom;

import fr.uge.Coordinate;
//import fr.uge.items.enums.Rarity;

/**
 * The interface for all rooms in the dungeon.
 * Every room must have a coordinate and a type.
 */
public sealed interface Room permits BattleRoom, ShopRoom, TreasureRoom, Corridor, ExitRoom, SurpriseRoom,HealRoom {
	/**
	 * Returns the coordinates of the room on the map.
	 * @return the coordinate.
	 */
	Coordinate coordRoom();
	
	/**
	 * Returns the symbol used to draw the room.
	 * @return a string symbol (like "S" or "T").
	 */
	String getSymbol();
	
	/**
	 * Returns the type of the room.
	 * @return the TypeRoom enum.
	 */
	TypeRoom getType();

}