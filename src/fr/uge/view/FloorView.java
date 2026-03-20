package fr.uge.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.ScreenInfo;
import fr.uge.Coordinate;
import fr.uge.dongeon.Floor;
import fr.uge.dongeon.TypeRoom;

/**
 * Renders the mini-map or floor map of the dungeon.
 * <p>
 * This view displays the grid of rooms, the hero's current position, and a legend
 * explaining the color codes for different room types.
 * </p>
 * * @param x      Number of horizontal rooms (grid width).
 * @param y      Number of vertical rooms (grid height).
 * @param Size   The size (in pixels) of a single room square on the map.
 * @param loader The image loader.
 */
public record FloorView(int x, int y, int Size, ImageLoader loader) {
	/**
	 * Canonical constructor with validation.
	 * * @param x      Grid width.
	 * @param y      Grid height.
	 * @param Size   Room tile size.
	 * @param loader The image loader.
	 * @throws NullPointerException if loader is null.
	 */
	public FloorView{
		Objects.requireNonNull(loader);
	}
	
	private void drawBackground(Graphics2D g, int width, int height) {
		var background = loader.get("MAP");
		if(background != null) {
			g.drawImage(background, 0, 0, width, height, null);
		}
	}
	
	
	private void drawHero(Graphics2D g, int col, int row, ScreenInfo info) {
		var hero = loader.get("MAPSHERO");
		if(hero != null) {
			var xOri = (info.width() - (this.x * Size))/2;
			var yOri = (info.height() - (this.y * Size))/2;
			
			var posX = xOri + (col * Size);
			var posY = yOri + (row * Size);
			
			g.drawImage(hero, posX, posY, Size, Size, null);
		}
	}
	
//	private void drawSquare(Graphics2D g, ScreenInfo info, int col, int row,Color color) {
//		var xOri = (info.width() - (this.x * Size)) / 2;
//		var yOri = (info.height() - (this.y * Size)) / 2;
//		
//		var posX = xOri + (col * Size);
//		var posY = yOri + (row * Size);
//		
//		g.setColor(color);
//		g.fillRect(posX, posY, Size, Size);
//	}
	
	private void drawGrid(Graphics2D g, ScreenInfo info, Floor floor) {
		g.setColor(new Color(155));
		int xOri = (info.width() - (x * Size))/2;
		int yOri = (info.height() - (y * Size))/2;
		for(int j = 0; j < y; j++) {
			for(int i = 0; i < x; i++) {
				var posX = xOri + (i * Size); 
				var posY = yOri + (j * Size);
				var room = floor.get(j, i);
				if(room == null) {
					g.setColor(Color.black);
					g.fillRect(posX,posY , Size, Size);
					g.setColor(Color.GRAY);
					g.drawRect(posX, posY, Size, Size);
				}else {
					g.setColor(getColorRoom(room.getType()));
					g.fillRect(posX,posY , Size, Size);
					
					g.setColor(Color.GRAY);
					g.drawRect(posX, posY, Size, Size);
				}
			}
		}
	}
	
	private Color getColorRoom(TypeRoom type) {
		return switch(type) {
		case BOSSROOM -> Color.MAGENTA;
		case BATTLEROOM -> Color.RED;
		case TREASUREROOM -> Color.YELLOW;
		case SHOPROOM -> Color.GREEN;
//		case BOSSROOM -> Color.BLUE;
		case EXITROOM -> Color.CYAN;
		case HEALROOM -> Color.PINK;
		case CORRIDOR -> Color.DARK_GRAY;
		case SURPRISEROOM -> Color.blue;
		
//		default -> Color.BLACK;
		};
	}
	
	private void drawLegend(Graphics2D g) {
		var x = 30;
		var y = 50;
		for(var typeRoom : TypeRoom.values()) {
			g.setColor(getColorRoom(typeRoom));
			g.fillRect(x, y, 20, 20);
			
			g.setColor(Color.WHITE);
			g.drawRect(x, y, 20, 20);
			
			String lab = typeRoom.name().replace("ROOM", "");
			g.drawString(lab, x + 30 , y + 15);
			y += 35;
		}
	}
	
	private void DrawMaps(Graphics2D g, int width, int height,ScreenInfo info, Coordinate c, Floor floor) {
		drawBackground(g, width, height);
		drawGrid(g,info,floor);
		drawHero(g, c.y(), c.x(), info);
		drawLegend(g);
	}
	
	/**
   * Entry point to render the Map screen.
   * <p>
   * Calculates the layout and delegates drawing of the grid, hero, and legend.
   * </p>
   *
   * @param ctx    The application context for graphics rendering.
   * @param width  The window width.
   * @param height The window height.
   * @param info   Screen information used for centering the grid.
   * @param c      The current coordinates of the hero on the map.
   * @param floor  The floor model containing the layout of rooms.
   */
	public void draw(ApplicationContext ctx, int width, int height,ScreenInfo info,Coordinate c, Floor floor) {
		ctx.renderFrame(g -> DrawMaps(g , width, height, info,c,floor));
	}
}