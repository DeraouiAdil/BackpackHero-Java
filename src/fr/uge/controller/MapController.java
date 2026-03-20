package fr.uge.controller;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.Coordinate;
import fr.uge.UserInterface;
import fr.uge.view.FloorView;
import fr.uge.view.ImageLoader;

/**
 * Controller responsible for the main map navigation.
 * Allows the player to click on adjacent rooms to move the hero.
 */
public class MapController {
	
	/**
   * Starts the map interaction loop.
   * This method handles the display of the floor, mouse clicks for movement,
   * checks for game-over conditions, and handles the exit command.
   *
   * @param ctx  The application context for event handling and rendering.
   * @param i    The main UserInterface controller containing the game state (floor, hero).
   * @param size The pixel size of a single map tile (used for rendering and click detection).
   */
    public static void runMaps(ApplicationContext ctx, UserInterface i, int size){       
      var loader = new ImageLoader("data/img/map");
      
      while(true) {
        var event = ctx.pollOrWaitEvent(10);
        var currentFloor = i.floor();
        var screenInfo = ctx.getScreenInfo();
        var floorView = new FloorView(currentFloor.width(), currentFloor.height(), size, loader);
        
        if(event != null) {
            switch(event) {
	            case PointerEvent e -> {
	              if(e.action() == PointerEvent.Action.POINTER_DOWN) {
                  int xOri = (screenInfo.width() - (i.floor().width() * size))/2;
                  int yOri = (screenInfo.height() - (i.floor().height() * size))/2;
                  var coordinate = new Coordinate((e.location().y() - yOri)/size, (e.location().x() -xOri)/size);
                  
                  i.updateHeroAfterMove(coordinate, ctx, floorView, screenInfo);

                  if (!i.hero().isAlive()) {
                      return;
                  }
	              }
	            }
              case KeyboardEvent e -> {
                if (e.action() == KeyboardEvent.Action.KEY_PRESSED && e.key() == KeyboardEvent.Key.Q) {
                  return;
                }
              }
            }
        }
        floorView.draw(ctx, screenInfo.width(), screenInfo.height(), screenInfo, i.hero().position(), i.floor());
      }
    }
}