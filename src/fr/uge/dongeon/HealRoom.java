package fr.uge.dongeon;

//import java.util.Objects;
import fr.uge.Coordinate;
//import fr.uge.characters.Hero;

/**
 * A room where the hero can pay gold to heal.
 * @param coordRoom position of the room.
 * @param levelHero level of the hero (not used here but keeps standard).
 */
public record HealRoom(Coordinate coordRoom, int levelHero) implements Room{
	
//	private void displayShop(Hero hero) {
//		System.out.println("═".repeat(50));
//		System.out.println(" ".repeat(20) + " Se soigner");
//		System.out.println("═".repeat(50));
//		System.out.println(hero);
//		System.out.println("Voulez vous vous soigner ? Choisissez une option :\n");
//		System.out.println("a - Aucun soin\n");
//		System.out.println("b - Soin de 25 (5 Or)\n");
//		System.out.println("c - Soin de 50 (10 Or)\n");
//		System.out.println("═".repeat(50));
//		System.out.println("Votre choix : ");
//	}
	
//	private void displayHealSuccess(Hero hero , int nbPv) {
//		System.out.println("═".repeat(50));
//		System.out.println(" ".repeat(20) + "Soin appliqué");
//		System.out.println("═".repeat(50));
//		System.out.println("Achat réussi, vous avez récupéré : " + nbPv);
//		System.out.println(hero );
//		System.out.println("═".repeat(50));
//	}
	
//	private void heal(Hero hero, int nbPv , int prix) {
//		Objects.requireNonNull(hero);
//		var backpack = hero.backpack();
//		if (hero.pv() >= hero.maxPv()) {
//      System.out.println("Vos PV sont déjà au maximum !");
//      return;
//		}
//		
//		if(!backpack.useGold(prix)) {
//			System.out.println("Vous n'avez pas assez d'argent");
//			return;
//		}
//		hero.heal(nbPv);
//		displayHealSuccess(hero,nbPv);
//	}
	
//	/**
//	 * Starts the interaction with the player to heal.
//	 * @param hero the hero entering the room.
//	 */
//	public void healHero(Hero hero) {
//		Objects.requireNonNull(hero);
//		if (hero.pv() >= hero.maxPv()) {
//			System.out.println("Vous êtes déjà en pleine forme ! Pas besoin de soins.");
//			return;
//		}
//		Scanner scanner = new Scanner(System.in);
//		displayShop(hero);
//		var answer = scanner.nextLine();
//		switch(answer) {
//			case "a" -> System.out.println("Vous quittez la salle de soin.");
//			case "b" -> heal(hero, 25, 5);
//			case "c" -> heal(hero, 50, 10);
//			default -> System.out.println("Mauvaise entrée");
//		}
//		scanner.close();
//	}
	
	@Override
	public Coordinate coordRoom() {
		return coordRoom;
	}
	@Override
	public String getSymbol() {
		return "H";
	}
	
	@Override
	public TypeRoom getType() {
		return TypeRoom.HEALROOM;
	}
}