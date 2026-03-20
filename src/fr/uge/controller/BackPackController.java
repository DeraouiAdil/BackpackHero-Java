package fr.uge.controller;

import java.util.ArrayList;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.items.Item;
import fr.uge.items.Weapon;
import fr.uge.items.enums.WeaponName;
import fr.uge.view.BackPackView;
import fr.uge.view.ImageLoader;

/**
 * Controller for managing the Backpack organization interface.
 * Allows the player to rearrange items in the grid.
 */
public class BackPackController {
  private final BackPack backPack = new BackPack();
  private final ArrayList<Item> inventory = new ArrayList<>();
  private Coordinate currentCursor = new Coordinate(0, 0);
  private Item heldItem;

  /**
   * Starts the main game loop for the Backpack interface.
   * This method initializes the view and handles events (mouse and keyboard)
   * until the user decides to quit.
   *
   * @param context The application context used for rendering and event polling.
   * @throws NullPointerException if the context is null.
   */
  public void gameLoop(ApplicationContext context) {
    Objects.requireNonNull(context);
    var view = initializeGame(context);
    
    while (true) {
      Event event = context.pollOrWaitEvent(10);
      if (event != null && !dispatchEvent(event, context, view)) {
        return;
      }
      BackPackView.draw(context, backPack, view, heldItem, currentCursor, inventory);
    }
  }

  /**
   * Initializes the game assets and view.
   *
   * @param context The application context.
   * @return The created BackPackView.
   */
  private BackPackView initializeGame(ApplicationContext context) {
    var loader = new ImageLoader("data/img");
    var view = BackPackView.create(context, loader);
    populateInventory();
    return view;
  }

  /**
   * Dispatches events to their specific handlers.
   *
   * @param event The event to handle.
   * @param ctx   The application context.
   * @param view  The current view.
   * @return true to continue the loop, false to exit.
   */
  private boolean dispatchEvent(Event event, ApplicationContext ctx, BackPackView view) {
    Objects.requireNonNull(event);
    return switch (event) {
      case PointerEvent pe -> handlePointer(pe, view);
      case KeyboardEvent ke -> handleKeyboard(ke, ctx);
      default -> true;
    };
  }

  /**
   * Handles mouse movements and clicks.
   *
   * @param pe   The pointer event.
   * @param view The view to translate coordinates.
   * @return true always (mouse interaction doesn't close the view).
   */
  private boolean handlePointer(PointerEvent pe, BackPackView view) {
    Objects.requireNonNull(pe);
    Objects.requireNonNull(view);
    updateCursor(pe, view);
    
    if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
      return true;
    }
    processClick(pe, view);
    return true;
  }

  /**
   * Updates the internal cursor position based on mouse location.
   *
   * @param pe   The pointer event.
   * @param view The view.
   */
  private void updateCursor(PointerEvent pe, BackPackView view) {
    var loc = pe.location();
    currentCursor = view.toCoordinate(loc.x(), loc.y());
  }

  /**
   * Processes a mouse click, either on the dock or inside the bag.
   *
   * @param pe   The pointer event.
   * @param view The view.
   */
  private void processClick(PointerEvent pe, BackPackView view) {
    var loc = pe.location();
    if (view.isCursorInDock(loc.y())) {
      var item = view.getItemAtDock(loc.x(), loc.y(), inventory);
      manageDockInteraction(item);
    } else {
      manageBagInteraction();
    }
  }

  /**
   * Manages interaction with the staging area (dock).
   *
   * @param target The item clicked in the dock (if any).
   */
  private void manageDockInteraction(Item target) {
    if (heldItem == null && target != null) {
      heldItem = target;
      inventory.remove(target);
      return;
    }

    if(heldItem != null) {
    	inventory.add(heldItem);
    	heldItem = null;
    }
  }

  /**
   * Manages interaction within the main backpack grid.
   * Handles placing and taking items, including handling cursed items.
   */
  private void manageBagInteraction() {
  	if(heldItem == null) {
  		heldItem = backPack.take(currentCursor);
  		return;
  	}
  	
  	if(heldItem.isCursed()) {
  		if(backPack.addCurse(heldItem, currentCursor)) {
  			heldItem = null;
  		}
  	}else {
  		if(backPack.add(heldItem, currentCursor)) {
  			heldItem = null;
  		}
  	}
  }

  /**
   * Handles keyboard inputs (Quit, Rotate).
   *
   * @param ke  The keyboard event.
   * @param ctx The application context.
   * @return false to quit, true otherwise.
   */
  private boolean handleKeyboard(KeyboardEvent ke, ApplicationContext ctx) {
    Objects.requireNonNull(ke);
    if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) {
      return true;
    }
    return switch (ke.key()) {
      case KeyboardEvent.Key.Q -> quitGame(ctx);
      case KeyboardEvent.Key.R -> rotateHeldItem();
      default -> true;
    };
  }

  /**
   * Disposes the context and exits the controller loop.
   *
   * @param ctx The application context.
   * @return false.
   */
  private boolean quitGame(ApplicationContext ctx) {
    ctx.dispose();
    return false;
  }

  /**
   * Rotates the item currently held by the cursor.
   *
   * @return true.
   */
  private boolean rotateHeldItem() {
    if (heldItem != null) {
      heldItem =  heldItem.rotate();
    }
    return true;
  }

  /**
   * Populates the inventory with default starting items.
   */
  private void populateInventory() {
    inventory.add(new Weapon(WeaponName.DAMAGEDKNIFE, 1));
    inventory.add(new Weapon(WeaponName.DAMAGEDKNIFE, 2));
    inventory.add(new Weapon(WeaponName.DAMAGEDKNIFE, 1));
  }
}