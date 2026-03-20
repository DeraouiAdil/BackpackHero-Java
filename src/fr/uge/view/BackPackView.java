package fr.uge.view;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.items.*;
import com.github.forax.zen.ApplicationContext;

/**
 * Handles the graphical presentation of the player's Backpack (Inventory).
 * <p>
 * This view draws the grid, the items placed within it, the bottom item dock,
 * and handles the rendering of items currently being dragged (floating).
 * </p>
 * * @param xOrigin   The screen X coordinate where the grid starts.
 * @param yOrigin   The screen Y coordinate where the grid starts.
 * @param width     The total width of the view area.
 * @param height    The total height of the view area.
 * @param tileSize  The size in pixels of a single grid square.
 * @param loader    The image loader.
 */
public record BackPackView(int xOrigin, int yOrigin, int width, int height, int tileSize, ImageLoader loader) {
  private static final int DOCK_H = 120;
  private static final List<String> TEXTS = List.of("CONTROLES :", "[Clic G] : Action", "[R] : Pivoter", "[Q] : Quitter");

  /**
   * Compact constructor with validation.
   * * @param xOrigin   X start.
   * @param yOrigin   Y start.
   * @param width     View width.
   * @param height    View height.
   * @param tileSize  Size of a tile.
   * @param loader    The image loader.
   * @throws NullPointerException if loader is null.
   * @throws IllegalArgumentException if dimensions or tileSize are not positive.
   */
  public BackPackView {
    Objects.requireNonNull(loader);
    if (tileSize <= 0 || width < 0 || height < 0) throw new IllegalArgumentException("Invalid dims");
  }

  /**
   * Factory method to create a centered BackPackView based on the current screen info.
   *
   * @param ctx    The application context.
   * @param loader The image loader.
   * @return A new instance of BackPackView centered on the screen.
   */
  public static BackPackView create(ApplicationContext ctx, ImageLoader loader) {
    var info = ctx.getScreenInfo();
    int size = 80;
    return new BackPackView((info.width() - 3 * size) / 2, (info.height() - 3 * size) / 2, info.width(), info.height(), size, loader);
  }

  /**
   * Static entry point to render the Backpack interface.
   *
   * @param ctx   The application context for graphics rendering.
   * @param bp    The backpack model containing items.
   * @param view  The view settings (dimensions, origin).
   * @param held  The item currently held by the mouse cursor (floating), or null.
   * @param cur   The current grid coordinate under the mouse cursor.
   * @param dock  The list of items available in the bottom dock.
   */
  public static void draw(ApplicationContext ctx, BackPack bp, BackPackView view, Item held, Coordinate cur, List<Item> dock) {
    Objects.requireNonNull(ctx);
    Objects.requireNonNull(bp);
    ctx.renderFrame(g -> view.render(g, bp, held, cur, dock));
  }

  private void render(Graphics2D g, BackPack bp, Item held, Coordinate cur, List<Item> dock) {
    Objects.requireNonNull(cur); Objects.requireNonNull(dock);
    drawBackground(g);
    drawGrid(g, bp);
    drawBagItems(g, bp);
    drawDock(g, dock);
    drawControls(g);
    if (held != null) drawFloatingItem(g, held, cur);
  }

  private void drawBackground(Graphics2D g) {
    g.setColor(Color.BLACK);
    g.fill(new Rectangle2D.Float(0, 0, width, height));
    drawImageInBox(g, loader.get("background"), new Rectangle2D.Float(0, 0, width, height), 0);
  }

  private void drawGrid(Graphics2D g, BackPack bp) {
    bp.items().keySet().forEach(c -> drawSlot(g, c));
  }

  private void drawSlot(Graphics2D g, Coordinate c) {
    var box = new Rectangle2D.Float(xOrigin + c.x() * tileSize, yOrigin + c.y() * tileSize, tileSize - 2, tileSize - 2);
    g.setColor(new Color(50, 50, 50, 200));
    g.fill(box);
    g.setColor(Color.GRAY);
    g.draw(box);
  }

  private void drawBagItems(Graphics2D g, BackPack bp) {
    var drawnIds = new HashSet<java.util.UUID>();
    bp.items().forEach((c, item) -> drawItemIfNew(g, bp, c, item, drawnIds));
  }

  private void drawItemIfNew(Graphics2D g, BackPack bp, Coordinate c, Item item, Set<java.util.UUID> ids) {
    switch (item) {
      case ItemEmpty _, Slot _ -> {}
      default -> renderActualItem(g, bp, c, item, ids);
    }
  }

  private void renderActualItem(Graphics2D g, BackPack bp, Coordinate c, Item item, Set<java.util.UUID> ids) {
    if (ids.contains(item.id())) return;
    drawImageInBox(g, loader.get(item.name()), calculateItemBounds(bp, c), item.rotation());
    ids.add(item.id());
  }

