package fr.uge.dongeon;

import java.util.Objects;
import fr.uge.Coordinate;

/**
 * Represents a node in the pathfinding graph.
 * Used to store a coordinate and its distance from the start.
 */
public record Node(Coordinate coord, int dist) {
  
  /**
   * Canonical constructor with validation.
   * @param coord the coordinate on the map.
   * @param dist the distance from the starting point.
   */
  public Node {
    Objects.requireNonNull(coord);
    if (dist < 0) {
    	throw new IllegalArgumentException("Distance cannot be negative");
    }
  }
}