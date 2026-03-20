package fr.uge.controller;

import java.io.IOException;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.PointerEvent;
import com.github.forax.zen.KeyboardEvent;
import fr.uge.characters.HallOfFame;
import fr.uge.view.HallOfFameView;
import fr.uge.view.ImageLoader;

/**
 * Controller for displaying the Hall of Fame (High Scores).
 */
public class HallOfFameController {

	/**
   * Displays the Hall of Fame screen.
   * This method blocks the execution loop until the user clicks the "Back" button
   * or presses the quit key.
   *
   * @param ctx    The application context used for event polling and rendering.
   * @param loader The image loader to retrieve background assets.
   * @throws IOException If an error occurs while reading the high scores from the file.
   */
  public void show(ApplicationContext ctx, ImageLoader loader) throws IOException {
    Objects.requireNonNull(ctx);
    Objects.requireNonNull(loader);
    var hof = new HallOfFame();
    var screen = ctx.getScreenInfo();
    var view = HallOfFameView.create(screen.width(), screen.height(), loader, hof.getScoresForDisplay());
    loop(ctx, view);
  }

  /**
   * Main loop for the Hall of Fame screen.
   *
   * @param ctx  The application context.
   * @param view The Hall of Fame view.
   */
  private void loop(ApplicationContext ctx, HallOfFameView view) {
    while (true) {
      var event = ctx.pollOrWaitEvent(10);
      if (event != null && !handleEvent(event, view)) {
        return;
      }
      HallOfFameView.draw(ctx, view);
    }
  }

  /**
   * Handles incoming events.
   *
   * @param event The event.
   * @param view  The view.
   * @return true to continue, false to exit.
   */
  private boolean handleEvent(Event event, HallOfFameView view) {
    return switch (event) {
      case PointerEvent pe -> handlePointer(pe, view);
      case KeyboardEvent ke -> handleKeyboard(ke);
      default -> true;
    };
  }

  /**
   * Handles pointer clicks (Back button).
   *
   * @param pe   The pointer event.
   * @param view The view.
   * @return false if back button clicked, true otherwise.
   */
  private boolean handlePointer(PointerEvent pe, HallOfFameView view) {
    if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
      var x = pe.location().x();
      var y = pe.location().y();
      if (view.backButton().contains(x, y)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Handles keyboard inputs.
   *
   * @param ke The keyboard event.
   * @return false if 'Q' is pressed, true otherwise.
   */
  private boolean handleKeyboard(KeyboardEvent ke) {
    if (ke.action() == KeyboardEvent.Action.KEY_PRESSED && ke.key() == KeyboardEvent.Key.Q) {
      return false;
    }
    return true;
  }
}