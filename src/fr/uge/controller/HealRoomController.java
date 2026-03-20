package fr.uge.controller;

import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.characters.Hero;
import fr.uge.view.HealRoomView;
import fr.uge.view.ImageLoader;

/**
 * Controller for the Heal Room.
 * Allows the hero to purchase healing or curse removal.
 */
public class HealRoomController {
  private final Hero hero;

  /**
   * Creates a controller for the healing room.
   *
   * @param hero The hero entering the room.
   * @throws NullPointerException if hero is null.
   */
  public HealRoomController(Hero hero) {
    this.hero = Objects.requireNonNull(hero);
  }

  /**
   * Static entry point to start the heal room interaction loop.
   *
   * @param context The application context used for event polling and rendering.
   * @param hero    The hero character.
   */
  public static void run(ApplicationContext context, Hero hero) {
  	var controller = new HealRoomController(hero);
  	controller.healLoop(context);
  }
  
  /**
   * Attempts to cure a curse for a gold cost.
   *
   * @param cost The cost in gold.
   * @return true if the action was successful or attempted, false if not enough gold.
   */
  private boolean performCure(int cost) {
  	if(hero.backpack().getGold() < cost) {
  		return false;
  	}
  	if(hero.backpack().removeCurseRandomly()) {
  		hero.backpack().useGold(cost);
  	}
  	return true;
  }

  /**
   * The main execution loop for the healing room.
   * Initializes the view and processes user input (clicks on buttons) until the player leaves.
   *
   * @param context The application context.
   */
  public void healLoop(ApplicationContext context) {
    var loader = new ImageLoader("data/img");
    
    var screen = context.getScreenInfo();
    var view = new HealRoomView(loader, screen.width(), screen.height());

    while (true) {
      var event = context.pollOrWaitEvent(10);
      if (event != null && !handleEvent(event, context, view)) {
        return;
      }
      HealRoomView.draw(context, view, hero);
    }
  }

  /**
   * Dispatches events to keyboard or pointer handlers.
   *
   * @param event The event.
   * @param ctx   The application context.
   * @param view  The heal room view.
   * @return true to continue, false to exit.
   */
  private boolean handleEvent(Event event, ApplicationContext ctx, HealRoomView view) {
    return switch (event) {
      case PointerEvent pe -> handlePointer(pe, view, ctx);
      case KeyboardEvent ke -> handleKeyboard(ke, ctx);
      default -> true;
    };
  }

  /**
   * Handles keyboard inputs.
   * 'Q' quits the room.
   *
   * @param ke  The keyboard event.
   * @param ctx The application context.
   * @return false if 'Q' is pressed, true otherwise.
   */
  private boolean handleKeyboard(KeyboardEvent ke, ApplicationContext ctx) {
    if (ke.action() == KeyboardEvent.Action.KEY_PRESSED && ke.key() == KeyboardEvent.Key.Q) {
      return false;
    }
    return true;
  }

  /**
   * Handles pointer events for selecting healing options.
   *
   * @param pe   The pointer event.
   * @param view The view.
   * @param ctx  The application context.
   * @return true to continue, false to exit.
   */
  private boolean handlePointer(PointerEvent pe, HealRoomView view, ApplicationContext ctx) {
    if (pe.action() != PointerEvent.Action.POINTER_DOWN) return true;
    var choice = view.getSelection(pe.location().x(), pe.location().y());
    return switch (choice) {
      case 0 -> close(ctx);
      case 1 -> performHeal(25, 5);
      case 2 -> performHeal(50, 10);
      case 3 -> performCure(15);
      default -> true;
    };
  }
  
  /**
   * Closes the room view.
   *
   * @param ctx The application context.
   * @return false.
   */
  private boolean close(ApplicationContext ctx) {
    return false;
  }

  /**
   * Performs healing if the hero has enough gold and isn't at max health.
   *
   * @param amount The amount of PV to heal.
   * @param cost   The gold cost.
   * @return true.
   */
  private boolean performHeal(int amount, int cost) {
    if (hero.pv() >= hero.maxPv()) {
      System.out.println("PV déjà au maximum !"); 
      return true; 
    }
    if (hero.backpack().useGold(cost)) {
      hero.heal(amount);
    }
    return true;
  }
  
}