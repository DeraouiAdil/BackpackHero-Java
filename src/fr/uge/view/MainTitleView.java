package fr.uge.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;

public record MainTitleView(int width, int height, ImageLoader loader, List<MenuButton> buttons) {

	/**
   * Represents a clickable button on the main menu.
   *
   * @param label  The text displayed inside the button.
   * @param color  The background color of the button.
   * @param bounds The geometric bounds (hitbox) of the button.
   */
  public record MenuButton(String label, Color color, Rectangle2D.Float bounds) {}

  public MainTitleView {
    Objects.requireNonNull(loader);
    Objects.requireNonNull(buttons);
  }

  
  /**
   * Factory method to initialize the main menu with standard buttons layout.
   * It calculates the position of the "JOUER", "RÈGLES", "HALL OF FAME", and "QUITTER" buttons
   * based on the screen dimensions.
   *
   * @param width  The width of the screen.
   * @param height The height of the screen.
   * @param loader The image loader.
   * @return A new instance of MainTitleView with pre-configured buttons.
   */
  public static MainTitleView create(int width, int height, ImageLoader loader) {
    int btnWidth = 300;
    int btnHeight = 60;
    int gap = 20;
    float startY = height / 2f + 20; 
    float centerX = (width - btnWidth) / 2f;

    var btnPlay = new MenuButton("JOUER", Color.GREEN, 
        new Rectangle2D.Float(centerX, startY, btnWidth, btnHeight));
    
    var btnRules = new MenuButton("RÈGLES", Color.GRAY, 
        new Rectangle2D.Float(centerX, startY + (btnHeight + gap), btnWidth, btnHeight));
    
    // Changement ici : Color.GRAY pour correspondre à l'image fournie
    var btnHof = new MenuButton("HALL OF FAME", Color.YELLOW, 
        new Rectangle2D.Float(centerX, startY + (btnHeight + gap) * 2, btnWidth, btnHeight));
    
    var btnQuit = new MenuButton("QUITTER", Color.RED, 
        new Rectangle2D.Float(centerX, startY + (btnHeight + gap) * 3, btnWidth, btnHeight));

    return new MainTitleView(width, height, loader, List.of(btnPlay, btnRules, btnHof, btnQuit));
  }

  /**
   * Static entry point to render the main title screen.
   * It handles both the standard menu display and the pseudo input overlay if active.
   *
   * @param ctx           The application context for rendering.
   * @param view          The view instance containing buttons and assets.
   * @param isTyping      True if the user is currently entering their name (shows overlay).
   * @param currentPseudo The text currently typed by the user.
   */
  public static void draw(ApplicationContext ctx, MainTitleView view, boolean isTyping, String currentPseudo) {
    ctx.renderFrame(g -> {
        view.render(g);
        if (isTyping) {
            view.drawInputOverlay(g, currentPseudo);
        }
    });
  }

  private void render(Graphics2D g) {
    drawBackground(g);
    drawButtons(g);
  }

  private void drawBackground(Graphics2D g) {
    BufferedImage bgImage = loader.get("MAINTITLE");

    if (bgImage != null) {
      g.drawImage(bgImage, 0, 0, width, height, null);
    } else {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, width, height);
      
      g.setColor(Color.WHITE);
      g.setFont(new Font("Serif", Font.BOLD, 40));
      String title = "BACKPACK HERO";
      FontMetrics fm = g.getFontMetrics();
      int x = (width - fm.stringWidth(title)) / 2;
      g.drawString(title, x, height / 3);
    }
  }

  private void drawButtons(Graphics2D g) {
    g.setFont(new Font("SansSerif", Font.BOLD, 20));
    for (var btn : buttons) {
      g.setColor(new Color(30, 30, 30, 200));
      g.fillRoundRect((int) btn.bounds.x + 5, (int) btn.bounds.y + 5, (int) btn.bounds.width, (int) btn.bounds.height, 20, 20);
      g.setColor(btn.color);
      g.fillRoundRect((int) btn.bounds.x, (int) btn.bounds.y, (int) btn.bounds.width, (int) btn.bounds.height, 20, 20);
      g.setColor(Color.WHITE);
      g.drawRoundRect((int) btn.bounds.x, (int) btn.bounds.y, (int) btn.bounds.width, (int) btn.bounds.height, 20, 20);
      g.setColor(Color.BLACK);
      if (btn.color == Color.RED || btn.color.equals(new Color(60, 60, 60)) || btn.color == Color.GRAY) { 
          g.setColor(Color.WHITE);
      }
      drawCenteredString(g, btn.label, btn.bounds, 0);
    }
  }

  private void drawCenteredString(Graphics2D g, String text, Rectangle2D.Float rect, int yOffset) {
    FontMetrics metrics = g.getFontMetrics(g.getFont());
    int x = (int) (rect.x + (rect.width - metrics.stringWidth(text)) / 2);
    int y = (int) (rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent()) + yOffset;
    g.drawString(text, x, y);
  }

  /**
   * Identifies which button (if any) is located at the specified screen coordinates.
   * Useful for handling mouse clicks.
   *
   * @param x The mouse X coordinate.
   * @param y The mouse Y coordinate.
   * @return The label of the button under the mouse, or null if no button is hit.
   */
  public String getButtonAt(float x, float y) {
    for (var btn : buttons) {
      if (btn.bounds.contains(x, y)) {
        return btn.label;
      }
    }
    return null;
  }
  
  private void drawInputOverlay(Graphics2D g, String currentPseudo) {//entrer son pseudo
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(0, 0, width, height);

    int boxW = 400;
    int boxH = 200;
    int boxX = (width - boxW) / 2;
    int boxY = (height - boxH) / 2;

    drawDialogBackground(g, boxX, boxY, boxW, boxH);
    drawDialogContent(g, boxX, boxY, boxW, currentPseudo);
  }

  private void drawDialogBackground(Graphics2D g, int x, int y, int w, int h) {
    g.setColor(Color.DARK_GRAY);
    g.fillRoundRect(x, y, w, h, 20, 20);
    g.setColor(Color.WHITE);
    g.drawRoundRect(x, y, w, h, 20, 20);
  }

  private void drawDialogContent(Graphics2D g, int x, int y, int w, String pseudo) {
    var titleFont = new Font("SansSerif", Font.BOLD, 24);
    drawCenteredText(g, "Entrez votre pseudo :", x, y + 60, w, titleFont, Color.WHITE);

    drawInputField(g, x + 50, y + 90, pseudo);

    var helpFont = new Font("SansSerif", Font.ITALIC, 14);
    var helpMsg = "Espace pour valider / Echap pour annuler";
    drawCenteredText(g, helpMsg, x, y + 180, w, helpFont, Color.LIGHT_GRAY);
  }

  private void drawInputField(Graphics2D g, int x, int y, String pseudo) {
    g.setColor(Color.BLACK);
    g.fillRect(x, y, 300, 50);
    g.setColor(Color.WHITE);
    g.drawRect(x, y, 300, 50);

    g.setFont(new Font("Monospaced", Font.PLAIN, 24));
    g.drawString(pseudo + "_", x + 10, y + 35);
  }

  private void drawCenteredText(Graphics2D g, String text, int x, int y, int w, Font f, Color c) {
    g.setFont(f);
    g.setColor(c);
    int textX = x + (w - g.getFontMetrics().stringWidth(text)) / 2;
    g.drawString(text, textX, y);
  }
 
}