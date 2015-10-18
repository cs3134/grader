import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class Tests {

  public static ScoreSheet runTests(ScoreSheet scoreSheet) throws IOException {
    Properties properties = new Properties();
    FileInputStream input = new FileInputStream("config.prop");

    properties.load(input);

    scoreSheet.className = properties.getProperty("className");
    scoreSheet.homeworkName = properties.getProperty("homeworkName");
    scoreSheet.studentMax = Integer.parseInt(properties.getProperty("studentMax"));
    int timeLimitSeconds = Integer.parseInt(properties.getProperty("timeLimitSeconds"));

    Callable<ScoreSheet> tests = new Callable<ScoreSheet>() {
      @Override
      public ScoreSheet call() throws Exception {
        return tests(scoreSheet);
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<ScoreSheet> future = executor.submit(tests);
    executor.shutdown();

    try {
      future.get(timeLimitSeconds, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      scoreSheet.errorMessage = "Timed out";
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    } catch (ExecutionException e) {
      scoreSheet.errorMessage = "Timed out";
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    } catch (TimeoutException e) {
      scoreSheet.errorMessage = "Timed out";
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    }

    if (!executor.isTerminated()) {
      executor.shutdownNow();
    }

    postJson(scoreSheet);

    return scoreSheet;
  }

  private static void postJson(ScoreSheet scoreSheet) throws IOException {
    System.out.println(scoreSheet.toJSONString());

    String postUrl = "http://jarvis.xyz/webhook/curl";
    HttpPost post = new HttpPost(postUrl);
    StringEntity postingString = new StringEntity(scoreSheet.toJSONString());
    post.setEntity(postingString);
    post.setHeader("Content-type", "application/json");
    HttpClient httpClient = HttpClientBuilder.create().build();
    httpClient.execute(post);
  }

  private static String stackTraceToString(Exception e) {
    StringBuilder sb = new StringBuilder();
    sb.append(e.getClass().getName() + "\n");

    int stackTraceCount = 0;
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element.toString());
      sb.append("\n");
      stackTraceCount++;
      if (stackTraceCount > 2) {
        sb.append("Stack trace redacted...");
        return sb.toString().trim();
      }
    }
    return sb.toString().trim();
  }

  private static <T> boolean compareCollections(Collection<T> l1, Collection<T> l2) {
    if (l1.size() != l2.size()) {
      return false;
    }
    T i2;
    Iterator<T> l2iter = l2.iterator();
    for (T i1 : l1) {
      i2 = l2iter.next();
      if (!(i2.equals(i1))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Graders, you should only edit this. No more.
   *
   * @param scoreSheet
   * @return
   * @throws IOException
   */
  private static ScoreSheet tests(ScoreSheet scoreSheet) throws IOException {

    testBST(scoreSheet);
    testTrie(scoreSheet);

    return scoreSheet;
  }

  private static void testBST(ScoreSheet scoreSheet) {
    String sectionString;
    int maxScore;

    BinarySearchTree<Integer> bst = new BinarySearchTree<Integer>();

    // isBst (test 1)

    sectionString = "isBst(): returns true on an empty BST";
    maxScore = 3;

    try {
      if (bst.isBst()) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "returns false on an empty BST");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }

    // isBst (test 2)

    sectionString = "isBst(): returns true on a BST built by inserting values";
    maxScore = 4;

    int[] vals = { 5, 3, 7, 2, 4, 6, 8 };
    for (Integer val : vals) {
      bst.insert(val);
    }

    try {
      if (bst.isBst()) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "returns false on a valid BST");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }

    // isBst (test 3)
    // TODO (can't build invalid BST with private BinaryNode class)

    sectionString = "isBst(): returns false on an invalid BST built with the constructor";
    maxScore = 4;

    BinarySearchTree<Integer> nonBst = new BinarySearchTree<Integer>();

    try {
      if (true) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "returns true on an invalid BST");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }

    // getInterval (test 1)

    sectionString = "getInterval(): bst contains no elements in interval";
    maxScore = 3;

    List<Integer> expected = new LinkedList<Integer>();

    try {
      if (bst.getInterval(10, 12).equals(expected)) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "incorrect interval");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }

    // getInterval (test 2)

    sectionString = "getInterval(): bst contains some elements in interval";
    maxScore = 4;

    expected = new HashSet<Integer>();
    for (int i = 4; i < 7; i++) {
      expected.add(i);
    }

    try {
      if (bst.getInterval(4, 6).equals(expected)) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "incorrect interval");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }

    // getInterval (test 3)

    sectionString = "getInterval(): bst contains all elements in interval";
    maxScore = 4;

    expected = new LinkedList<Integer>();
    for (int i = 2; i < 4; i++) {
      expected.add(i);
    }

    try {
      if (bst.getInterval(0, 3).equals(expected)) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "incorrect interval");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }

    // iterator (test 1)

    sectionString = "iterator(): returns inorder traversal of bst";
    maxScore = 11;

    expected = new LinkedList<Integer>();
    for (int i = 2; i < 9; i++) {
      expected.add(i);
    }
    List<Integer> actual = new LinkedList<Integer>();
    for (Integer data : bst) {
      actual.add(data);
    }

    try {
      if (actual.equals(expected)) {
        scoreSheet.addSection(sectionString, maxScore, maxScore, "");
      } else {
        scoreSheet.addSection(sectionString, 0, maxScore, "incorrect inorder traversal");
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionString, 0, maxScore, stackTraceToString(e));
    }
  }

  private static void testTrie(ScoreSheet scoreSheet) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("dictionary.txt"));
    String line;

    HashSet<String> dictionary = new HashSet<>();
    while ((line = bufferedReader.readLine()) != null) {
      dictionary.add(line);
    }

    bufferedReader.close();

    Trie trie = new Trie();

    // empty check contains returns correctly
    try {
      if (!trie.contains("pikachu")) {
        scoreSheet.addSection("contains(): empty trie returns false", 2, 2, "");
      } else {
        scoreSheet.addSection("contains(): empty trie returns false", 0, 2, "Empty trie returned true for a word");
      }
    } catch (Exception e) {
      scoreSheet.addSection("contains(): empty trie returns false", 0, 2, stackTraceToString(e));
    }

    // empty return list size 0
    try {
      if (trie.getStrings().size() == 0) {
        scoreSheet.addSection("getStrings(): empty trie returns list of size 0", 2, 2, "");
      } else {
        scoreSheet.addSection("getStrings(): empty trie returns list of size 0", 0, 2,
            "Empty trie did not return list of size 0");
      }
    } catch (Exception e) {
      scoreSheet.addSection("getStrings(): empty trie returns list of size 0", 0, 2, stackTraceToString(e));
    }

    // empty returns list size 0
    try {
      if (trie.getStartsWith("rat").size() == 0) {
        scoreSheet.addSection("getStartsWith(): empty trie returns list of size 0", 2, 2, "");
      } else {
        scoreSheet.addSection("getStartsWith(): empty trie returns list of size 0", 0, 2,
            "Empty trie did not return list of size 0");
      }
    } catch (Exception e) {
      scoreSheet.addSection("getStartrsWith(): empty trie returns list of size 0", 0, 2, stackTraceToString(e));
    }

    // add words
    try {
      for (String word : dictionary) {
        trie.addWord(word);
      }
      scoreSheet.addSection("addWords(): added many words", 11, 11, "");
    } catch (Exception e) {
      scoreSheet.addSection("addWords(): added many words", 0, 11, stackTraceToString(e));
    }

    // contains rattata
    try {
      if (trie.contains("rattata")) {
        scoreSheet.addSection("contains(): returns true for added word", 5, 5, "");
      } else {
        scoreSheet.addSection("contains(): returns true for added word", 0, 5, "Did not return true");
      }
    } catch (Exception e) {
      scoreSheet.addSection("contains(): returns true for added word", 0, 5, stackTraceToString(e));
    }

    // does not contains rat
    try {
      if (!trie.contains("rat")) {
        scoreSheet.addSection("contains(): returns false for non-added word", 4, 4, "");
      } else {
        scoreSheet.addSection("contains(): returns false for non-added word", 0, 4, "Did not return false");
      }
    } catch (Exception e) {
      scoreSheet.addSection("contains(): returns false for non-added word", 0, 4, stackTraceToString(e));
    }

    // get back list of added words
    try {
      HashSet<String> trieDictionary = new HashSet<String>(trie.getStrings());

      if (dictionary.equals(trieDictionary)) {
        scoreSheet.addSection("getStrings(): returns all added words", 9, 9, "");
      } else {
        scoreSheet.addSection("getStrings(): returns all added words", 0, 9, "Did not return all words correctly");
      }
    } catch (Exception e) {
      scoreSheet.addSection("getStrings(): returns all added words", 0, 9, stackTraceToString(e));
    }

    // getStartsWith pid
    try {
      String[] pids = { "pidgey", "pidgeotto", "pidgeot", "pidove", "pidgeotmega" };
      HashSet<String> pid = new HashSet<String>(Arrays.asList(pids));
      HashSet<String> pidTrie = new HashSet<String>(trie.getStartsWith("pid"));
      if (pid.equals(pidTrie)) {
        scoreSheet.addSection("getStartsWith(): returns words starting with a certain prefix", 5, 5, "");
      } else {
        scoreSheet.addSection("getStartsWith(): returns words starting with a certain prefix", 0, 5,
            "Did not return words correctly");
      }
    } catch (Exception e) {
      scoreSheet.addSection("getStartsWith(): returns words starting with a certain prefix", 0, 5,
          stackTraceToString(e));
    }

    // getStartsWith mew. notice that mew itself is a word
    try {
      String[] mews = { "mew", "mewtwo", "mewtwomegax", "mewtwomegay" };
      HashSet<String> mew = new HashSet<String>(Arrays.asList(mews));
      HashSet<String> mewTrie = new HashSet<String>(trie.getStartsWith("mew"));
      if (mew.equals(mewTrie)) {
        scoreSheet.addSection("getStartsWith(): returns words starting with a prefix which is also a word", 4, 4, "");
      } else {
        scoreSheet.addSection("getStartsWith(): returns words starting with a prefix which is also a word", 0, 4,
            "Did not return words correctly");
      }
    } catch (Exception e) {
      scoreSheet.addSection("getStartsWith(): returns words starting with a prefix which is also a word", 0, 4,
          stackTraceToString(e));
    }
  }
}
