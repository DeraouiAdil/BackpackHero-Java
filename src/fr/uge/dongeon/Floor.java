package fr.uge.dongeon;

import java.util.*;
import fr.uge.Coordinate;

/**
 * Represents a single floor of the dungeon.
 * It manages room generation and pathfinding.
 */
public class Floor {
  private final Map<Coordinate, Room> rooms = new HashMap<>();
  private final int WIDTH = 11;
  private final int HEIGHT = 5;
  private final int number;
  private final Random random = new Random();

  /**
   * Creates a new floor with a specific number.
   * @param number the level number of this floor.
   */
  public Floor(int number) {
    if (number < 0) throw new IllegalArgumentException("Floor number must be positive");
    this.number = number;
  }

  /**
   * Returns an unmodifiable view of the rooms.
   * @return the map of coordinates to rooms.
   */
  public Map<Coordinate, Room> rooms() {
    return Collections.unmodifiableMap(rooms);
  }

  /**
   * Gets a room at specific coordinates.
   * @param x the row index.
   * @param y the column index.
   * @return the room at (x, y) or null.
   */
  public Room get(int x, int y) {
    return rooms.get(new Coordinate(x, y));
  }
  
  /**
   * Returns the width of the floor grid.
   * @return the width.
   */
  public int width() {
  	return WIDTH; 
  }
  
  /**
   * Returns the height of the floor grid.
   * @return the height.
   */
  public int height() {
  	return HEIGHT; 
  }

  /**
   * Generates the layout of the floor using a randomized DFS.
   * Ensures rooms are separated by corridors and assigns types.
   * @param level the difficulty level for the rooms.
   */
  public void finalizeFloorGeneration(int level) {
    var start = new Coordinate(HEIGHT / 2, WIDTH / 2);
    rooms.clear();
    var targetSize = 8 + random.nextInt(8); 
    var generatedSlots = runRandomizedDFS(start, targetSize);

    var exit = findFurthestRoom(start, generatedSlots);
    assignRoomTypes(start, exit, generatedSlots, level);
  }

  /**
   * Runs a randomized DFS to create the room structure.
   * Limits the dungeon size to a random target.
   * @param start the starting coordinate.
   * @param limit the maximum number of rooms.
   * @return the list of created room coordinates.
   */
  private List<Coordinate> runRandomizedDFS(Coordinate start, int limit) {
    var stack = new Stack<Coordinate>();
    var slots = new ArrayList<Coordinate>();
    
    stack.push(start);
    slots.add(start);
    rooms.put(start, new Corridor(start, 0));

    while (!stack.isEmpty()) {
      if (slots.size() >= limit) {
        stack.pop();
        continue;
      }
      processStackStep(stack, slots);
    }
    return slots;
  }

  /**
   * Processes a single step of the DFS.
   * Picks a random neighbor to continue the path.
   * @param stack the DFS stack.
   * @param slots the list of valid room slots.
   */
  private void processStackStep(Stack<Coordinate> stack, List<Coordinate> slots) {
    var current = stack.peek();
    var neighbors = getUnvisitedNeighborsDist2(current);
    
    Collections.shuffle(neighbors, random); 

    if (neighbors.isEmpty()) {
      stack.pop();
    } else {
      var dest = neighbors.get(0);
      connectAndPush(current, dest, stack, slots);
    }
  }

  /**
   * Connects two rooms with a corridor and updates the stack.
   * @param curr the current coordinate.
   * @param dest the destination coordinate.
   * @param stack the DFS stack.
   * @param slots the list of valid room slots.
   */
  private void connectAndPush(Coordinate curr, Coordinate dest, Stack<Coordinate> stack, List<Coordinate> slots) {
    var midX = (curr.x() + dest.x()) / 2;
    var midY = (curr.y() + dest.y()) / 2;
    var midCoord = new Coordinate(midX, midY);

    rooms.put(midCoord, new Corridor(midCoord, 0));
    rooms.put(dest, new Corridor(dest, 0));
    
    slots.add(dest);
    stack.push(dest);
  }

