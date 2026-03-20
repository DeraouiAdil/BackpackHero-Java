package fr.uge.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import fr.uge.characters.Hero;

/**
 * Displays the Sanctuary/Heal Room interface.
 * <p>
 * Provides options for the hero to heal using gold or remove curses.
 * </p>
 * * @param loader The image loader.
 * @param width  The screen width.
 * @param height The screen height.
 */
public record HealRoomView(ImageLoader loader, int width, int height) {
  private static final int BTN_W = 300;
  private static final int BTN_H = 60;
  private static final int BTN_X = 350;

  /**
   * Compact constructor with validation.
   * * @param loader The image loader.
   * @param width  Screen width.
   * @param height Screen height.
   * @throws NullPointerException if loader is null.
   * @throws IllegalArgumentException if dimensions are negative.
   */
  public HealRoomView {
    Objects.requireNonNull(loader);
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Invalid dimensions");
    }
  }

  /**
   * Static entry point to render the Heal Room interface.
   *
   * @param ctx   The application context for graphics rendering.
   * @param view  The view instance containing display settings.
   * @param hero  The hero model (to display current HP and Gold).
   */
  public static void draw(ApplicationContext ctx, HealRoomView view, Hero hero) {
    Objects.requireNonNull(ctx);
    Objects.requireNonNull(hero);
    ctx.renderFrame(g -> view.render(g, hero));
  }

  private void render(Graphics2D g, Hero hero) {
    g.setColor(Color.BLACK);
    g.fill(new Rectangle2D.Float(0, 0, width, height));
    drawBackground(g);
    drawHeader(g, hero);
    drawButton(g, 200, "Leave (No Heal)");
    drawButton(g, 300, "Heal 25 PV (5 Gold)");
    drawButton(g, 400, "Heal 50 PV (10 Gold)");
    drawButton(g, 500 , "Cure Curse (15 Gold)");
  }

  private void drawBackground(Graphics2D g) {
    // CORRECTION : On demande "heal" (sans extension), ImageLoader trouvera "heal.jpg" ou "heal.png"
    var img = loader.get("heal");
    if (img != null) {
      g.drawImage(img, 0, 0, width, height, null);
    }
    // Voile sombre pour lisibilité du texte
    g.setColor(new Color(0, 0, 0, 150));
    g.fill(new Rectangle2D.Float(0, 0, width, height));
  }

  private void drawHeader(Graphics2D g, Hero hero) {
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 40));
    g.drawString("SANCTUAIRE DE SOIN", (width - 450) / 2, 80);
    g.setFont(new Font("Arial", Font.PLAIN, 25));
    var stats = "Héros : " + hero.pv() + " PV | " + hero.backpack().getGold() + " Or";
    g.drawString(stats, (width - 300) / 2, 130);
  }

  private void drawButton(Graphics2D g, int y, String text) {
    var box = new Rectangle2D.Float(BTN_X, y, BTN_W, BTN_H);
    g.setColor(new Color(50, 150, 50));
    g.fill(box);
    g.setColor(Color.WHITE);
    g.draw(box);
    var metrics = g.getFontMetrics();
    int tx = BTN_X + (BTN_W - metrics.stringWidth(text)) / 2;
    int ty = y + (BTN_H + metrics.getAscent()) / 2 - 5;
    g.drawString(text, tx, ty);
  }
  
  /**
   * Determines which button (if any) was clicked based on mouse coordinates.
   *
   * @param x The mouse X coordinate.
   * @param y The mouse Y coordinate.
   * @return An integer representing the selected action:
   * <ul>
   * <li>0 for Leave</li>
   * <li>1 for Heal 25 HP</li>
   * <li>2 for Heal 50 HP</li>
   * <li>3 for Cure Curse</li>
   * <li>-1 if no button was clicked</li>
   * </ul>
   */
  public int getSelection(int x, int y) {
    if (x < BTN_X || x > BTN_X + BTN_W) return -1;
    if (y >= 200 && y <= 260) return 0;
    if (y >= 300 && y <= 360) return 1;
    if (y >= 400 && y <= 460) return 2;
    if(y >= 500 && y <= 560) return 3;
    return -1;
  }
}