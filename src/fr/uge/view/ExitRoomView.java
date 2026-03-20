package fr.uge.view;

import java.awt.Color;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;

/**
 * Manages the view for the Exit Room (the end of a level/floor).
 */
public class ExitRoomView {
	private final ImageLoader loader;
	
	/**
   * Creates a new ExitRoomView with the specified image loader.
   *
   * @param loader The image loader to retrieve the background asset.
   * @throws NullPointerException if loader is null.
   */
	public ExitRoomView(ImageLoader loader) {
		this.loader = Objects.requireNonNull(loader);
	}
	
	/**
   * Renders the Exit Room scene.
   * <p>
   * Draws the background image and the navigation UI (Back button).
   * </p>
   *
   * @param ctx The application context for graphics rendering.
   */
	public void draw(ApplicationContext ctx) {
		ctx.renderFrame( g -> {
			var screen = ctx.getScreenInfo();
			var background = loader.get("exitRoom");
			if(background != null) {
				g.drawImage(background, 0, 0, screen.width(), screen.height(), null);
			}
			
			g.setColor(Color.RED);
			g.fillRect(20, 20, 100, 40);
			g.setColor(Color.WHITE);
			g.drawString("RETOUR", 40, 45);
		});
	}
	
	/**
   * Checks if the provided coordinates correspond to a click on the "Return" button.
   * <p>
   * The button is located at the top-left corner (20, 20) with dimensions 100x40.
   * </p>
   *
   * @param x The mouse X coordinate.
   * @param y The mouse Y coordinate.
   * @return true if the click is within the button's bounds, false otherwise.
   */
	public boolean isClickOnBackButton(float x, float y) {
		return x >= 20 && x <= 120 && y >= 20 && y <= 60;
	}
	
	/**
   * Checks if the provided coordinates correspond to a click on the exit door.
   * <p>
   * The door hit-box is calculated proportionally to the screen size (center area).
   * </p>
   *
   * @param x      The mouse X coordinate.
   * @param y      The mouse Y coordinate.
   * @param width  The total width of the screen.
   * @param height The total height of the screen.
   * @return true if the click is within the door's bounds, false otherwise.
   */
	public boolean isCLickOnDoor(float x, float y, int width, int height) {
		float doorLeft = width * 0.38f;
		float doorRight = width * 0.62f;
		float doorTop = height * 0.45f;
		float doorBottom = height * 0.95f;
		
		return x >= doorLeft && x <= doorRight && y >= doorTop && y <= doorBottom;
	}
}