  /**
   * Finds valid unvisited neighbors at distance 2.
   * Checks bounds and existing rooms.
   * @param c the center coordinate.
   * @return a list of valid neighbors.
   */
  private List<Coordinate> getUnvisitedNeighborsDist2(Coordinate c) {
    var list = new ArrayList<Coordinate>();
    int[][] dirs = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};
    
    for (var d : dirs) {
      var dest = new Coordinate(c.x() + d[0], c.y() + d[1]);
      if (isInside(dest) && !rooms.containsKey(dest)) {
        list.add(dest);
      }
    }
    return list;
  }

  /**
   * Assigns specific types to the generated rooms.
   * Guarantees a Boss Room and an Exit.
   * @param start the spawn point.
   * @param exit the exit point.
   * @param slots the available room coordinates.
   * @param lvl the level difficulty.
   */
  private void assignRoomTypes(Coordinate start, Coordinate exit, List<Coordinate> slots, int lvl) {
    slots.remove(start);
    slots.remove(exit);
    Collections.shuffle(slots);

    rooms.put(start, new Corridor(start, lvl));
    rooms.put(exit, new ExitRoom(exit, lvl));
    
    var deck = createRoomDeck(slots.size());
    for (var pos : slots) {
      var type = deck.isEmpty() ? TypeRoom.CORRIDOR : deck.poll();
      rooms.put(pos, createRoom(type, pos, lvl));
    }
  }

  /**
   * Creates a randomized deck of room types.
   * Guarantees one Boss room and one Surprise room.
   * @param size the number of rooms needed.
   * @return a queue of room types.
   */
  private Queue<TypeRoom> createRoomDeck(int size) {
    var deck = new LinkedList<TypeRoom>();
    deck.add(TypeRoom.BOSSROOM);
    deck.add(TypeRoom.SURPRISEROOM);
    
    while (deck.size() < size) {
      int r = random.nextInt(100);
      if (r < 30) deck.add(TypeRoom.BATTLEROOM);
      else if (r < 50) deck.add(TypeRoom.TREASUREROOM);
      else if (r < 65) deck.add(TypeRoom.SHOPROOM);
      else if (r < 80) deck.add(TypeRoom.HEALROOM);
      else deck.add(TypeRoom.BATTLEROOM);
    }
    Collections.shuffle(deck);
    return deck;
  }

  /**
   * Factory method to create a Room object from a type.
   * @param type the type of room.
   * @param c the coordinate.
   * @param lvl the level difficulty.
   * @return the new Room instance.
   */
  private Room createRoom(TypeRoom type, Coordinate c, int lvl) {
    return switch (type) {
      case BOSSROOM -> new BattleRoom(c, lvl, true);
      case BATTLEROOM -> new BattleRoom(c, lvl, false);
      case TREASUREROOM -> new TreasureRoom(c, lvl);
      case SHOPROOM -> new ShopRoom(c, lvl);
      case HEALROOM -> new HealRoom(c, lvl);
      case SURPRISEROOM -> new SurpriseRoom(c, lvl);
      case EXITROOM -> new ExitRoom(c, lvl);
      default -> new Corridor(c, lvl);
    };
  }

  /**
   * Computes the shortest path between two coordinates.
   * Uses Dijkstra's algorithm.
   * @param start the start coordinate.
   * @param end the target coordinate.
   * @return a list of rooms representing the path.
   */
  public LinkedList<Room> giveShortestPath(Coordinate start, Coordinate end) {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    if (!rooms.containsKey(start) || !rooms.containsKey(end)) return new LinkedList<>();

    var prev = new HashMap<Coordinate, Coordinate>();
    var dist = new HashMap<Coordinate, Integer>();
    dijkstra(start, end, dist, prev);
    return buildPath(end, prev);
  }

  /**
   * Runs the main loop of Dijkstra's algorithm.
   * Populates the distance and parent maps.
   * @param start start coord.
   * @param end end coord (can be null for full scan).
   * @param dist map of distances.
   * @param prev map of previous nodes.
   */
  private void dijkstra(Coordinate start, Coordinate end, 
      Map<Coordinate, Integer> dist, Map<Coordinate, Coordinate> prev) {
    var pq = new PriorityQueue<Node>(Comparator.comparingInt(Node::dist));
    rooms.keySet().forEach(c -> dist.put(c, Integer.MAX_VALUE));
    dist.put(start, 0);
    pq.add(new Node(start, 0));

    while (!pq.isEmpty()) {
      var curr = pq.poll();
      if (curr.coord().equals(end)) break; // Utilisation de coord() car record externe
      if (curr.dist() > dist.get(curr.coord())) continue;
      exploreNeighbors(curr, dist, prev, pq);
    }
  }

  /**
   * Explores neighbors for Dijkstra's algorithm.
   * Updates distances if a shorter path is found.
   * @param curr the current node.
   * @param dist the distance map.
   * @param prev the parent map.
   * @param pq the priority queue.
   */
  private void exploreNeighbors(Node curr, Map<Coordinate, Integer> dist, 
      Map<Coordinate, Coordinate> prev, PriorityQueue<Node> pq) {
    for (var n : getConnectedNeighbors(curr.coord())) {
      var newDist = dist.get(curr.coord()) + 1;
      if (newDist < dist.get(n)) {
        dist.put(n, newDist);
        prev.put(n, curr.coord());
        pq.add(new Node(n, newDist));
      }
    }
  }

  /**
   * Identifies the room furthest from the start.
   * Used to place the exit.
   * @param start the start coordinate.
   * @param slots the list of candidate rooms.
   * @return the furthest coordinate.
   */
  private Coordinate findFurthestRoom(Coordinate start, List<Coordinate> slots) {
    var distMap = new HashMap<Coordinate, Integer>();
    dijkstra(start, null, distMap, new HashMap<>());
    
    return slots.stream()
        .max(Comparator.comparingInt(c -> distMap.getOrDefault(c, 0)))
        .orElse(slots.get(slots.size() - 1));
  }

  /**
   * Reconstructs the path from the parent map.
   * @param end the destination coordinate.
   * @param prev the map of previous nodes.
   * @return the path as a list of rooms.
   */
  private LinkedList<Room> buildPath(Coordinate end, Map<Coordinate, Coordinate> prev) {
    var path = new LinkedList<Room>();
    var curr = end;
    if (!prev.containsKey(end) && prev.isEmpty()) return path;

    while (curr != null) {
      path.addFirst(rooms.get(curr));
      curr = prev.get(curr);
    }
    return path;
  }

  /**
   * Checks if a coordinate is inside the grid boundaries.
   * @param c the coordinate to check.
   * @return true if inside, false otherwise.
   */
  private boolean isInside(Coordinate c) {
    return c.x() >= 0 && c.x() < HEIGHT && c.y() >= 0 && c.y() < WIDTH;
  }

  /**
   * Gets valid neighbors at distance 1.
   * Used for movement and pathfinding.
   * @param c the current coordinate.
   * @return a list of adjacent neighbors.
   */
  private List<Coordinate> getConnectedNeighbors(Coordinate c) {
    var list = new ArrayList<Coordinate>();
    int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    for (var d : dirs) {
      var n = new Coordinate(c.x() + d[0], c.y() + d[1]);
      if (rooms.containsKey(n)) list.add(n);
    }
    return list;
  }

  @Override
  public String toString() {
    return "Floor " + number + " (" + rooms.size() + " rooms)";
  }
  
  /**
   * Replaces an existing room at a specific coordinate with a new one.
   * Useful for SurpriseRooms revealing their true nature.
   * @param coord the coordinate to update.
   * @param newRoom the new room to place there.
   */
  public void replaceRoom(Coordinate coord, Room newRoom) {
    Objects.requireNonNull(coord);
    Objects.requireNonNull(newRoom);
    if (rooms.containsKey(coord)) {
      rooms.put(coord, newRoom);
    }
  }
}