  private Rectangle2D.Float calculateItemBounds(BackPack bp, Coordinate ref) {
    var list = bp.getCoordinates(ref);
    int minX = list.stream().mapToInt(Coordinate::x).min().orElse(0);
    int minY = list.stream().mapToInt(Coordinate::y).min().orElse(0);
    int w = (list.stream().mapToInt(Coordinate::x).max().orElse(0) - minX + 1) * tileSize - 4;
    int h = (list.stream().mapToInt(Coordinate::y).max().orElse(0) - minY + 1) * tileSize - 4;
    return new Rectangle2D.Float(xOrigin + minX * tileSize + 2, yOrigin + minY * tileSize + 2, w, h);
  }

  private void drawDock(Graphics2D g, List<Item> items) {
    int y = height - DOCK_H;
    g.setColor(new Color(30, 30, 30, 220));
    g.fill(new Rectangle2D.Float(0, y, width, DOCK_H));
    int x = 20;
    for (var item : items) {
      drawDockSlot(g, item, x, y + 20);
      x += tileSize + 10;
    }
  }

  private void drawDockSlot(Graphics2D g, Item item, int x, int y) {
    var box = new Rectangle2D.Float(x, y, tileSize, tileSize);
    g.setColor(new Color(60, 60, 60));
    g.fill(box);
    g.setColor(Color.LIGHT_GRAY);
    g.draw(box);
    drawImageInBox(g, loader.get(item.name()), box, 0);
  }

  private void drawFloatingItem(Graphics2D g, Item item, Coordinate cur) {
    var list = item.size();
    int minX = list.stream().mapToInt(Coordinate::x).min().orElse(0) + cur.x();
    int minY = list.stream().mapToInt(Coordinate::y).min().orElse(0) + cur.y();
    int w = list.stream().mapToInt(Coordinate::x).max().orElse(0) - minX + cur.x() + 1;
    int h = list.stream().mapToInt(Coordinate::y).max().orElse(0) - minY + cur.y() + 1;
    
    var box = new Rectangle2D.Float(xOrigin + minX * tileSize, yOrigin + minY * tileSize, w * tileSize, h * tileSize);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
    drawImageInBox(g, loader.get(item.name()), box, item.rotation());
    g.setComposite(AlphaComposite.SrcOver);
  }

  private void drawImageInBox(Graphics2D g, BufferedImage img, Rectangle2D.Float box, int rot) {
    if (img == null) return;
    var trans = new AffineTransform();
    trans.translate(box.x + box.width / 2.0, box.y + box.height / 2.0);
    trans.rotate(Math.toRadians(rot * 90));
    double s = (rot % 2 != 0) ? Math.min(box.width / img.getHeight(), box.height / img.getWidth()) 
                              : Math.min(box.width / img.getWidth(), box.height / img.getHeight());
    trans.scale(s, s);
    trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
    g.drawImage(img, trans, null);
  }

  /**
   * Converts screen mouse coordinates into Backpack grid coordinates.
   *
   * @param x The mouse X position on the screen.
   * @param y The mouse Y position on the screen.
   * @return The corresponding coordinate in the backpack grid.
   */
  public Coordinate toCoordinate(float x, float y) {
    
  	int gridX = (int)Math.floor((x - xOrigin) / tileSize);
  	int gridY = (int)Math.floor((y - yOrigin) / tileSize);
  	return new Coordinate(gridX, gridY);
  }

  /**
   * Checks if the mouse Y position is within the bottom dock area.
   *
   * @param y The mouse Y position.
   * @return true if the cursor is over the dock.
   */
  public boolean isCursorInDock(float y) { return y > height - DOCK_H; }

  /**
   * Retrieves the item located at the specific screen coordinates within the dock.
   *
   * @param x     The mouse X position.
   * @param y     The mouse Y position.
   * @param items The list of items currently in the dock.
   * @return The item under the cursor, or null if none found.
   */
  public Item getItemAtDock(float x, float y, List<Item> items) {
    Objects.requireNonNull(items);
    if (!isCursorInDock(y)) return null;
    int curX = 20;
    for (var item : items) {
      if (x >= curX && x <= curX + tileSize) return item;
      curX += tileSize + 10;
    }
    return null;
  }
  
  private void drawControls(Graphics2D g) {
    g.setFont(new Font("SansSerif", Font.BOLD, 14));
    int w = 220, h = TEXTS.size() * g.getFontMetrics().getHeight() + 20;
    var box = new Rectangle2D.Float(width - w - 10, height - h - 10, w, h);
    g.setColor(new Color(20, 20, 20, 200));
    g.fillRoundRect((int)box.x, (int)box.y, (int)box.width, (int)box.height, 15, 15);
    g.setColor(Color.WHITE);
    int y = (int)box.y + 20;
    for (var t : TEXTS) {
      g.drawString(t, (int)box.x + 15, y);
      y += g.getFontMetrics().getHeight();
    }
  }
  
  
}