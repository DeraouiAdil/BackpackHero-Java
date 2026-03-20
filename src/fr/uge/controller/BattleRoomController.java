package fr.uge.controller;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.PointerEvent;
import com.github.forax.zen.PointerEvent.Action;
import fr.uge.Coordinate;
import fr.uge.UserInterface;
import fr.uge.characters.Enemy;
import fr.uge.characters.Fight;
import fr.uge.characters.HallOfFame;
import fr.uge.characters.Hero;
import fr.uge.characters.enums.EnemyActionType;
import fr.uge.characters.enums.HeroActionType;
import fr.uge.dongeon.BattleRoom;
import fr.uge.dongeon.TreasureRoom;
import fr.uge.items.Armor;
import fr.uge.items.Item;
import fr.uge.items.Weapon;
import fr.uge.items.enums.ArmorType;
import fr.uge.view.BackPackView;
import fr.uge.view.BattleRoomView;
import fr.uge.view.ImageLoader;

/**
 * Controller responsible for the Battle Room logic.
 * Manages the turn-based combat system, enemy AI, hero actions, and rewards.
 */
public class BattleRoomController {
	private static int refusalCost = 1;
	
	/**
   * Static entry point to start a battle.
   * Initializes the view and starts the main battle loop.
   *
   * @param i    The user interface containing game state (Hero, Floor).
   * @param size The tile size for rendering.
   */
	public static void runBattleRoom(UserInterface i, int size) {
    Objects.requireNonNull(i);
    Application.run(Color.BLACK, ctx -> {
      var loader = new ImageLoader("data/img");
      var view = new BattleRoomView(loader);
      var room = i.floor().get(i.hero().position().x(), i.hero().position().y());
      

      switch (room) {
        case BattleRoom battle -> {
          try {
            startBattleLoop(ctx, view, i, battle);
          } catch (IOException e) {
            System.err.println("erreur" + e.getMessage());
            ctx.dispose();
          }
        }
        default -> { }
      }
    });   
	}


  /**
   * The core loop of the battle.
   * Alternates turns between hero and enemies until victory or defeat.
   *
   * @param ctx    The application context.
   * @param view   The battle view.
   * @param i      The user interface.
   * @param battle The battle room model.
   * @throws IOException If saving the score fails.
   */
	private static void startBattleLoop(ApplicationContext ctx, BattleRoomView view, UserInterface i, BattleRoom battle) throws IOException {	
	  var fight = new Fight(i.hero()).withHeroTurn(true);
	  var enemies = battle.enemies();
	  while (fight.hero().isAlive() && !battle.isCleared()) {
      fight = executeTurn(ctx, view, fight, battle, enemies);
	      
      if (!fight.hero().isAlive()) {
        var h = new HallOfFame();
        h.saveScore(fight.hero());
        view.drawGameOver(ctx);
        pause(3000);//gameover
        ctx.dispose();
        return;
      }
      if (battle.isCleared()) {
        handleEndGame(ctx, view, i, battle);
        return;
      }
	  }
	}

  /**
   * Executes a single turn (either Hero or Enemy).
   *
   * @param ctx     The application context.
   * @param view    The battle view.
   * @param fight   The current fight state.
   * @param battle  The battle room data.
   * @param enemies The list of enemies.
   * @return The updated fight state.
   */
  private static Fight executeTurn(ApplicationContext ctx, BattleRoomView view, Fight fight, BattleRoom battle, List<Enemy> enemies) {
    view.draw(ctx, fight, battle.enemies(), -1, fight.hero().backpack(),null);
    if (fight.isHeroTurn()) {
      return heroTurn(ctx, view, fight, battle);
    }
    return executeEnemiesTurn(ctx, view, fight, battle, battle.enemies());
  }

  /**
   * Handles the sequence of events when the battle is won.
   * Awards XP, handles level up, and creates a loot chest.
   *
   * @param ctx    The application context.
   * @param view   The battle view.
   * @param i      The user interface.
   * @param battle The battle room.
   */
  private static void handleEndGame(ApplicationContext ctx, BattleRoomView view, UserInterface i, BattleRoom battle) {
    var oldLvl = i.hero().level();
    battle.claimRewards(i.hero());
    i.hero().addEnergy(3);
    boolean isBoss = battle.enemies().stream().anyMatch(Enemy::isBoss); //

    if (i.hero().level() > oldLvl) {
        handleLevelUp(ctx, view, i, isBoss);
    }
    var lootChest = new TreasureRoom(new Coordinate(-1, -1), i.hero().level());
    TreasureRoomController.run(ctx, i.hero(), lootChest);
    ctx.dispose();

  }

  
  /**
   * Executes the AI turn for all enemies.
   *
   * @param ctx     The application context.
   * @param view    The battle view.
   * @param fight   The fight state.
   * @param battle  The battle room.
   * @param enemies The list of enemies.
   * @return The updated fight state with hero's turn enabled.
   */
  private static Fight executeEnemiesTurn(ApplicationContext ctx, BattleRoomView view, Fight fight, BattleRoom battle, List<Enemy> enemies) {
    var currentFight = fight;
    for (int idx = 0; idx < enemies.size(); idx++) {
      var enemy = enemies.get(idx);
    	if (!enemies.get(idx).isAlive() || !currentFight.hero().isAlive()) continue;
      
      pause(550);
      if(enemies.get(idx).nextAction().enemyAction() == EnemyActionType.CURSE) {
      	if(currentFight.hero().backpack().hasCurseableItem()) {
      		handleCurse(ctx, view, currentFight.hero());
      		enemy.updateNextAction();
      	}
      	
      }else {
        var result = battle.enemyTurn(currentFight.withHeroTurn(false), idx);
        currentFight = result.fight();
      }

      view.draw(ctx, currentFight, enemies, idx, currentFight.hero().backpack(),null);
    }
    currentFight.hero().addEnergy(3);
    pause(550);
    while (ctx.pollEvent() != null); 
    currentFight.hero().setProtection(0);
    return currentFight.withHeroTurn(true);
  }

