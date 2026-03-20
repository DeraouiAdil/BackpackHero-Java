package fr.uge.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;

/**
 * Represents the view for the Hall of Fame screen.
 * <p>
 * Displays the list of top scores and provides a button to return to the main menu.
 * </p>
 * * @param width       The width of the screen.
 * @param height      The height of the screen.
 * @param loader      The image loader for backgrounds.
 * @param scores      The list of score strings to display.
 * @param backButton  The geometric bounds of the "Return" button.
 */
public record HallOfFameView(int width, int height, ImageLoader loader, List<String> scores, Rectangle2D.Float backButton) {

  /**
   * Compact constructor that validates inputs.
   * @param width       The width of the screen.
   * @param height      The height of the screen.
   * @param loader      The image loader.
   * @param scores      The list of scores.
   * @param backButton  The back button rectangle.
   * @throws NullPointerException if loader, scores, or backButton are null.
   * @throws IllegalArgumentException if dimensions are not positive.
   */
  public HallOfFameView {
    Objects.requireNonNull(loader);
    Objects.requireNonNull(scores);
    Objects.requireNonNull(backButton);
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Dimensions must be positive");
    }
  }

  /**
   * Factory method to create a HallOfFameView.
   * <p>
   * It automatically calculates the position of the "RETOUR" (Back) button centered at the bottom of the screen.
   * </p>
   *
   * @param width  The width of the screen.
   * @param height The height of the screen.
   * @param loader The image loader.
   * @param scores The list of scores to display.
   * @return A new instance of HallOfFameView with the configured back button.
   */
  public static HallOfFameView create(int width, int height, ImageLoader loader, List<String> scores) {
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid dimensions");
    Objects.requireNonNull(loader);
    Objects.requireNonNull(scores);
    var btnW = 200f;
    var btnH = 50f;
    var backBtn = new Rectangle2D.Float((width - btnW) / 2, height - 100, btnW, btnH);
    return new HallOfFameView(width, height, loader, scores, backBtn);
  }

  /**
   * Static entry point to render the Hall of Fame screen.
   *
   * @param ctx  The application context for graphics rendering.
   * @param view The view instance containing the scores and layout data.
   */
  public static void draw(ApplicationContext ctx, HallOfFameView view) {
    Objects.requireNonNull(ctx);
    Objects.requireNonNull(view);
    ctx.renderFrame(g -> view.render(g));
  }

  private void render(Graphics2D g) {
    drawBackground(g);
    drawOverlay(g);
    drawTitle(g);
    drawScores(g);
    drawBackButton(g);
  }

  private void drawBackground(Graphics2D g) {
    var bg = loader.get("MAINTITLE");
    if (bg != null) {
      g.drawImage(bg, 0, 0, width, height, null);
    } else {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, width, height);
    }
  }

  private void drawOverlay(Graphics2D g) {
    g.setColor(new Color(0, 0, 0, 200));
    g.fillRect(0, 0, width, height);
  }

  private void drawTitle(Graphics2D g) {
    g.setColor(Color.ORANGE);
    g.setFont(new Font("Serif", Font.BOLD, 50));
    drawCenteredString(g, "HALL OF FAME", width / 2, 80);
  }

  private void drawScores(Graphics2D g) {
    g.setFont(new Font("Monospaced", Font.BOLD, 24));
    g.setColor(Color.WHITE);
    var y = 160;
    if (scores.isEmpty()) {
      drawCenteredString(g, "Aucun score enregistré.", width / 2, y);
      return;
    }
    var rank = 1;
    for (var line : scores) {
      drawCenteredString(g, String.format("%2d. %s", rank++, line), width / 2, y);
      y += 40;
    }
  }

  private void drawBackButton(Graphics2D g) {
    g.setColor(new Color(180, 180, 180));
    g.fill(backButton);
    g.setColor(Color.WHITE);
    g.draw(backButton);
    g.setColor(Color.BLACK);
    g.setFont(new Font("SansSerif", Font.BOLD, 20));
    drawCenteredString(g, "RETOUR", (int) (backButton.x + backButton.width / 2), (int) (backButton.y + 32));
  }

  private void drawCenteredString(Graphics2D g, String text, int x, int y) {
    var fm = g.getFontMetrics();
    g.drawString(text, x - fm.stringWidth(text) / 2, y);
  }
}