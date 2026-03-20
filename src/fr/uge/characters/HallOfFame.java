package fr.uge.characters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the high scores of the game (Hall of Fame).
 * It reads and writes scores to a text file.
 */
public class HallOfFame {
  private static final Path PATH = Path.of("data/state/HallOfFame/hall_of_fame.txt");
  private Map<String, Long> scores = new LinkedHashMap<>();

  /**
   * Creates the HallOfFame and loads existing scores from disk.
   * @throws IOException if the file cannot be read.
   */
  public HallOfFame() throws IOException {
    loadScores();
  }
  
  /**
   * Saves a hero's score if it's high enough to be in the top 10.
   * Merges with existing score if the username already exists (keeps the best).
   * * @param hero the hero whose score to save.
   * @throws IOException if saving to file fails.
   */
  public void saveScore(Hero hero) throws IOException {
    Objects.requireNonNull(hero);
    
    scores.merge(hero.username(), hero.computeFinalScore(), Math::max);
    sortAndLimit();
    saveToFile();
  }

  /**
   * Returns a list of formatted strings to display the top scores in the UI.
   * @return list of "Username : Score" strings.
   */
  public List<String> getScoresForDisplay() {
    return scores.entrySet().stream()
        .map(entry -> String.format("%-15s %10d", entry.getKey(), entry.getValue()))
        .toList();
  }

  /**
   * Sorts the scores in descending order and keeps only the top 10.
   */
  private void sortAndLimit() {
    scores = scores.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(10)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (e1, _) -> e1,
            LinkedHashMap::new
        ));
  }

  /**
   * Writes the current top scores to the file.
   * Creates parent directories if they don't exist.
   * @throws IOException if writing fails.
   */
  private void saveToFile() throws IOException {
    var lines = scores.entrySet().stream()
        .map(entry -> entry.getKey() + ";" + entry.getValue())
        .toList();

    if (PATH.getParent() != null) {
      Files.createDirectories(PATH.getParent());
    }
    Files.write(PATH, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * Reads scores from the file.
   * @throws IOException if reading fails.
   */
  private void loadScores() throws IOException {
    if (!Files.exists(PATH)) {
      return;
    }
    var lines = Files.readAllLines(PATH, StandardCharsets.UTF_8);
    scores.clear();
    for (var line : lines) {
      var parts = line.split(";");
      if (parts.length == 2 && parts[1].matches("\\d+")) {
        scores.put(parts[0], Long.parseLong(parts[1]));
      }
    }
    sortAndLimit();
  }
}