  /**
   * Manages the Hero's turn logic.
   * Waits for player input to select items, attack enemies, or use shields.
   *
   * @param ctx    The application context.
   * @param view   The battle view.
   * @param fight  The fight state.
   * @param battle The battle room.
   * @return The updated fight state with hero's turn disabled.
   */
  private static Fight heroTurn(ApplicationContext ctx, BattleRoomView view, Fight fight, BattleRoom battle) {
    var currentFight = fight;
    Weapon weapon = null;
    Item itemForDisplay = null;
    
    while (currentFight.hero().isAlive() && currentFight.isHeroTurn() && !battle.isCleared() && currentFight.hero().energy() > 0) {
      
      var event = ctx.pollOrWaitEvent(10);
      if (event == null) continue;
      
      switch (event) {
        case PointerEvent pe when pe.action() == Action.POINTER_DOWN -> {
           var item = getClickedItem(pe, view, currentFight, ctx);
           if(item != null) {
          	 itemForDisplay = item;
          	 var realItem = item.unwrap();
           
           switch(realItem) {
	           case Weapon w -> weapon = w;
	           case Armor a when a.getArmorType() == ArmorType.SHIELD -> currentFight = tryApplyArmor(currentFight, a);
	           default -> currentFight = resolveClick(pe, item, currentFight, battle, weapon, ctx);
	           }
           }else {
          	 currentFight = resolveClick(pe, null, currentFight, battle, weapon, ctx);
           }
           view.draw(ctx, currentFight, battle.enemies(), -1, currentFight.hero().backpack(), itemForDisplay);
        }
        default -> { }
      }
    }

    return currentFight.withHeroTurn(false);
  }

  /**
   * Resolves a click event during the hero's turn.
   * Checks for attacks, button clicks (skip), or item usage.
   *
   * @param pe     The pointer event.
   * @param item   The item clicked (if any).
   * @param fight  The fight state.
   * @param battle The battle room.
   * @param weapon The currently selected weapon.
   * @param ctx    The application context.
   * @return The updated fight state.
   */
  private static Fight resolveClick(PointerEvent pe, Item item, Fight fight, BattleRoom battle, Weapon weapon, ApplicationContext ctx) {
  	return switch(item) {
  	case Armor a -> tryApplyArmor(fight, a);
  	case null -> {
  		if(isSkipButton(pe, ctx)) {
  			yield fight.withHeroTurn(false);
  		}
  		if(weapon != null) {
  			yield tryAttack(pe, ctx, battle, weapon, fight);
  		}
  		yield fight;
  	}
  	default -> fight;
  	};
  }

  /**
   * Applies armor effect (Shield) if energy is sufficient.
   *
   * @param fight The fight state.
   * @param armor The armor to use.
   * @return The updated fight state.
   */
  private static Fight tryApplyArmor(Fight fight, Armor armor) {
    if (armor.getArmorType() == ArmorType.SHIELD && fight.hero().energy() > 0) {
    	fight.hero().useEnergy(1);
      return fight.heroProtects(armor.getProtection()).withHeroTurn(true);
    }
    return fight;
  }

  /**
   * Attempts to attack a specific enemy.
   *
   * @param pe     The pointer event used to identify the target enemy.
   * @param ctx    The application context.
   * @param battle The battle room.
   * @param weapon The weapon used.
   * @param fight  The fight state.
   * @return The updated fight state.
   */
  private static Fight tryAttack(PointerEvent pe, ApplicationContext ctx, BattleRoom battle, Weapon weapon, Fight fight) {
    var idx = getEnemyAtClick(pe, ctx, battle.enemies());
    if (idx != -1) {
    	if(fight.hero().energy() <= 0) {
    		return fight;
    	}
    	fight.hero().useEnergy(1);
    	var result = battle.heroTurn(fight, HeroActionType.ATTACK, weapon.damage(), idx);
       return result.fight().withHeroTurn(true);
    }
    return fight;
  }

