package fr.uge.controller;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.EventModifier; // Assurez-vous d'avoir cet import
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.UserInterface;
import fr.uge.view.ImageLoader;
import fr.uge.view.MainTitleView;

/**
 * Controller for the Main Title Screen.
 * Handles menu navigation (Play, Rules, Hall of Fame, Quit) and player name input.
 */
public class MainTitleController {
	
  private boolean isInputtingName = false;
  private final StringBuilder pseudoBuffer = new StringBuilder();

  /**
   * Static entry point to launch the application.
   * It initializes the application context with a black background and starts the main event loop.
   */
  public static void run() {
    Application.run(Color.BLACK, ctx -> new MainTitleController().mainLoop(ctx));
  }

  /**
   * The main loop for the title screen.
   * Manages rendering and event dispatching.
   *
   * @param context The application context.
   */
  private void mainLoop(ApplicationContext context) {
    var loader = new ImageLoader("data/img");
    var screen = context.getScreenInfo();
    var view = MainTitleView.create(screen.width(), screen.height(), loader);

    while (true) {
      Event event = context.pollOrWaitEvent(10);
      if (event != null) {
        if(!handleEvent(event, context, view, loader)){
          return;
        }
      }
      MainTitleView.draw(context, view, isInputtingName, pseudoBuffer.toString());
    }
  }

  /**
   * Dispatches events based on type.
   *
   * @param event   The event.
   * @param ctx     The application context.
   * @param view    The title screen view.
   * @param loader  The image loader.
   * @return true to continue, false to exit application.
   */
  private boolean handleEvent(Event event, ApplicationContext ctx, MainTitleView view, ImageLoader loader) {
    return switch (event) {
      case PointerEvent pe -> handlePointer(pe, ctx, view, loader);
      case KeyboardEvent ke -> handleKeyboard(ke, ctx);
      default -> true;
    };
  }

  private void openRulesPdf(String path) {
  	if(!Desktop.isDesktopSupported()) {
  		return;
  	}
  	try {
  		File pdfFile = new File(path);
  		if(pdfFile.exists()) {
  			Desktop.getDesktop().open(pdfFile);
  		}else {
  		}
  	}catch(IOException e) {
  		System.out.println("Erreur lors de l'ouverture du PDF : " + e.getMessage());
  	}
  }
  /**
   * Handles mouse clicks on menu buttons.
   *
   * @param pe     The pointer event.
   * @param ctx    The application context.
   * @param view   The title view.
   * @param loader The image loader.
   * @return true to continue, false to exit.
   */
  private boolean handlePointer(PointerEvent pe, ApplicationContext ctx, MainTitleView view, ImageLoader loader) {
    if (isInputtingName) {
        return true; 
    }

    if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
      return true;
    }

    var location = pe.location();
    String clickedButton = view.getButtonAt(location.x(), location.y());
    if (clickedButton != null) {
      switch (clickedButton) {
        case "JOUER" -> {
          System.out.println("Action: Demande de pseudo");
          isInputtingName = true;
          pseudoBuffer.setLength(0);
        }
        case "RÈGLES" -> {
          System.out.println("Action: Afficher les règles");
          openRulesPdf("docs/user.pdf");
        }
        case "HALL OF FAME" -> {
          System.out.println("Action: Afficher les scores");
          try {
             new HallOfFameController().show(ctx, loader);
          } catch (IOException e) {
             System.err.println("Erreur: " + e.getMessage());
          }
        }
        case "QUITTER" -> {
          ctx.dispose();
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Handles keyboard events (Name input or Quitting).
   *
   * @param ke  The keyboard event.
   * @param ctx The application context.
   * @return true to continue, false to exit.
   */
  private boolean handleKeyboard(KeyboardEvent ke, ApplicationContext ctx) {//saisi du pseudo
    if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) {
      return true;
    }
    if (isInputtingName) {
      return handleNameInput(ke, ctx);
    }
    if (ke.key() == KeyboardEvent.Key.Q) {
      ctx.dispose();
      return false;
    }
    return true;
  }

  /**
   * Handles text input for the player's name.
   *
   * @param ke  The keyboard event.
   * @param ctx The application context.
   * @return true.
   */
  private boolean handleNameInput(KeyboardEvent ke, ApplicationContext ctx) {
    var key = ke.key();
    if (key == KeyboardEvent.Key.SPACE) {
      launchGame(ctx);
      return true;
    }
    if (key == KeyboardEvent.Key.ESCAPE) {
      isInputtingName = false;
      pseudoBuffer.setLength(0);
      return true;
    }
    appendCharacter(ke);
    return true;
  }

  /**
   * Launches the main game map with the entered pseudo.
   *
   * @param ctx The application context.
   */
  private void launchGame(ApplicationContext ctx) {
    var finalPseudo = pseudoBuffer.toString().trim();
    if (finalPseudo.isEmpty()) {
      finalPseudo = "Hero";
    }
    System.out.println("Lancement avec : " + finalPseudo);
    MapController.runMaps(ctx, new UserInterface(finalPseudo), 50);
    isInputtingName = false;
    pseudoBuffer.setLength(0);
  }

  /**
   * Appends a character to the pseudo buffer based on key press.
   * Handles letters and digits.
   *
   * @param ke The keyboard event.
   */
  private void appendCharacter(KeyboardEvent ke) {
    if (pseudoBuffer.length() >= 12) {
      return;
    }
    var keyName = ke.key().name();
    var isShift = ke.modifiers().contains(EventModifier.SHIFT);

    if (keyName.length() == 1) {
      processSingleChar(keyName.charAt(0), isShift);
    } else if (keyName.startsWith("DIGIT")) {
      pseudoBuffer.append(keyName.replace("DIGIT", ""));
    }
  }

  /**
   * Processes a single character input.
   *
   * @param c       The character.
   * @param isShift True if Shift is held (uppercase).
   */
  private void processSingleChar(char c, boolean isShift) {
    if (Character.isLetter(c)) {
      pseudoBuffer.append(isShift ? c : Character.toLowerCase(c));
    } else if (Character.isDigit(c)) {
      pseudoBuffer.append(c);
    }
  }
}