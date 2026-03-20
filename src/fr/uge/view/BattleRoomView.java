package fr.uge.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.PointerEvent;

import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.characters.Enemy;
import fr.uge.characters.Fight;
import fr.uge.characters.enums.EnemyType;
import fr.uge.items.Armor;
import fr.uge.items.Gold;
import fr.uge.items.Item;
import fr.uge.items.Weapon;

/**
 * Handles the graphical rendering of the Battle scene.
 * <p>
 * This view is responsible for drawing the hero, enemies, the backpack interface during combat,
 * popups (curses, level up expansion), and the game over screen.
 * </p>
 * * @param loader The image loader used to retrieve assets for the battle.
 */
public record BattleRoomView(ImageLoader loader) {
	/**
	 * Canonical constructor.
	 * * @param loader The image loader.
	 * @throws NullPointerException if the loader is null.
	 */
	public BattleRoomView {
		Objects.requireNonNull(loader);
	}

	/**
	 * Draws the selection grid for the backpack expansion (Level Up event).
	 * <p>
	 * This method renders a yellow highlight over valid coordinates where the player
	 * can choose to expand their inventory slot.
	 * </p>
	 *
	 * @param ctx     The application context.
	 * @param bp      The hero's backpack.
	 * @param targets The list of valid coordinates where a slot can be added.
	 * @param isBoss  True if the current context is a boss fight (affects background rendering).
	 */
	public void drawExpansionChoice(ApplicationContext ctx, BackPack bp, List<Coordinate> targets, boolean isBoss) {
    ctx.renderFrame(g -> {
        var screen = ctx.getScreenInfo();
        int width = screen.width();
        int height = screen.height();
        
        // On passe le booléen isBoss à drawBackground pour avoir le fond rouge ou noir
        drawBackground(g, width, height, isBoss);
        
        drawBackPack(ctx, bp, width, height);
        
        int tileSize = height / 16;
        int bagWidth = 3 * tileSize;
        int x0 = (width / 2) - (bagWidth / 2);
        int y0 = height / 6;
        
        g.setColor(new Color(255, 255, 0, 150));
        for (var coord : targets) {
            int x = x0 + coord.x() * tileSize;
            int y = y0 + coord.y() * tileSize;
            g.fillRect(x, y, tileSize - 2, tileSize - 2);
        }
    });
}
	
	
	private String extractImageKey(String imageName) {
		if (imageName == null || imageName.isEmpty()) {
			return "DEFAULT";
		}
		var dotIndex = imageName.lastIndexOf('.');
		return (dotIndex != -1) ? imageName.substring(0, dotIndex).toUpperCase() : imageName.toUpperCase();
	}


	private void drawBackground(Graphics2D g, int width, int height, boolean isBoss) {
    // Si c'est un boss, on cherche l'image "BOSSROOM", sinon "BATTLEROOM"
    var key = isBoss ? "BOSSROOM" : "BATTLEROOM";
    var background = loader.get(key);
    
    if (background != null) {
        g.drawImage(background, 0, 0, width, height, null);
    } else {
        // Fallback couleur : Rouge foncé pour Boss, Noir pour normal
        g.setColor(isBoss ? new Color(50, 0, 0) : Color.BLACK);
        g.fillRect(0, 0, width, height);
    }
}

	/**
   * Converts mouse screen coordinates into Backpack grid coordinates.
   * <p>
   * This is used to determine which slot in the backpack is being clicked or hovered.
   * </p>
   *
   * @param mouseX       The X position of the mouse.
   * @param mouseY       The Y position of the mouse.
   * @param screenWidth  The width of the window.
   * @param screenHeight The height of the window.
   * @return The corresponding coordinate in the backpack grid.
   */
	public Coordinate toBagCoordinate(float mouseX, float mouseY,int screenWidth, int screenHeight) {
		int tileSize = screenHeight / 16;
		int bagWidth = 3 * tileSize;
		int x0 = (screenWidth / 2) - (bagWidth / 2);
		int y0 = screenHeight/6;
		
		int gridX = (int) Math.floor((mouseX - x0) / tileSize);
		int gridY = (int) Math.floor((mouseY - y0) / tileSize);
		return new Coordinate(gridX,gridY);

	}
	


	private void drawInterface(Graphics2D g, int width, int height, Fight fight, boolean isBoss) {
    g.setFont(new Font("Arial", Font.BOLD, 20));
    g.setColor(Color.WHITE);
    
    String title = isBoss ? "COMBAT DE BOSS" : (fight.isHeroTurn() ? "C'est votre tour" : "Tour des ennemis");
    if (isBoss) g.setColor(Color.RED); // Titre en rouge pour le boss
    
    g.drawString(title, width / 15, height / 20);

    // ... reste de la méthode drawInterface inchangé
    g.setColor(Color.YELLOW);
    g.drawString("PV :" + fight.hero().pv() + " / " + fight.hero().maxPv(), 50, height - height / 10);
    g.setColor(Color.DARK_GRAY);
    g.drawString("ARMURE : " + (fight.hero().getTotalProtection() + fight.hero().protection()), 50, height - height / 12);
    g.setColor(Color.CYAN);
    g.drawString("Energie : " + fight.hero().energy(), 50, height - 70);
	}
	
