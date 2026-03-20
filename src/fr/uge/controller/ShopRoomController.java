package fr.uge.controller;

import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.Coordinate;
import fr.uge.characters.Hero;
import fr.uge.dongeon.ShopRoom;
import fr.uge.items.Item;
import fr.uge.view.ImageLoader;
import fr.uge.view.ShopRoomView;

/**
 * Controller managing the interactions within a Shop Room.
 * It handles the merchant's stock, buying/selling items, and the drag-and-drop interface.
 */
public class ShopRoomController {
  private final Hero hero;
  private final ShopRoom shop;
  private Item heldItem = null;
  private Coordinate mousePosition = new Coordinate(0, 0);
  private Coordinate sourceShopCoord = null; 

  /**
   * Creates a controller for the shop room.
   *
   * @param hero The hero visiting the shop.
   * @param shop The shop model containing the merchant's inventory.
   * @throws NullPointerException if hero or shop is null.
   */
  public ShopRoomController(Hero hero, ShopRoom shop) {
    this.hero = Objects.requireNonNull(hero);
    this.shop = Objects.requireNonNull(shop);
  }
  
  /**
   * Static entry point to start the shop interaction loop.
   *
   * @param context The application context used for event polling and rendering.
   * @param hero    The hero character.
   * @param shop    The shop room data.
   */
  public static void run(ApplicationContext context, Hero hero, ShopRoom shop) {
  	var controller = new ShopRoomController(hero, shop);
  	controller.shopLoop(context);
  }

  /**
   * The main execution loop for the shop.
   * Generates merchant stock if necessary, initializes the view, 
   * and processes events until the player chooses to leave.
   *
   * @param context The application context.
   */
  public void shopLoop(ApplicationContext context) {
    if (!shop.isStockGenerated()) shop.generateMerchantStock();
    
    var loader = new ImageLoader("data/img");
    var screen = context.getScreenInfo();
    var view = new ShopRoomView(loader, screen.width(), screen.height());

    while (true) {
      var event = context.pollOrWaitEvent(10);
      if (event != null && !handleEvent(event, context, view)) {
        return;
      }
      ShopRoomView.draw(context, view, shop, hero, heldItem, mousePosition);
    }
  }

  /**
   * Dispatches events to specific handlers (Keyboard or Pointer).
   *
   * @param event The event to handle.
   * @param ctx   The application context.
   * @param view  The current view of the shop.
   * @return true to continue the loop, false to exit.
   */
  private boolean handleEvent(Event event, ApplicationContext ctx, ShopRoomView view) {
    return switch (event) {
      case PointerEvent pe -> handlePointer(pe, view, ctx);
      case KeyboardEvent ke -> handleKeyboard(ke, ctx, view); 
      default -> true;
    };
  }

  /**
   * Handles keyboard inputs.
   * 'Q' to quit, 'R' to rotate held item, 'S' to sell an item pointed by the mouse.
   *
   * @param ke   The keyboard event.
   * @param ctx  The application context.
   * @param view The shop view used for coordinate conversion.
   * @return false if the user wants to quit, true otherwise.
   */
  private boolean handleKeyboard(KeyboardEvent ke, ApplicationContext ctx, ShopRoomView view) {
    if (ke.action() == KeyboardEvent.Action.KEY_PRESSED) {
      switch (ke.key()) {
        case Q -> { return false; }
        case R -> {
        	if (heldItem != null && !heldItem.isCursed()) {
          	heldItem.rotate();
          }
        }
        case S -> {
          if (heldItem == null) {
            var bagCoord = view.toBagCoordinate(mousePosition.x(), mousePosition.y());
            
            if (bagCoord != null) {
              shop.sellItem(hero, bagCoord);
            }
          }
        }
        default -> {}
      }
    }
    return true;
  }

  /**
   * Handles pointer events (mouse movement and clicks).
   * Manages picking up and placing items (drag and drop) between the shop and the inventory.
   *
   * @param pe   The pointer event.
   * @param view The shop view.
   * @param ctx  The application context.
   * @return false if the exit button is clicked, true otherwise.
   */
  private boolean handlePointer(PointerEvent pe, ShopRoomView view, ApplicationContext ctx) {
    mousePosition = new Coordinate(pe.location().x(), pe.location().y());
    if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
    	return true;
    }
    if (view.isOverButton(pe.location().x(), pe.location().y())) {
      return false;
    }
    var shopC = view.toShopCoordinate(pe.location().x(), pe.location().y());
    var bagC = view.toBagCoordinate(pe.location().x(), pe.location().y());
    if (heldItem == null) {
      tryPickUp(shopC, bagC);
    } else {
      tryPlace(shopC, bagC);
    }
    return true;
  }
  
  /**
   * Attempts to pick up an item from the shop shelf or the hero's backpack.
   *
   * @param shopC The coordinate in the shop grid (can be null).
   * @param bagC  The coordinate in the backpack grid (can be null).
   */
  private void tryPickUp(Coordinate shopC, Coordinate bagC) {
    if (shopC != null) {
      var key = new Coordinate(shopC.y(), shopC.x());
      var item = shop.getItem(key);
      if (item != null && !item.name().equals("EMPTY")) {
        heldItem = item;
        sourceShopCoord = key;
      }
    } else if (bagC != null) {
      heldItem = hero.backpack().take(bagC);
      sourceShopCoord = null;
    }
  }

  /**
   * Attempts to place the currently held item into the shop (buy) or the backpack.
   *
   * @param shopC The coordinate in the shop grid (can be null).
   * @param bagC  The coordinate in the backpack grid (can be null).
   */
  private void tryPlace(Coordinate shopC, Coordinate bagC) {
    if (bagC != null) {
      if (sourceShopCoord != null) {
        shop.buyItem(hero, sourceShopCoord, bagC);
        heldItem = null;
        sourceShopCoord = null;
      } else {
        if (hero.backpack().add(heldItem, bagC)) heldItem = null;
      }
    } else if (shopC == null) {
      heldItem = null;
      sourceShopCoord = null;
    }
  }
}