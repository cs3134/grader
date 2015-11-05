import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BwogBotJava {

  private HashMap<String, Integer> counts;

  public BwogBotJava() {
    counts = new HashMap<>();
  }

  public void readFile(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    String line;
    while ((line = br.readLine()) != null) {
      for (String word : line.split("\\s+")) {
        if (counts.containsKey(word)) {
          counts.put(word, counts.get(word) + 1);
        } else {
          counts.put(word, 1);
        }
      }
    }
    br.close();
  }

  public int getCount(String word) {
    return counts.get(word);
  }

  public List<String> getNMostPopularWords(int n) {
    List<String> sorted = counts.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue())
        .map((entry) -> entry.getKey()).collect(Collectors.toList());
    return sorted.subList(0, n);
  }

  public Map<String, Integer> getMap() {
    // invalid because Java's HashMap does not implement our Map interface
    return null;
  }

  public static void main(String[] args) throws IOException {
    BwogBotJava bot = new BwogBotJava();
    bot.readFile("comments.txt");
    System.out.println(bot.getCount("hamdel")); // because linan's hungry now
    System.out.println(bot.getNMostPopularWords(100));
  }
}