	private void drawHero(Graphics2D g, int width, int height, Fight fight) {
		var heroKey = extractImageKey(fight.hero().img());
		var heroImg = loader.get(heroKey);
		if (heroImg != null) {
			g.drawImage(heroImg, width / 3, height / 2, height / 5, height / 5, null);
		} else {
			g.setColor(Color.BLUE);
			g.fillRect(width / 3, height / 2, height / 5, height / 5);

		}
	}

	private void drawEnemyImage(Graphics2D g, Enemy enemy, int x, int y, int size) {
		var enemyKey = extractImageKey(enemy.img());
		var enemyImg = loader.get(enemyKey);
		if (enemyImg != null) {
			g.drawImage(enemyImg, x, y, size, size, null);
		} else {
			g.setColor(Color.RED);
			g.fillRect(x, y, size, size);
		}
	}

	private void drawEnemyStats(Graphics2D g, Enemy enemy, int x, int y, int height) {
		g.setColor(Color.CYAN);
		g.setFont(new Font("Arial", Font.BOLD, 10));
		g.drawString(enemy.type().toString(), x, y - height / 15);

		g.setFont(new Font("Arial", Font.BOLD, 20));
		g.setColor(Color.RED);
		g.drawString(enemy.pv() + " HP", x, y - height / 60);
		
		g.setColor(Color.DARK_GRAY);
		g.drawString("ARMURE : " + enemy.protection(), x, y - height / 30);
	}

	private void drawSingleEnemy(Graphics2D g, int width, int height, Enemy enemy, int index) {
		var x = width - width / 10 - index * 150;
		var y = height / 2;
		var size = height / 6;

		drawEnemyImage(g, enemy, x, y, size);
		drawEnemyStats(g, enemy, x, y, height);
		drawEnemyNextAttack(g, enemy, x, y, height);
	}

	private void drawEnemies(Graphics2D g, int width, int height, List<Enemy> enemies) {
		for (var i = 0; i < enemies.size(); i++) {
			var enemy = enemies.get(i);
			if (enemy.isAlive()) {
				drawSingleEnemy(g, width, height, enemy, i);
			}
		}
	}
	
	
	/**
   * Renders a popup dialog offering a Cursed Item.
   * <p>
   * Displays two buttons: "Prendre" (Take) and "Refuser" (Refuse).
   * Refusing a cursed item results in damage.
   * </p>
   *
   * @param c The application context.
   * @param k Cost or identifier (unused in display currently).
   */
	public void drawCursePopup(ApplicationContext c, int k) {
		c.renderFrame( g -> {
			var screen = c.getScreenInfo();
			int width = screen.width();
			int height = screen.height();
			
			g.setColor(new Color(0,0,0,200));
			g.fillRect(0, 0, width, height);
			
			g.setColor(Color.WHITE);
			g.drawString("Malédiction en cas de refus vous subirez de degats !", width / 2 - 100, height / 2 -50);
			g.setColor(Color.GREEN);
			g.fillRect(width / 2 - 110, height / 2, 100, 40);
			
			g.setColor(Color.RED);
      g.fillRect(width / 2 + 10, height / 2, 100, 40);
      
      g.setColor(Color.black);
      g.drawString("Prendre", width / 2 - 100, height / 2 + 25);
      g.drawString("Refuser", width / 2 + 20, height / 2 + 25);
		});
	}

	/**
   * Checks if the mouse click was on the "Accept Curse" button.
   *
   * @param p            The pointer event.
   * @param screenWidth  The screen width.
   * @param screenHeight The screen height.
   * @return true if the click is within the accept button's bounds.
   */
	public boolean clickAcceptCurse(PointerEvent p, int screenWidth,int screenHeight) {
		int cx = screenWidth /2;
		int cy = screenHeight /2;
		return p.location().x() >= cx - 110 && p.location().x() <= cx - 10 
        && p.location().y() >= cy && p.location().y() <= cy + 40;
	}
	
	/**
   * Checks if the mouse click was on the "Refuse Curse" button.
   *
   * @param p            The pointer event.
   * @param screenWidth  The screen width.
   * @param screenHeight The screen height.
   * @return true if the click is within the refuse button's bounds.
   */
	public boolean clickRefuseCurse(com.github.forax.zen.PointerEvent p, int screenWidth, int screenHeight) {
    int cx = screenWidth / 2;
    int cy = screenHeight / 2;
    return p.location().x() >= cx + 10 && p.location().x() <= cx + 110 
        && p.location().y() >= cy && p.location().y() <= cy + 40;
}
	
