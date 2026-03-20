package fr.uge;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import fr.uge.characters.Hero;
import fr.uge.dongeon.Floor;
import fr.uge.dongeon.Room;
import fr.uge.dongeon.TypeRoom;

/**
 * Represents the state and logic of a single dungeon floor game session.
 * <p>
 * This record holds the current floor layout and the hero's game state.
 * It manages hero spawning and movement pathfinding.
 * </p>
 * @param dungeonLevel The current difficulty/depth level of the dungeon.
 * @param currentFloor The floor model containing the grid of rooms.
 * @param rooms        A direct map reference to the rooms on the floor.
 */
public record DongeonGame(int dungeonLevel, Floor currentFloor, Map<Coordinate, Room> rooms) {
  /**
   * Constructs a game session for a specific floor.
   * If the floor is empty, it triggers the finalization of floor generation.
   *
   * @param dungeonLevel The current level.
   * @param floor        The floor layout.
   * @throws NullPointerException if floor is null.
   */
  public DongeonGame(int dungeonLevel, Floor floor) {
    this(dungeonLevel, Objects.requireNonNull(floor), floor.rooms());
    if (floor.rooms().isEmpty()) {
      floor.finalizeFloorGeneration(dungeonLevel);
    }
  }

  /**
   * Safely retrieves a room at a specific coordinate.
   *
   * @param coordinate The position to check.
   * @return An Optional containing the Room if it exists, or empty otherwise.
   * @throws NullPointerException if coordinate is null.
   */
  public Optional<Room> getRoom(Coordinate coordinate) {
    Objects.requireNonNull(coordinate);
    return Optional.ofNullable(rooms.get(coordinate));
  }


  /**
   * Places the hero at a random valid starting position.
   * <p>
   * The hero is always spawned in a {@link TypeRoom#CORRIDOR} to avoid immediate combat 
   * or interaction upon entering a floor.
   * </p>
   *
   * @param baseHero The hero object containing current stats and inventory.
   * @return A new Hero instance positioned at the spawn point.
   * @throws NullPointerException if baseHero is null.
   * @throws IllegalStateException if no corridors are found on the floor.
   */
  public Hero initializeHeroPosition(Hero baseHero) {
    Objects.requireNonNull(baseHero);
    var corridors = rooms.values().stream()
            .filter(r -> r.getType().equals(TypeRoom.CORRIDOR))
            .toList();

    if (corridors.isEmpty()) {
      throw new IllegalStateException();
    }
    
    var spawnRoom = corridors.get(new Random().nextInt(corridors.size()));
    var newHero = new Hero(baseHero.username(), baseHero.heroType(), 
        baseHero.backpack(), spawnRoom.coordRoom(), baseHero.lvl());
    newHero.setPv(baseHero.pv());
    return newHero;
  }

  /**
   * Calculates a path and moves the hero towards a target coordinate.
   * <p>
   * The movement stops automatically if the hero enters an interactive room 
   * (like a Battle, Treasure, or Boss room), allowing the game loop to trigger the event.
   * </p>
   *
   * @param hero   The hero moving.
   * @param target The destination coordinate.
   * @return The updated Hero object after the movement (or partial movement).
   * @throws NullPointerException if hero or target is null.
   */
  public Hero moveTo(Hero hero, Coordinate target) {
    Objects.requireNonNull(hero);
    Objects.requireNonNull(target);
    var path = currentFloor.giveShortestPath(hero.position(), target);
    
    if (path.isEmpty()) {
    	return hero;
    }
    
    return processMove(hero, path);
  }

  /**
   * Processes the movement step-by-step along the calculated path.
   * Stops the movement loop if the hero enters a non-corridor room.
   *
   * @param hero The hero to move.
   * @param path The list of rooms constituting the path.
   * @return The updated Hero at the final reached position.
   */
  private Hero processMove(Hero hero, LinkedList<Room> path) {
    var currentHero = hero;
    while (currentHero.isAlive() && !path.isEmpty()) {
      var result = currentHero.move(path);
      currentHero = result.updatedHero();

      if (isRoomInteraction(currentHero.position())) {
        break;
      }
    }
    return currentHero;
  }

  /**
   * Checks if the room at the given position requires interaction.
   * * @param pos The coordinate to check.
   * @return true for any room that is NOT a corridor (Battle, Boss, Treasure, etc.), false otherwise.
   */
  private boolean isRoomInteraction(Coordinate pos) {
    return getRoom(pos)
            .map(r -> r.getType().equals(TypeRoom.CORRIDOR) == false)
            .orElse(false);
  }
}