package fr.uge;

import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.ScreenInfo;

import fr.uge.characters.Hero;
import fr.uge.controller.BattleRoomController;
import fr.uge.controller.ExitRoomController;
import fr.uge.controller.HealRoomController;
import fr.uge.controller.ShopRoomController;
import fr.uge.controller.TreasureRoomController;
import fr.uge.dongeon.BattleRoom;
import fr.uge.dongeon.ExitRoom;
import fr.uge.dongeon.Floor;
import fr.uge.dongeon.HealRoom;
import fr.uge.dongeon.Room;
import fr.uge.dongeon.ShopRoom;
import fr.uge.dongeon.SurpriseRoom;
import fr.uge.dongeon.TreasureRoom;
import fr.uge.items.Gold;
import fr.uge.items.Weapon;
import fr.uge.items.enums.WeaponName;
import fr.uge.view.FloorView;

/**
 * Acts as the central manager for the game session.
 * <p>
 * This class coordinates the interaction between the game model (DongeonGame),
 * the player (Hero), and the various controllers that manage specific room logic.
 * It handles the initialization of the game state and the transition between floors.
 * </p>
 */
public class UserInterface {
  private DongeonGame dungeonGame;
  private final Floor floor;
  private int level;
  private Hero hero;
  
  /**
   * Initializes a new game session.
   * Creates the first floor, initializes the hero with starting items (Gold, Weapons),
   * and places the hero in the dungeon.
   *
   * @param username The name of the player.
   */
  public UserInterface(String username) {
    this.level = 1;
    this.floor = new Floor(level);
    this.dungeonGame = new DongeonGame(level, floor);
   
    var baseHero = new Hero(username);
    this.hero = dungeonGame.initializeHeroPosition(baseHero);
  
    this.hero.backpack().add(new Gold(10), new Coordinate(1, 1));
    this.hero.backpack().add(new Gold(1000000), new Coordinate(0, 0));
    var cursedKnife = new Weapon(WeaponName.DAMAGEDKNIFE, 1).becomeCursed();
    this.hero.backpack().addCurse(cursedKnife, new Coordinate(0, 0));
  }

  /**
   * Gets the current hero instance.
   * @return The hero.
   */
  public Hero hero() {
  	return hero; 
  }
  
  /**
   * Gets the current floor layout.
   * @return The floor model.
   */
  public Floor floor() {
  	return dungeonGame.currentFloor(); 
  }

  /**
   * Moves the hero to a destination and triggers the room event if successful.
   * <p>
   * This method is called by the main loop when the user clicks on the map.
   * It delegates pathfinding to the {@link DongeonGame} model.
   * </p>
   *
   * @param dest The target coordinate on the map.
   * @param ctx  The graphics context (to pass to controllers).
   * @param view The floor view (unused here but kept for API consistency).
   * @param info Screen info (resolution).
   * @throws NullPointerException if dest is null.
   */
  public void updateHeroAfterMove(Coordinate dest, ApplicationContext ctx, FloorView view, ScreenInfo info) {
    Objects.requireNonNull(dest);
    this.hero = dungeonGame.moveTo(hero, dest);
    if (hero.isAlive()) {
      runRoom(hero.position(), ctx);
    }
  }

  /**
   * Identifies the room at the given coordinate and launches its specific controller.
   *
   * @param coord The coordinate of the room to run.
   * @param ctx   The application context.
   */
  public void runRoom(Coordinate coord, ApplicationContext ctx) {
    Objects.requireNonNull(coord);
    dungeonGame.getRoom(coord).ifPresent(room -> dispatchRoomController(room, ctx));
  }

  /**
   * Routes the logic based on the type of room the hero is currently in.
   * Uses pattern matching to determine the specific Room subclass.
   *
   * @param room The room object.
   * @param ctx  The application context.
   */
  private void dispatchRoomController(Room room, ApplicationContext ctx) {
    switch (room) {
      case TreasureRoom t -> TreasureRoomController.run(ctx, hero, t);
      case BattleRoom b   -> handleBattleRoom(b);
      case ShopRoom s     -> ShopRoomController.run(ctx, hero, s);
      case HealRoom _     -> HealRoomController.run(ctx, hero);
      case ExitRoom _     -> ExitRoomController.runExitRoom(this);
      case SurpriseRoom s -> handleSurpriseRoom(s, ctx);
      default -> {}
    }
  }

  private void handleBattleRoom(BattleRoom room) {
    if (!room.isCleared()) {
      BattleRoomController.runBattleRoom(this, 64);
    }
  }

  /**
   * Handles the transition to the next dungeon floor/level.
   * Increases difficulty, generates a new floor, and respawns the hero.
   */
  public void dungeonTransition() {
    this.level++;
    var newFloor = new Floor(level);
    this.dungeonGame = new DongeonGame(level, newFloor);
    this.hero = dungeonGame.initializeHeroPosition(hero);
  }
  
  private void handleSurpriseRoom(SurpriseRoom surprise, ApplicationContext ctx) {
    var realRoom = surprise.randRoom(); 
    floor.replaceRoom(surprise.coordRoom(), realRoom);
    dispatchRoomController(realRoom, ctx);
  }

}