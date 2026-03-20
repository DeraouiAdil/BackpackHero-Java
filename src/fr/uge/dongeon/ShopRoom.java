package fr.uge.dongeon;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import fr.uge.BackPack;
import fr.uge.Coordinate;
import fr.uge.characters.Hero;
import fr.uge.items.Gold;
import fr.uge.items.Item;
import fr.uge.items.ItemEmpty;
import fr.uge.items.enums.ItemEnum;

/**
 * Represents a shop in the dungeon.
 * The hero can buy items here with gold or sell items from their backpack.
 */
public final class ShopRoom implements Room{
	private final Map<Coordinate,Item> shop = new HashMap<>();
	private final int levelHero;
	private final Coordinate coordRoom;
	private final int height = 2;
	private final int width = 5;

  
  /**
   * Gets an item from the shop shelf.
   * @param coordinate the position of the item in the shop.
   * @return the item or null.
   */
  public Item getItem(Coordinate coordinate) {
  	Objects.requireNonNull(coordinate);
  	return shop.getOrDefault(coordinate, null);
  }
	
  /**
   * Checks if the shop has items.
   * @return true if the shop is not empty.
   */
  public boolean isStockGenerated() {
  	return !shop.isEmpty();
  }
  
  /**
   * Creates a shop room.
   * @param coordRoom the location of the room.
   * @param levelHero the level of the hero (for item generation).
   */
	public ShopRoom(Coordinate coordRoom, int levelHero) {
		Objects.requireNonNull(coordRoom);
		this.levelHero = levelHero;
		this.coordRoom = coordRoom;
	}
	
	public Item get() {
		return shop.getOrDefault(coordRoom, null);
	}

	private void displayNotEnoughtGold(long requiredGold, long currentGold) {
		System.out.println("=".repeat(50));
		System.out.println("Vous n'avez pas assez d'Or pour cet achat !");
		System.out.println("Or requis : " + requiredGold);
		System.out.println("Or actuel : " + currentGold);
		System.out.println("=".repeat(50));
	}
	
	private void displayBuyResultat(Hero hero, Item item, boolean success, BackPack backpack) {
		System.out.println("=".repeat(50));
		System.out.println(" ".repeat(20) + "Shop");
		System.out.println("=".repeat(50));
		if(success) {
			var price = Item.buyingPrice(item).amount();
			System.out.println("Achat réussi !");
			System.out.println("Vous avez acheté " + item.name());
			System.out.println("Coût : " + price + " Or.");
			System.out.println("Or restant : " + backpack.getGold());
			System.out.println("Voici votre sac après l'achat !");
			System.out.println(backpack);
		}else {
			System.out.println("Achat échoué");
			System.out.println("Impossible d'ajouter " + item.name() + " a votre sac");
		}
	}


	/**
	 * Fills the shop with random items.
	 * It uses the hero level to choose the items.
	 */
	public void generateMerchantStock() {
    var rdm = new Random();
    for(int nb1 = 0 ; nb1 < height ; nb1++) {
      for(int nb2 = 0 ; nb2 < width ; nb2++) {
        var randomLvl = rdm.nextInt(0, levelHero + 1);
        var rdmType = rdm.nextInt(0, ItemEnum.values().length);
        var ie = ItemEnum.values()[rdmType];
        
        
        shop.put(new Coordinate(nb1, nb2), Item.getRandom(ie, randomLvl));
      }   
    }
	}
	
	private boolean isEmpty(Item item) {
		return switch(item) {
		case ItemEmpty _ -> true;
		default -> false;
		};
	}
	
	private boolean isGold(Item item) {
		return switch(item) {
			case Gold _ -> true;
			default -> false;
		};
	}
	
	/**
	 * Tries to buy an item from the shop.
	 * It checks if the hero has enough gold.
	 * @param hero the hero buying the item.
	 * @param coordinateItem the location of the item in the shop.
	 * @param coordinateBP the location in the backpack to put the item.
	 * @return the updated hero.
	 */
	public Hero buyItem(Hero hero,Coordinate coordinateItem,Coordinate coordinateBP) {
		Objects.requireNonNull(coordinateItem);
		Objects.requireNonNull(coordinateBP);
		Objects.requireNonNull(hero);
		var item = shop.get(coordinateItem);
		boolean isEmpty = isEmpty(item);
		if(isEmpty) {
			return hero;
		}
		var price = Item.buyingPrice(item).amount();
		var backpack = hero.backpack();
		
		if(backpack.getGold() < price) {
			displayNotEnoughtGold(price, backpack.getGold());
			return hero;
		}
		
		if(backpack.useGold(price)) {
			if(backpack.add(item, coordinateBP)) {
				shop.put(coordinateItem, new ItemEmpty());
				displayBuyResultat(hero, item, true, backpack);
			}else {
				backpack.add(new Gold(price), new Coordinate(0, 0));
				displayBuyResultat(hero, item, false, backpack);
			}
		}
		return hero;
	}
	
	/**
	 * Sells an item from the hero's backpack.
	 * @param hero the hero selling the item.
	 * @param coordinateBP the location of the item in the backpack.
	 * @return true if the sale was successful.
	 */
	public boolean sellItem(Hero hero, Coordinate coordinateBP) {
    Objects.requireNonNull(hero);
    Objects.requireNonNull(coordinateBP);
    var item = hero.backpack().get(coordinateBP);
    if (item == null || isEmpty(item) || isGold(item)) {
      System.out.println("Il n'y a rien à vendre à cet emplacement.");
      return false;
    }
    var price = Item.sellingPrice(item).amount();
    var removedItem = hero.backpack().take(coordinateBP); 
    
    if(removedItem != null){
    	hero.backpack().add(new Gold(price), coordinateBP);
      displaySellResult(removedItem, price, hero.backpack());
      return true;
    }
    IO.println("erreur:impossible de retirer l'objet.");
    return false;
}
	
	//debug
	private void displaySellResult(Item item, long price, BackPack backpack) {
    IO.println("=".repeat(50));
    IO.println(" ".repeat(20) + "Vente");
    IO.println("=".repeat(50));
    IO.println("Vente réussie !");
    IO.println("Vous avez vendu " + item.name());
    IO.println("Gain : " + price + " Or.");
    IO.println("Or total : " + backpack.getGold());
    IO.println("=".repeat(50));
	}
	
	@Override
	public Coordinate coordRoom() {
		return coordRoom;
	}
	@Override
	public String getSymbol() {
		return "S";
	}

	@Override
	public TypeRoom getType() {
		return TypeRoom.SHOPROOM;
	}
	
	@Override 
	public String toString() {
		var sb = new StringBuilder();
		sb.append("=".repeat(50)).append("\n").append(" ".repeat(20)).append("SHOP").append("\n").append("=".repeat(50)).append("\n");
		var separator = "";
		for(var elem : shop.keySet()) {
			var element = shop.get(elem);
			sb.append(separator).append( element )
			.append("Coût: ").append(Item.buyingPrice(element).amount()).append(" Or ")
			.append("Coordonnées : ").append(elem).append("\n");
			separator = "\n";
		}
		sb.append("=".repeat(50));
		return sb.toString();
	}
	
}