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
import fr.uge.characters.Hero;
import fr.uge.dongeon.ShopRoom;
import fr.uge.items.Item;
import fr.uge.items.ItemEmpty;
import fr.uge.items.Slot;

public record ShopRoomView(ImageLoader loader, int width, int height) {

	private static final List<String> CONTROLS_TEXT = List.of(
	   "CONTROLES :",
	   "[Clic souris] : Acheter / Poser",
	   "[Touche R]    : Pivoter l'objet",
	   "[Touche S]    : Vendre",
	   "[Touche Q]    : Quitter"
	);
  private static final int TILE_SIZE = 80;
  private static final int SHOP_X = 50;
  private static final int SHOP_Y = 150;

  public ShopRoomView {
    Objects.requireNonNull(loader);
    if (width < 0 || height < 0) throw new IllegalArgumentException("Invalid dimensions");
  }

  /**
   * Static entry point to render the complete Shop Room scene.
   *
   * @param ctx   The application context for graphics rendering.
   * @param view  The view instance containing display settings.
   * @param shop  The shop model containing the merchant's items.
   * @param hero  The hero model (used for gold balance and backpack).
   * @param held  The item currently held by the mouse cursor (null if none).
   * @param cur   The current cursor position (for drawing the floating item).
   */
  public static void draw(ApplicationContext ctx, ShopRoomView view, ShopRoom shop, Hero hero, Item held, Coordinate cur) {
    Objects.requireNonNull(ctx);
    ctx.renderFrame(g -> view.render(g, shop, hero, held, cur));
  }

  private void render(Graphics2D g, ShopRoom shop, Hero hero, Item held, Coordinate cur) {
    drawBackground(g);
    drawHeader(g, hero);
    drawShopGrid(g, shop);
    drawBackPack(g, hero.backpack());
    drawButton(g);
    drawControls(g);
    if (held != null) drawFloatingItem(g, held, cur);
  }

  private void drawBackground(Graphics2D g) {
    g.setColor(Color.BLACK);
    g.fill(new Rectangle2D.Float(0, 0, width, height));
    var bg = loader.get("shop"); 
    if (bg != null) g.drawImage(bg, 0, 0, width, height, null);
  }

  private void drawHeader(Graphics2D g, Hero hero) {
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 30));
    g.drawString("MAGASIN", SHOP_X, 80);
    g.drawString("Votre Or : " + hero.backpack().getGold(), getBagX(), 80);
    g.setFont(new Font("Arial", Font.BOLD, 20));
    g.drawString("Inventaire du Marchand", SHOP_X, SHOP_Y - 20);
    g.drawString("Votre Sac", getBagX(), getBagY() - 20);
  }

  private void drawShopGrid(Graphics2D g, ShopRoom shop) {
    for (int y = 0; y < 2; y++) {
      for (int x = 0; x < 5; x++) {
        var coord = new Coordinate(y, x); 
        drawSlotAt(g, SHOP_X, SHOP_Y, x, y);
        var item = shop.getItem(coord);
        drawShopItem(g, item, x, y);
      }
    }
  }

  private void drawShopItem(Graphics2D g, Item item, int x, int y) {
    switch (item) {
      case ItemEmpty _, Slot _ -> { }
      default -> {
        var bounds = new Rectangle2D.Float(SHOP_X + x * TILE_SIZE + 2, SHOP_Y + y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        drawImage(g, item, bounds, item.rotation());
        drawPrice(g, item, bounds);
      }
    }
  }

  private void drawPrice(Graphics2D g, Item item, Rectangle2D.Float bounds) {
    g.setColor(Color.YELLOW);
    g.setFont(new Font("Arial", Font.BOLD, 12));
    g.drawString(Item.buyingPrice(item) + " G", (int)bounds.x + 5, (int)bounds.y + 15);
  }

  private void drawBackPack(Graphics2D g, BackPack bp) {
    bp.items().keySet().forEach(c -> drawSlotAt(g, getBagX(), getBagY(), c.x(), c.y()));
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

  private void drawSlotAt(Graphics2D g, int originX, int originY, int x, int y) {
    var box = new Rectangle2D.Float(originX + x * TILE_SIZE, originY + y * TILE_SIZE, TILE_SIZE - 2, TILE_SIZE - 2);
    g.setColor(new Color(50, 50, 50, 200));
    g.fill(box);
    g.setColor(Color.GRAY);
    g.draw(box);
  }

  private Rectangle2D.Float getBagItemBounds(BackPack bp, Coordinate ref) {
    var occ = bp.getCoordinates(ref);
    int minX = min(occ, true), minY = min(occ, false);
    int w = (max(occ, true) - minX + 1) * TILE_SIZE - 4;
    int h = (max(occ, false) - minY + 1) * TILE_SIZE - 4;
    return new Rectangle2D.Float(getBagX() + minX * TILE_SIZE + 2, getBagY() + minY * TILE_SIZE + 2, w, h);
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
    var tx = new AffineTransform();
    tx.translate(box.getCenterX(), box.getCenterY());
    tx.rotate(Math.toRadians(rot * 90));
    double s = Math.min(box.width / (rot % 2 != 0 ? img.getHeight() : img.getWidth()), 
                        box.height / (rot % 2 != 0 ? img.getWidth() : img.getHeight()));
    tx.scale(s, s);
    tx.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
    g.drawImage(img, tx, null);
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
  private int getBagX() { return Math.max(SHOP_X + 5 * TILE_SIZE + 50, (width - 3 * TILE_SIZE) / 2 + 100); }
  private int getBagY() { return SHOP_Y; }
  
  /**
   * Checks if the mouse coordinates (x, y) are hovering over the "FINISHED" button.
   *
   * @param x The mouse X position.
   * @param y The mouse Y position.
   * @return true if the mouse is over the button, false otherwise.
   */
  public boolean isOverButton(int x, int y) { return getButtonBounds().contains(x, y); }
  private Rectangle2D.Float getButtonBounds() { return new Rectangle2D.Float((width - 200) / 2f, height - 100, 200, 60); }
  
  
  public Coordinate toShopCoordinate(int x, int y) { return toGrid(x, y, SHOP_X, SHOP_Y, 5, 2); }
  
  /**
   * Converts screen mouse coordinates into shop grid coordinates.
   *
   * @param x The mouse X position on screen.
   * @param y The mouse Y position on screen.
   * @return The corresponding coordinate in the shop grid, or null if outside.
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
  
//--- MÉTHODES DE L'ATH (HUD) ---
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