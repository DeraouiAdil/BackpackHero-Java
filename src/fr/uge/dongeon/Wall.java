package fr.uge.dongeon;

import java.util.Objects;
import fr.uge.Coordinate;

/**
 * Represents a wall between two rooms in the dungeon.
 * It is used to know where the hero cannot go.
 * @param room1 the coordinate of the first room.
 * @param room2 the coordinate of the second room.
 */
public record Wall(Coordinate room1 , Coordinate room2) {
	public Wall{
		Objects.requireNonNull(room1);
		Objects.requireNonNull(room2);
	}
}