import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
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
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    } catch (ExecutionException e) {
      postJson(scoreSheet);
      System.out.println(scoreSheet.toJSONString());
      System.exit(0);
    } catch (TimeoutException e) {
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

  /**
   * Graders, you should only edit this. No more.
   *
   * @param scoreSheet
   * @return
   * @throws IOException
   */
  private static ScoreSheet tests(ScoreSheet scoreSheet) throws IOException {
    searchTheory(scoreSheet);
    testSuperSoda(scoreSheet);

    return scoreSheet;
  }

  private static void searchTheory(ScoreSheet scoreSheet) {
    File folder = new File("./" + scoreSheet.homeworkName.replaceAll("hw", "") + "/");
    File[] listOfFiles = folder.listFiles();
    HashSet<String> ignoredFileNames = new HashSet<>();
    ignoredFileNames.add("readme.md");
    ignoredFileNames.add("ttredges_test.txt");
    ignoredFileNames.add("ttredges.txt");
    ignoredFileNames.add("ttrvertices_test.txt");
    ignoredFileNames.add("ttrvertices.txt");

    HashSet<String> acceptedFileExtensions = new HashSet<>();
    acceptedFileExtensions.add("txt");
    acceptedFileExtensions.add("pdf");
    acceptedFileExtensions.add("md");

    long maxFileSize = 600000; // 500kb

    String sectionName = "Theory: ";
    int sectionScoreMax = 42;

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        String fileName = listOfFiles[i].getName().toLowerCase();
        if (!ignoredFileNames.contains(fileName)) {
          if (acceptedFileExtensions.contains(getExtension(fileName))) {
            // found file
            System.out.println("Found theory file: " + fileName);
            System.out.println("Size: " + listOfFiles[i].length());
            if (listOfFiles[i].length() > maxFileSize) {
              scoreSheet.addSection(sectionName + fileName + " found", 0, sectionScoreMax,
                  fileName + " (" + (listOfFiles[i].length() / 1000) + "kb) exceeds max file size of 500kb.");
              return;
            } else {
              scoreSheet.addSection(sectionName + fileName + " found", sectionScoreMax, sectionScoreMax, "");
              return;
            }
          }
        }
      }
    }
    scoreSheet.addSection(sectionName + "no theory submission detected", 0, sectionScoreMax,
        "We could not find your theory submission in the " + "./" + scoreSheet.homeworkName.replaceAll("hw", "")
            + "/ folder. Please place it in the folder (and not in /src/ or /bin/ or any other folders)");
  }

  private static String getExtension(String fileName) {
    String extension = "";

    int i = fileName.lastIndexOf('.');
    if (i > 0) {
      extension = fileName.substring(i + 1);
    }
    return extension;
  }

  private static void testSuperSoda(ScoreSheet scoreSheet) {
    String sectionName;
    int sectionScoreMax;

    int[] sodaSizes = new int[] { 1, 6, 12, 25, 36 };
    double[] costs = new double[] { 0.8, 4, 7.5, 14, 20 };
    double errorMargin = 0.00001;

    // minimum soda cost
    sectionName = "SuperSoda.minimalSodaCost: n = 100";
    sectionScoreMax = 10;
    try {
      double student = SuperSoda.minimalSodaCost(sodaSizes, costs, 100);
      double correct = 56.0;
      if (doubleEquals(correct, student, errorMargin)) {
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "Wrong value\nActual: " + student + "\nExpected: " + correct);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SuperSoda.minimalSodaCost: n = 1337";
    sectionScoreMax = 19;
    try {
      double student = SuperSoda.minimalSodaCost(sodaSizes, costs, 1337);
      double correct = 743.6;
      if (doubleEquals(correct, student, errorMargin)) {
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "Wrong value\nActual: " + student + "\nExpected: " + correct);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SuperSoda.maximumSodaNumber: cost = 25";
    sectionScoreMax = 10;
    try {
      int student = SuperSoda.maximumSodaNumber(sodaSizes, costs, 25);
      int correct = 43;
      if (student == correct) {
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "Wrong value\nActual: " + student + "\nExpected: " + correct);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SuperSoda.maximumSodaNumber: cost = 53.0";
    sectionScoreMax = 19;
    try {
      int student = SuperSoda.maximumSodaNumber(sodaSizes, costs, 53.0);
      int correct = 93;
      if (student == correct) {
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "Wrong value\nActual: " + student + "\nExpected: " + correct);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "SuperSoda.minimalSodaCostCombinations: n = 1337";
    sectionScoreMax = 20;
    try {
      int[] student = SuperSoda.minimalSodaCostCombinations(sodaSizes, costs, 1337);
      int[] correct = new int[] { 2, 0, 0, 3, 35 };
      if (Arrays.equals(student, correct)) {
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "Wrong value\nActual: " + Arrays.toString(student) + "\nExpected: " + Arrays.toString(correct));
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

  }

  private static boolean doubleEquals(double a, double b, double epsilon) {
    return (Math.abs(a - b) < epsilon);
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
}
