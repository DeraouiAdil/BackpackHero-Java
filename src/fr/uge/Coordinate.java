package fr.uge;

import java.util.List;
import java.util.Objects;

/**
 * Represents a 2D coordinate in a grid system (x, y).
 * <p>
 * This record includes utility methods for console input and geometric transformations
 * required for the inventory shape system.
 * </p>
 * @param x The horizontal position.
 * @param y The vertical position.
 */
public record Coordinate(int x, int y) {
	
	/**
	 * Canonical constructor.
	 */
	public Coordinate{ }
	
	@Override
	public String toString() {
		return "coordinate: (" + x + ", " + y + ")";
	}

	/**
   * Prompts the user to enter coordinates via the console.
   * <p>
   * It loops indefinitely until the user provides two valid integers separated by a space.
   * This is primarily used for the console-based version of the game.
   * </p>
   * @return A valid Coordinate object parsed from user input.
   */
	public static Coordinate askCoordinate() {
    while(true){
      var input = IO.readln("").trim();
      var tab = input.split("\\s+");
      if(tab.length != 2){
        IO.println("erreur: entrer deux nombres séparés par un espace");
        continue;
      }
      try{
        var x = Integer.parseInt(tab[0]);
        var y = Integer.parseInt(tab[1]);
        return new Coordinate(x, y);
      }catch (NumberFormatException e){
        IO.println("erreur : Les coordonnées doivent être des chiffres entiers.");
      }catch (IllegalArgumentException e){
        IO.println("erreur : " + e.getMessage());
      }
    }
	}

	/**
   * Rotates a list of coordinates (representing an item's shape) by 90-degree increments.
   * <p>
   * After the rotation transformation, the shape is normalized (shifted) so that its
   * top-left most point aligns with (0,0). This ensures the shape remains anchored correctly.
   * </p>
   *
   * @param coordinates The list of coordinates defining the item's shape.
   * @param rotation    The number of 90-degree clockwise rotations (e.g., 1 = 90°, 2 = 180°).
   * @return A new list of rotated and normalized coordinates.
   * @throws NullPointerException if coordinates list is null.
   * @throws IllegalArgumentException if rotation is negative.
   */
	public static List<Coordinate> applyRotation(List<Coordinate> coordinates, int rotation) {
    Objects.requireNonNull(coordinates);
    if(rotation < 0) {
        throw new IllegalArgumentException();
    }
    var r = rotation % 4;
    if (r == 0) return coordinates;
    var cur = coordinates;
    for (int i = 0; i < r; i++) {
        cur = cur.stream()
                 .map(c -> new Coordinate(c.y(), -c.x()))
                 .toList();
    }

    int minX = cur.stream().mapToInt(Coordinate::x).min().orElse(0);
    int minY = cur.stream().mapToInt(Coordinate::y).min().orElse(0);

    return cur.stream()
              .map(c -> new Coordinate(c.x() - minX, c.y() - minY))
              .toList();
	}
}