  /**
   * Retrieves the item located at the clicked coordinates in the backpack view.
   *
   * @param pe   The pointer event.
   * @param v    The battle view.
   * @param f    The fight state.
   * @param ctx  The application context.
   * @return The item found, or null.
   */
  private static Item getClickedItem(PointerEvent pe, BattleRoomView v, Fight f, ApplicationContext ctx) {
    var s = ctx.getScreenInfo();
    int ts = s.height() / 16;
    int x0 = (s.width() / 2) - ((3 * ts) / 2);
    int y0 = s.height() /6;
    var bpView = new BackPackView(x0, y0, s.width(), s.height(), ts, v.loader());
    return f.hero().backpack().get(bpView.toCoordinate(pe.location().x(), pe.location().y()));
  }

  /**
   * Identifies which enemy was clicked based on screen coordinates.
   *
   * @param pe      The pointer event.
   * @param ctx     The application context.
   * @param enemies The list of enemies.
   * @return The index of the enemy, or -1 if none.
   */
  private static int getEnemyAtClick(PointerEvent pe, ApplicationContext ctx, List<Enemy> enemies) {
    var s = ctx.getScreenInfo();
    for (int i = 0; i < enemies.size(); i++) {
      if (!enemies.get(i).isAlive()) continue;
      
      int x = s.width() - s.width() / 10 - i * 150;
      int y = s.height() / 2;
      int size = s.height() / 6;
      
      var loc = pe.location();
      if (loc.x() >= x && loc.x() <= x + size && loc.y() >= y && loc.y() <= y + size) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Checks if the "End Turn" button was clicked.
   *
   * @param pe  The pointer event.
   * @param ctx The application context.
   * @return true if the button was clicked.
   */
  private static boolean isSkipButton(PointerEvent pe, ApplicationContext ctx) {
    var screen = ctx.getScreenInfo();
    var x = (screen.width() / 2) - 75;
    var loc = pe.location();
    return loc.x() >= x && loc.x() <= x + 150 && loc.y() >= screen.height() - 60;
  }

  /**
   * Handles the Cursing event from enemies.
   * Displays a popup offering to accept a curse or take damage.
   *
   * @param ctx The application context.
   * @param v   The battle view.
   * @param h   The hero.
   */
  private static void handleCurse(ApplicationContext ctx,BattleRoomView v, Hero h) {
  	var screen = ctx.getScreenInfo();
  	while(true) {
  		v.drawCursePopup(ctx, refusalCost);
  		var event = ctx.pollOrWaitEvent(10);
  		if(event == null) {
  			continue;
  		}
  		
  		switch(event) {
  		case PointerEvent pe when pe.action() == Action.POINTER_DOWN -> {
  			if(v.clickAcceptCurse(pe, screen.width(), screen.height())) {
  				h.backpack().curseRandomItem();
  				return;
  			}
  			if(v.clickRefuseCurse(pe, screen.width(), screen.height())) {
  				h.takeDamage(refusalCost);
  				refusalCost++;
  				return;
  			}
  		}default -> {}

  		}
  	}
  }

  /**
   * Manages the Level Up UI sequence.
   * Allows the player to choose a backpack expansion slot.
   *
   * @param ctx    The application context.
   * @param view   The battle view.
   * @param i      The user interface.
   * @param isBoss True if the battle was against a boss (affects UI).
   */
  private static void handleLevelUp(ApplicationContext ctx, BattleRoomView view, UserInterface i, boolean isBoss) {
    var bp = i.hero().backpack();
    view.drawExpansionChoice(ctx, bp, bp.getPossibleExpansion(), isBoss); 

    while (bp.getSpace() > 0) {
        var event = ctx.pollOrWaitEvent(10);
        if (event == null) continue;

        switch(event) {
            case PointerEvent pe when pe.action() == Action.POINTER_DOWN -> 
                processExpansion(pe, ctx, view, bp, isBoss); 
            default -> {}
        }
    }
}
  
  /**
   * Processes the click for backpack expansion.
   *
   * @param pe     The pointer event.
   * @param ctx    The application context.
   * @param view   The battle view.
   * @param bp     The backpack.
   * @param isBoss True if it was a boss battle.
   */
  private static void processExpansion(PointerEvent pe, ApplicationContext ctx, BattleRoomView view, fr.uge.BackPack bp, boolean isBoss) {
    var s = ctx.getScreenInfo();
    var c = view.toBagCoordinate(pe.location().x(), pe.location().y(), s.width(), s.height());
    if (bp.addSlot(bp, c)) {
        view.drawExpansionChoice(ctx, bp, bp.getPossibleExpansion(), isBoss);
    }
}
  
  /**
   * Pauses the execution thread for a specified duration.
   *
   * @param ms Milliseconds to sleep.
   */
  private static void pause(int ms) {
    try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
  }
	
}