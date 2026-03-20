package fr.uge;

/**
 * Manages character progression, including current level, experience (XP), and thresholds.
 * * @param level         The current character level.
 * @param xp            The current amount of experience points.
 * @param xpToNextLevel The experience points required to reach the next level.
 */
public record Level(int level,int xp,int xpToNextLevel) {
	
	/**
	 * Default constructor initializing a level 1 character with 0 XP
	 * and a requirement of 10 XP for the next level.
	 */
	public Level() {
		this(1,0,10);
	}
	
	/**
	 * Compact constructor with validation.
	 * * @param level Current level.
	 * @param xp Current experience.
	 * @param xpToNextLevel Experience needed for next level.
	 * @throws IllegalArgumentException if any parameter is negative.
	 */
	public Level{
		if(level < 0 || xp < 0 || xpToNextLevel < 0) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Adds experience points to the current pool and handles leveling up logic.
	 * <p>
	 * If the total XP exceeds the threshold, the level increases and the excess XP carries over.
	 * This process repeats if the amount is large enough to gain multiple levels at once.
	 * The difficulty (XP needed) increases by 5 for each subsequent level.
	 * </p>
	 * * @param amount The amount of XP to gain (must be positive).
	 * @return A new Level instance reflecting the updated stats.
	 * @throws IllegalArgumentException if amount is negative.
	 */
	public Level addXp(int amount) {
		if(amount < 0) {
			throw new IllegalArgumentException();
		}
		int newXp = xp + amount;
		int newLevel = level;
		int newXpToNextLevel=  xpToNextLevel;
		while(newXp >= newXpToNextLevel ) {
			newXp -= newXpToNextLevel;
			newLevel +=1;
			newXpToNextLevel = 5 * (newLevel + 1);
		}
		return new Level(newLevel,newXp,newXpToNextLevel);
	}
	
	@Override
	public String toString() {
		return "Level = " + level + " xp = " + xp + "xp need to next lvl up = " + xpToNextLevel;
	}
}