package fr.uge.controller;

import java.awt.Color;
import java.util.Objects;
import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.characters.Hero;
import fr.uge.dongeon.TreasureRoom;
import fr.uge.items.Item;
import fr.uge.items.ItemEmpty;
import fr.uge.items.Slot;
import fr.uge.view.ImageLoader;
import fr.uge.view.TreasureRoomView;

/**
 * Controller handling the logic inside a Treasure Room.
 * Allows the hero to open a chest and transfer items to their inventory.
 */
public class TreasureRoomController {
  private final Hero hero;
  private final TreasureRoom treasureRoom;
  private boolean isChestOpen = false;
  private Item heldItem = null;
  private Coordinate mousePosition = new Coordinate(0, 0);

  /**
   * Creates a controller for a specific treasure room.
   *
   * @param hero         The hero interacting with the room.
   * @param treasureroom The treasure room model containing the items.
   * @throws NullPointerException if hero or treasureRoom is null.
   */
  public TreasureRoomController(Hero hero, TreasureRoom treasureroom) {
    this.hero = Objects.requireNonNull(hero);
    this.treasureRoom = Objects.requireNonNull(treasureroom);
  }

  /**
   * Static entry point to start the treasure room interaction loop.
   * This method initializes the view and blocks until the player decides to leave the room.
   *
   * @param context The application context used for event polling and rendering.
   * @param hero    The hero entering the room.
   * @param room    The treasure room data.
   */
  public static void run(ApplicationContext context, Hero hero, TreasureRoom room) {
    var controller = new TreasureRoomController(hero, room);
    controller.treasureLoop(context);
  }

  /**
   * Main game loop for the treasure room.
   * Updates the view and handles user events (mouse, keyboard).
   *
   * @param context The application context.
   */
  private void treasureLoop(ApplicationContext context) {
    var loader = new ImageLoader("data/img");

    var screen = context.getScreenInfo();
    var view = new TreasureRoomView(loader, screen.width(), screen.height());

    while (true) {
      var event = context.pollOrWaitEvent(10);
      if (event != null && !handleEvent(event, context, view)) {
        return;
      }
      TreasureRoomView.draw(context, view, treasureRoom, hero.backpack(), heldItem, mousePosition, isChestOpen);
    }
  }

  /**
   * Dispatches events to appropriate handlers.
   *
   * @param event The event to process.
   * @param ctx   The application context.
   * @param view  The treasure room view.
   * @return true to continue, false to exit.
   */
  private boolean handleEvent(Event event, ApplicationContext ctx, TreasureRoomView view) {
    return switch (event) {
      case PointerEvent pe -> handlePointer(pe, view, ctx);
      case KeyboardEvent ke -> handleKeyboard(ke, ctx);
      default -> true;
    };
  }

  /**
   * Handles keyboard inputs.
   * 'Q' to quit (leave room), 'R' to rotate held item.
   *
   * @param ke  The keyboard event.
   * @param ctx The application context.
   * @return false if the user wants to leave, true otherwise.
   */
  private boolean handleKeyboard(KeyboardEvent ke, ApplicationContext ctx) {
    if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) return true;
    return switch (ke.key()) {
      case KeyboardEvent.Key.Q -> {
        yield false;
      }
      case KeyboardEvent.Key.R -> {
      	if (heldItem != null && !heldItem.isCursed()) heldItem = heldItem.rotate();
        yield true;
      }
      default -> true;
    };
  }

  /**
   * Handles pointer events (opening chest, moving items).
   *
   * @param pe   The pointer event.
   * @param view The view.
   * @param ctx  The application context.
   * @return true to continue, false to exit (if exit button clicked).
   */
  private boolean handlePointer(PointerEvent pe, TreasureRoomView view, ApplicationContext ctx) {
    mousePosition = new Coordinate(pe.location().x(), pe.location().y());
    if (pe.action() != PointerEvent.Action.POINTER_DOWN) return true;

    if (!isChestOpen) {
      isChestOpen = true;
      return true;
    }
    return handleClickOpen(pe.location().x(), pe.location().y(), view, ctx);
  }

  /**
   * Handles clicks when the chest is already open.
   * Manages drag and drop logic.
   *
   * @param x    Mouse X coordinate.
   * @param y    Mouse Y coordinate.
   * @param view The view.
   * @param ctx  The application context.
   * @return false if the exit button is clicked, true otherwise.
   */
  private boolean handleClickOpen(int x, int y, TreasureRoomView view, ApplicationContext ctx) {
    if (view.isOverButton(x, y)) {
      return false;
    }
    var chestC = view.toChestCoordinate(x, y);
    var bagC = view.toBagCoordinate(x, y);

    if (heldItem == null) {
      tryPickUp(chestC, bagC);
    } else {
      tryPlace(chestC, bagC);
    }
    return true;
  }

  /**
   * Tries to pick up an item from the chest or the backpack.
   *
   * @param chestC Chest slot coordinate.
   * @param bagC   Backpack slot coordinate.
   */
  private void tryPickUp(Coordinate chestC, Coordinate bagC) {
    Item item = null;
    if (chestC != null) {
      item = treasureRoom.remove(chestC);
    } else if (bagC != null) {
      item = hero.backpack().take(bagC);
    }
    if (!isEmpty(item)) {
      heldItem = item;
    }
  }

  /**
   * Tries to place the held item into the chest or the backpack.
   *
   * @param chestC Chest slot coordinate.
   * @param bagC   Backpack slot coordinate.
   */
  private void tryPlace(Coordinate chestC, Coordinate bagC) {
    if (chestC != null) {
      if (treasureRoom.add(heldItem, chestC)) heldItem = null;
    } else if (bagC != null) {
      if (hero.backpack().add(heldItem, bagC)) heldItem = null;
    }
  }
  
  /**
   * Checks if an item is considered empty (null, ItemEmpty, or Slot).
   *
   * @param item The item to check.
   * @return true if the item acts as a placeholder or is null.
   */
  private boolean isEmpty(Item item) {
    return switch (item) {
        case null -> true;
        case ItemEmpty _ -> true;
        case Slot _ -> true;
        default -> false;
    };
  }
}