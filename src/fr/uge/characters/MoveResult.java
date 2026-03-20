package fr.uge.characters;


import java.util.LinkedList;
import java.util.Objects;

import fr.uge.dongeon.Room;

/**
 * The result of a move action by the hero.
 * Contains the updated hero state (new coordinates) and the path remaining to traverse.
 * * @param updatedHero the hero after moving.
 * @param remainingPath the list of rooms left to visit in the current movement chain.
 */
public record MoveResult(Hero updatedHero, LinkedList<Room> remainingPath) {
	public MoveResult{
		Objects.requireNonNull(updatedHero);
		Objects.requireNonNull(remainingPath);
	}
}