package fr.uge.controller;

import java.awt.Color;

import com.github.forax.zen.Application;
import com.github.forax.zen.PointerEvent;

import fr.uge.UserInterface;
import fr.uge.view.ExitRoomView;
import fr.uge.view.ImageLoader;

/**
 * Controller for the Exit Room interaction.
 * Allows the player to proceed to the next dungeon level or return to the map.
 */
public class ExitRoomController {
	
	/**
   * Starts the Exit Room interaction loop.
   * It renders the exit view and listens for mouse events to trigger either 
   * a dungeon transition (next floor) or an exit from the room (back to map).
   *
   * @param ui The main user interface controller containing the game state.
   */
	public static void runExitRoom(UserInterface ui) {
		Application.run(Color.BLACK, context ->{
			var loader = new ImageLoader("data/img");
			var view = new ExitRoomView(loader);
			var hero = ui.hero();
			
			while(hero.isAlive()) {
				var event = context.pollOrWaitEvent(10);
				view.draw(context);
				if(event == null) {
					continue;
				}
				switch(event) {
				case PointerEvent pe ->{
					if(pe.action() == PointerEvent.Action.POINTER_DOWN) {
						var screen = context.getScreenInfo();
						var loc = pe.location();
						if(view.isCLickOnDoor(loc.x(), loc.y(), screen.width(), screen.height())) {
							ui.dungeonTransition();
							context.dispose();
							return;
						}
						if(view.isClickOnBackButton(loc.x(), loc.y())) {
							context.dispose();
							return;
						}
						
					}
				}default -> {
					
				}
				}
			}
		});
	}
}