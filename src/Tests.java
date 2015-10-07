import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
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
      System.exit(124);
    } catch (ExecutionException e) {
      scoreSheet.errorMessage = "Timed out";
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(124);
    } catch (TimeoutException e) {
      scoreSheet.errorMessage = "Timed out";
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(124);
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

  /**
   * Graders, you should only edit this. No more.
   *
   * @param scoreSheet
   * @return
   */
  private static ScoreSheet tests(ScoreSheet scoreSheet) {

    countOutQueueTestList(scoreSheet);
    countOutQueueTestWinner(scoreSheet);
    countOutQueueTestWinnerRec(scoreSheet);
    try {
      bufferTest(scoreSheet);
    } catch (IOException e) {
      System.out.println(stackTraceToString(e));
      System.err.println("Skipping buffer test.");
    }
    return scoreSheet;
  }

  private static void countOutQueueTestList(ScoreSheet scoreSheet) {
    String testName = "CountOut with Queue, list of players";
    int MAXSCORE = 6;

    int score = MAXSCORE;

    try {
      // Homework example
      if (!(compareCollections(Arrays.asList(3, 7, 1, 6, 2, 9, 8, 0, 5, 4), CountOut.play(10, 4))))
        score -= 3;
      // k == N
      if (!(compareCollections(Arrays.asList(3, 0, 2, 1), CountOut.play(4, 4))))
        score -= 3;
      // k > N
      if (!(compareCollections(Arrays.asList(0), CountOut.play(1, 4))))
        score -= 3;
      if (!(compareCollections(Arrays.asList(1, 0), CountOut.play(2, 4))))
        score -= 3;
      if (!(compareCollections(Arrays.asList(0, 1), CountOut.play(2, 3))))
        score -= 3;
      if (!(compareCollections(Arrays.asList(0, 2, 1), CountOut.play(3, 4))))
        score -= 3;
      // k == 1
      if (!(compareCollections(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), CountOut.play(10, 1))))
        score -= 3;
    } catch (Exception e) {
      scoreSheet.addSection(testName, 0, MAXSCORE, stackTraceToString(e));
      return;
    }
    score = score < 0 ? 0 : score;
    if (score == MAXSCORE) {
      scoreSheet.addSection(testName, score, MAXSCORE, "");
    } else {
      scoreSheet.addSection(testName, score, MAXSCORE, "Incorrect return value.");
    }
  }

  private static void countOutQueueTestWinner(ScoreSheet scoreSheet) {
    String testName = "CountOut with Queue, find winner";
    int MAXSCORE = 5;

    int score = MAXSCORE;

    try {
      // Homework example
      if (CountOut.findWinner(10, 4) != 4)
        score -= 2;
      // k == N
      if (CountOut.findWinner(4, 4) != 1)
        score -= 2;
      // k > N
      if (CountOut.findWinner(1, 4) != 0)
        score -= 2;
      if (CountOut.findWinner(2, 4) != 0)
        score -= 2;
      if (CountOut.findWinner(2, 3) != 1)
        score -= 2;
      if (CountOut.findWinner(3, 4) != 1)
        score -= 2;
      // k == 1
      if (CountOut.findWinner(10, 1) != 9)
        score -= 2;
    } catch (Exception e) {
      scoreSheet.addSection(testName, 0, MAXSCORE, stackTraceToString(e));
      return;
    }
    score = score < 0 ? 0 : score;
    if (score == MAXSCORE) {
      scoreSheet.addSection(testName, score, MAXSCORE, "");
    } else {
      scoreSheet.addSection(testName, score, MAXSCORE, "Incorrect return value.");
    }
  }

  private static void countOutQueueTestWinnerRec(ScoreSheet scoreSheet) {
    String testName = "CountOut recursive , find winner";
    int MAXSCORE = 15;
    int score = MAXSCORE;

    StringBuilder message = new StringBuilder();
    message.append("Incorrect return value. ");
    try {
      // Homework example
      if (CountOut.findWinnerRec(10, 4) != 4) {
        score -= 5;
      }
      // k == N
      if (CountOut.findWinnerRec(4, 4) != 1) {
        score -= 5;
      }
      // k > N
      if (CountOut.findWinnerRec(1, 4) != 0) {
        score -= 5;
      }
      if (CountOut.findWinnerRec(2, 4) != 0) {
        score -= 5;
      }
      if (CountOut.findWinnerRec(2, 3) != 1) {
        score -= 5;
      }
      if (CountOut.findWinnerRec(3, 4) != 1) {
        score -= 5;
      }
      // k == 1 // I bet a lot of them get this wrong
      if (CountOut.findWinnerRec(10, 1) != 9) {
        score -= 5;
        message.append("what happens with k=1?\n");
      }
      ;
    } catch (Exception e) {
      scoreSheet.addSection(testName, 0, MAXSCORE, stackTraceToString(e));
      return;
    }
    score = score < 0 ? 0 : score;
    if (score == MAXSCORE) {
      scoreSheet.addSection(testName, score, MAXSCORE, "");
    } else {
      scoreSheet.addSection(testName, score, MAXSCORE, message.toString().trim());
    }
  }

  private static void bufferTest(ScoreSheet scoreSheet) throws IOException {
    String testName = "FastBuffer test";
    int MAXSCORE = 26;
    int score = MAXSCORE;

    StringBuilder message = new StringBuilder();

    StringBuilder testStringBuilder = new StringBuilder();
    BufferedReader testReader = new BufferedReader(new InputStreamReader(new FileInputStream("loremipsum.txt")));
    String chars;
    while ((chars = testReader.readLine()) != null) {
      testStringBuilder.append(chars);
    }
    String testString = testStringBuilder.toString();

    try {
      Buffer studentBuffer = new FastBuffer();

      // Try inserting chars
      for (char c : testString.toCharArray()) {
        studentBuffer.insertLeft(c);
      }

      // Size okay?
      if (studentBuffer.size() != testString.length()) {
        score -= 6;
        message.append("Incorrect size.");
      }

      // Set cursor
      studentBuffer.setCursor(10);

      // Move right
      studentBuffer.moveRight(); // -> 11
      studentBuffer.moveRight(); // -> 12
      studentBuffer.moveLeft(); // -> 11

      // Check characters correct
      boolean movesOkay = true;
      if (studentBuffer.deleteRight() != testString.charAt(11)) {
        score -= 5;
        movesOkay = false;
      }
      if (studentBuffer.deleteLeft() != testString.charAt(10)) {
        score -= 5;
        movesOkay = false;
      }
      if (studentBuffer.deleteLeft() != testString.charAt(9)) {
        score -= 5;
        movesOkay = false;
      }
      if (!movesOkay)
        message.append("Cursor moves and/or deletes don't work. ");

      // Size okay?
      if (studentBuffer.size() != testString.length() - 3) {
        score -= 5;
        message.append("Size incorrect after deletes.");
      }

      // Test toArray
      studentBuffer = new FastBuffer();
      for (char c : testString.toCharArray()) {
        studentBuffer.insertLeft(c);
      }
      // moves should not affect toArray
      studentBuffer.setCursor(5);
      studentBuffer.moveRight();
      studentBuffer.moveRight();
      studentBuffer.moveRight();

      StringBuilder studentArrayStringBuilder = new StringBuilder();
      for (char c : studentBuffer.toArray()) {
        studentArrayStringBuilder.append(c);
      }
      if (!studentArrayStringBuilder.toString().equals(testString)) {
        score -= 5;
        message.append("toArray() return value incorrect. ");
        System.out.println(studentArrayStringBuilder.toString());
        System.out.println(testString);
      }

      if (score == MAXSCORE) {
        scoreSheet.addSection(testName, score, MAXSCORE, "");
      } else {
        scoreSheet.addSection(testName, score < 0 ? 0 : score, MAXSCORE, message.toString().trim());
      }
    } catch (Exception e) {
      scoreSheet.addSection(testName, 0, MAXSCORE, stackTraceToString(e));
      return;
    }
    testReader.close();
  }

  /** Return true if the two iterables are identical. */
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

}
