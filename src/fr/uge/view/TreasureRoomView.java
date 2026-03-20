package fr.uge.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.github.forax.zen.ApplicationContext;
import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.dongeon.TreasureRoom;
import fr.uge.items.Item;
import fr.uge.items.ItemEmpty;
import fr.uge.items.Slot;

/**
 * Handles the graphical display of the Treasure Room.
 * <p>
 * Displays the treasure chest inventory on the left and the hero's backpack on the right,
 * allowing for item transfer. It supports two states: Closed (Chest locked) and Open (Inventory view).
 * </p>
 * @param loader The image loader.
 * @param width  Screen width.
 * @param height Screen height.
 */
public record TreasureRoomView(ImageLoader loader, int width, int height) {
	private static final List<String> CONTROLS_TEXT = List.of(
	    "CONTROLES :",
	    "[Clic Gauche] : Prendre / Poser",
	    "[Touche R]    : Pivoter l'objet",
	    "[Touche Q]    : Quitter"
	);
  private static final int TILE_SIZE = 80;
  private static final int CHEST_X = 50;
  private static final int CHEST_Y = 150;

  /**
   * Compact constructor to validate view parameters.
   * Checks that the loader is not null and that dimensions are non-negative.
   */
  public TreasureRoomView {
    Objects.requireNonNull(loader);
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Invalid dimensions");
    }
  }

  
  /**
   * Static entry point to draw the complete Treasure Room scene.
   *
   * @param ctx   The application context (for graphics rendering).
   * @param view  The view instance containing display settings.
   * @param room  The treasure room model (chest data).
   * @param bag   The hero's backpack model.
   * @param held  The item currently held by the cursor (null if none).
   * @param cur   The current cursor position (for the floating item).
   * @param open  Indicates if the chest is open (Inventory view) or closed (Title screen).
   */
  public static void draw(ApplicationContext ctx, TreasureRoomView view, TreasureRoom room, BackPack bag, Item held, Coordinate cur, boolean open) {
    Objects.requireNonNull(ctx);
    ctx.renderFrame(g -> view.render(g, room, bag, held, cur, open));
  }

  private void render(Graphics2D g, TreasureRoom room, BackPack bag, Item held, Coordinate cur, boolean open) {
    g.setColor(Color.BLACK);
    g.fill(new Rectangle2D.Float(0, 0, width, height));
    var bg = loader.get("treasure");
    if (bg != null) {
      g.drawImage(bg, 0, 0, width, height, null);
    }
    if (!open) {
      drawClosedState(g);
    } else {
      drawOpenState(g, room, bag, held, cur);
    }
    drawControls(g);
}

  private void drawClosedState(Graphics2D g) {
    g.setFont(new Font("Arial", Font.BOLD, 40));
    var msg = "CLICK ME !";
    var metrics = g.getFontMetrics();
    int x = (width - metrics.stringWidth(msg)) / 2;
    int y = height / 2;
    g.setColor(Color.BLACK); 
    g.drawString(msg, x + 2, y + 2);
    g.setColor(Color.ORANGE); 
    g.drawString(msg, x, y);
  }

  private void drawOpenState(Graphics2D g, TreasureRoom room, BackPack bag, Item held, Coordinate cur) {
    drawText(g, "COFFRE", CHEST_X, CHEST_Y - 20);
    drawText(g, "SAC À DOS", getBagX(), getBagY() - 20);
    drawChest(g, room);
    drawBackPack(g, bag);
    drawButton(g);
    if (held != null) {
      drawFloatingItem(g, held, cur);
    }
  }

  private void drawChest(Graphics2D g, TreasureRoom room) {
    for (int y = 0; y < 2; y++) {
      for (int x = 0; x < 3; x++) {
        var coord = new Coordinate(x, y);
        drawSlotAt(g, CHEST_X, CHEST_Y, coord);
        drawItemIfVisible(g, room.get(coord), CHEST_X, CHEST_Y, coord);
      }
    }
  }

  private void drawBackPack(Graphics2D g, BackPack bp) {
    bp.items().keySet().forEach(c -> drawSlotAt(g, getBagX(), getBagY(), c));
    var drawn = new HashSet<UUID>();
    bp.items().forEach((c, item) -> drawBackPackItem(g, bp, c, item, drawn));
  }

  private void drawBackPackItem(Graphics2D g, BackPack bp, Coordinate c, Item item, HashSet<UUID> drawn) {
    switch (item) {
      case ItemEmpty _, Slot _ -> { }
      default -> {
        if (!drawn.add(item.id())) return;
        var bounds = getBagItemBounds(bp, c);
        drawImage(g, item, bounds, item.rotation());
      }
    }
  }

  private void drawItemIfVisible(Graphics2D g, Item item, int x0, int y0, Coordinate c) {
    switch (item) {
      case ItemEmpty _, Slot _ -> { }
      default -> {
        var b = new Rectangle2D.Float(x0 + c.x() * TILE_SIZE + 2, y0 + c.y() * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        drawImage(g, item, b, item.rotation());
      }
    }
  }

  private Rectangle2D.Float getBagItemBounds(BackPack bp, Coordinate ref) {
    var occ = bp.getCoordinates(ref);
    int minX = min(occ, true);
    int minY = min(occ, false);
    int w = (max(occ, true) - minX + 1) * TILE_SIZE - 4;
    int h = (max(occ, false) - minY + 1) * TILE_SIZE - 4;
    return new Rectangle2D.Float(getBagX() + minX * TILE_SIZE + 2, getBagY() + minY * TILE_SIZE + 2, w, h);
  }

  private void drawSlotAt(Graphics2D g, int x0, int y0, Coordinate c) {
    var box = new Rectangle2D.Float(x0 + c.x() * TILE_SIZE, y0 + c.y() * TILE_SIZE, TILE_SIZE - 2, TILE_SIZE - 2);
    g.setColor(new Color(50, 50, 50, 200));
    g.fill(box);
    g.setColor(Color.GRAY);
    g.draw(box);
  }

  private void drawFloatingItem(Graphics2D g, Item item, Coordinate cursor) {
    var coords = item.size();
    int w = (max(coords, true) - min(coords, true) + 1) * TILE_SIZE;
    int h = (max(coords, false) - min(coords, false) + 1) * TILE_SIZE;
    var bounds = new Rectangle2D.Float(cursor.x() - TILE_SIZE / 2f, cursor.y() - TILE_SIZE / 2f, w, h);
    
    g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.7f));
    drawImage(g, item, bounds, item.rotation());
    g.setComposite(java.awt.AlphaComposite.SrcOver);
  }

  private void drawImage(Graphics2D g, Item item, Rectangle2D.Float box, int rot) {
    var img = loader.get(item.name());
    if (img == null) return;
    var trans = new AffineTransform();
    trans.translate(box.getCenterX(), box.getCenterY());
    trans.rotate(Math.toRadians(rot * 90));
    double s = (rot % 2 != 0) 
        ? Math.min(box.width / img.getHeight(), box.height / img.getWidth()) 
        : Math.min(box.width / img.getWidth(), box.height / img.getHeight());
    trans.scale(s, s);
    trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
    g.drawImage(img, trans, null);
  }

  private void drawText(Graphics2D g, String txt, int x, int y) {
    g.setColor(Color.WHITE); 
    g.setFont(new Font("Arial", Font.BOLD, 20)); 
    g.drawString(txt, x, y);
  }
  
  private void drawButton(Graphics2D g) {
    var b = getButtonBounds();
    g.setColor(new Color(100, 200, 100)); 
    g.fill(b);
    g.setColor(Color.BLACK); 
    g.draw(b);
    g.drawString("FINISHED", (int)b.x + 50, (int)b.y + 35);
  }

  private int min(List<Coordinate> l, boolean x) { return l.stream().mapToInt(c -> x ? c.x() : c.y()).min().orElse(0); }
  private int max(List<Coordinate> l, boolean x) { return l.stream().mapToInt(c -> x ? c.x() : c.y()).max().orElse(0); }
  private int getBagX() { return Math.max((width - 3 * TILE_SIZE) / 2, 310); }
  private int getBagY() { return (height - 3 * TILE_SIZE) / 2; }

  
  /**
   * Checks if the mouse coordinates (x, y) are hovering over the "FINISHED" button.
   *
   * @param x The mouse X position.
   * @param y The mouse Y position.
   * @return true if the mouse is over the button, false otherwise.
   */
  public boolean isOverButton(int x, int y) {
    return getButtonBounds().contains(x, y); 
  }
  private Rectangle2D.Float getButtonBounds() { 
    return new Rectangle2D.Float((width - 200) / 2f, height - 120, 200, 60); 
  }
  
  
  /**
   * Converts screen mouse coordinates into chest grid coordinates.
   *
   * @param x The mouse X position on screen.
   * @param y The mouse Y position on screen.
   * @return The corresponding coordinate in the chest grid, or null if outside.
   */
  public Coordinate toChestCoordinate(int x, int y) {
    return toGrid(x, y, CHEST_X, CHEST_Y, 3, 2); 
  }
  
  /**
   * Converts screen mouse coordinates into backpack grid coordinates.
   *
   * @param x The mouse X position on screen.
   * @param y The mouse Y position on screen.
   * @return The corresponding coordinate in the backpack grid.
   */
  public Coordinate toBagCoordinate(int x, int y) { 
    int gridX = (int)Math.floor((x - getBagX()) / (double)TILE_SIZE);
    int gridY = (int)Math.floor((y - getBagY()) / (double)TILE_SIZE);
    return new Coordinate(gridX, gridY);
  }
  
  private Coordinate toGrid(int mx, int my, int ox, int oy, int cols, int rows) {
    if (mx < ox || my < oy) return null;
    int gx = (mx - ox) / TILE_SIZE;
    int gy = (my - oy) / TILE_SIZE;
    return (gx < cols && gy < rows) ? new Coordinate(gx, gy) : null;
  }
  
	 private void drawControls(Graphics2D g) {
	   g.setFont(new Font("SansSerif", Font.BOLD, 14));
	   var box = getControlBox(g.getFontMetrics());
	   drawControlBackground(g, box);
	   drawControlText(g, box);
	 }

	private Rectangle2D.Float getControlBox(java.awt.FontMetrics fm) {
		int maxWidth = CONTROLS_TEXT.stream().mapToInt(fm::stringWidth).max().orElse(200);
		int lineHeight = fm.getHeight();
		int boxWidth = maxWidth + 30;
		int boxHeight = (CONTROLS_TEXT.size() * lineHeight) + 20;
		return new Rectangle2D.Float(width - boxWidth - 10, height - boxHeight - 10, 
		                              boxWidth, boxHeight);
	}

	private void drawControlBackground(Graphics2D g, Rectangle2D.Float box) {
		 int x = (int) box.x, y = (int) box.y;
		 int w = (int) box.width, h = (int) box.height;
		 
		 g.setColor(new Color(20, 20, 20, 200));
		 g.fillRoundRect(x, y, w, h, 15, 15);
		 g.setColor(new Color(100, 100, 100));
		 g.drawRoundRect(x, y, w, h, 15, 15);
	}

	private void drawControlText(Graphics2D g, Rectangle2D.Float box) {
		var fm = g.getFontMetrics();
		int x = (int) box.x + 15;
		int y = (int) box.y + fm.getAscent() + 10;
		 
		for (var line : CONTROLS_TEXT) {
		  g.setColor(line.startsWith("CONTROLES") ? Color.ORANGE : Color.WHITE);
		  g.drawString(line, x, y);
		  y += fm.getHeight();
		}
	}
	
}