	private void drawSkipButton(Graphics2D g, int width, int height) {
		var x = (width / 2) - 70;
		var y = height - 60;

		g.setColor(Color.GRAY);
		g.fillRect(x, y, 150, 40);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 15));
		g.drawString("PASSER LE TOUR", x + 5, y + 25);
	}

	private void drawEnemyNextAttack(Graphics2D g, Enemy enemy, int x, int y, int height) {
		var nextAttack = enemy.nextAction();
		g.setColor(Color.ORANGE);
		var descr = switch (nextAttack.enemyAction()) {
			case ATTACK -> "Attaque : " + nextAttack.value();
			case PROTECTION -> "Protection +" + nextAttack.value();
			case CURSE -> "Malediction";
			default -> "Repos";
		};
		if(enemy.type() == EnemyType.MIMIC) {
			descr = "COPIE";
		}
		g.drawString(descr, x, y - height / 20);
	}
	
	private void drawBackPack(ApplicationContext ctx, BackPack bp, int width, int height) {
		var tileSize = height / 16;
		var bagWidth = 3 * tileSize;
		var x0 = (width / 2) - (bagWidth / 2);
		var y0 = height/6;
		var viewBackpack = new BackPackView(x0, y0, 0, 0, tileSize, loader);
		BackPackView.draw(ctx, bp, viewBackpack, null, new Coordinate(0, 0), List.of());
		
		ctx.renderFrame(g ->{
			for(var entry : bp.items().entrySet()) {
				var item = entry.getValue();
				if(item.isCursed()) {
					var coord = entry.getKey();
					int x = x0 + coord.x() * tileSize;
					int y = y0 + coord.y() * tileSize;
					g.setColor(new Color(255,0,0,100));
					g.fillRect(x, y, tileSize, tileSize);
					g.setColor(Color.RED);
					g.drawRect(x, y, tileSize,tileSize);
 				}
			}
		});
	}

	private void drawStatPanel(Graphics2D g, int width, int height, Item item) {
		int pannel_width =280;
		int pannel_height =150;
		int x = 20;
		int y = height - pannel_height - 150;
		
		g.setColor(new Color(0,0,0,180));
		g.fillRoundRect(x, y, pannel_width, pannel_height, 15, 15);
		g.setColor(Color.WHITE);
		
		g.setFont(new Font("SansSerif", Font.BOLD,16));
		g.setColor(Color.LIGHT_GRAY);
		var realItem = item.unwrap();
		String stats = switch(realItem) {
		case Weapon w -> "Degats : " + w.damage() + " | Rareté : " + w.rarity()+ (item.isCursed() ? " | Objet maudit":"");
		case Armor a -> "Defense : " + a.getProtection() + " | Type : " + a.getArmorType() + (item.isCursed() ? " | Objet maudit":"");
		case Gold v -> "Gold : " + v.amount();
		default -> "Objet utilitaire";
		};
		g.drawString(stats, x + 15, y + 55);
	}
	
	/**
   * Main entry point to render the entire Battle scene.
   * <p>
   * This handles the display of the hero, the enemies, the turn interface,
   * the backpack, and item statistic panels. It also automatically detects if the
   * current fight involves a Boss to adjust the background and UI.
   * </p>
   *
   * @param ctx           The application context.
   * @param fight         The current fight state.
   * @param enemies       The list of enemies.
   * @param activeIndex   The index of the currently active enemy (if any).
   * @param bp            The hero's backpack.
   * @param selectedItem  The item currently selected/hovered (for stats display).
   */
	public void draw(ApplicationContext ctx, Fight fight, List<Enemy> enemies, int activeIndex, BackPack bp, Item selectedItem) {
    Objects.requireNonNull(ctx);
    Objects.requireNonNull(fight);
    Objects.requireNonNull(enemies);
    Objects.requireNonNull(bp);

    // DÉTECTION AUTOMATIQUE DU BOSS
    // On regarde si au moins un ennemi dans la liste est un Boss
    boolean isBossFight = enemies.stream().anyMatch(Enemy::isBoss);

    ctx.renderFrame(g -> {
        var screen = ctx.getScreenInfo();
        
        // On passe l'information au background
        drawBackground(g, screen.width(), screen.height(), isBossFight);
        
        drawHero(g, screen.width(), screen.height(), fight);
        drawEnemies(g, screen.width(), screen.height(), enemies);
        
        // Optionnel : Changer le texte de l'interface pour le Boss
        drawInterface(g, screen.width(), screen.height(), fight, isBossFight);
        
        drawSkipButton(g, screen.width(), screen.height());
        drawBackPack(ctx, bp, screen.width(), screen.height());
        
        if(selectedItem != null) {
            drawStatPanel(g, screen.width(), screen.height(), selectedItem);
        }
    });
	}
	
	/**
   * Renders the Game Over screen.
   * <p>
   * Displays a simple black screen with "GAME OVER" in red text.
   * </p>
   *
   * @param ctx The application context.
   */
	public void drawGameOver(ApplicationContext ctx) {
		ctx.renderFrame(g -> {
	     var screen = ctx.getScreenInfo();
		   g.setColor(Color.BLACK);
		   g.fillRect(0, 0, screen.width(), screen.height());
		   
		   g.setColor(Color.RED);
		   g.setFont(new Font("Arial", Font.BOLD, 60));
		   var text = "GAME OVER";
		   var metrics = g.getFontMetrics();
		   
	
	     var x = (screen.width() - metrics.stringWidth(text)) / 2;
	     var y = screen.height() / 2;
	     g.drawString(text, x, y);
		});
	}
	
	
}