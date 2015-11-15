import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    testKBest(scoreSheet);
    testIterativeMergeSort(scoreSheet);
    testMergeSortList(scoreSheet);
    return scoreSheet;
  }

  private static void searchTheory(ScoreSheet scoreSheet) {
    File folder = new File("./" + scoreSheet.homeworkName.replaceAll("hw", "") + "/");
    File[] listOfFiles = folder.listFiles();

    HashSet<String> ignoredFileNames = new HashSet<>();
    ignoredFileNames.add("readme.md");
    ignoredFileNames.add("comments.txt");

    HashSet<String> acceptedFileExtensions = new HashSet<>();
    acceptedFileExtensions.add("txt");
    acceptedFileExtensions.add("pdf");
    acceptedFileExtensions.add("md");

    long maxFileSize = 600000; // 500kb

    String sectionName = "Theory: ";
    int sectionScoreMax = 40;

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
              scoreSheet.addSection(sectionName + fileName + " found", 40, sectionScoreMax, "");
              return;
            }
          }
        }
      }
    }
    scoreSheet.addSection(sectionName + "no theory submission detected", 0, sectionScoreMax,
        "We could not find your theory submission in the /4/ folder. Please place it in the folder (and not in /src/ or /bin/ or any other folders)");
  }

  private static String getExtension(String fileName) {
    String extension = "";

    int i = fileName.lastIndexOf('.');
    if (i > 0) {
      extension = fileName.substring(i + 1);
    }
    return extension;
  }

  private static void testKBest(ScoreSheet scoreSheet) {
    String sectionName;
    int sectionScoreMax;

    // less than k
    sectionName = "KBestCounter.count(): added x counts without exceptions thrown (x < k)";
    sectionScoreMax = 5;

    int k = 5;
    List<Integer> numbers = Arrays.asList(new Integer[] { 1, 2, 3 });
    KBestCounter<Integer> counter = new KBestCounter<>(k);
    try {
      numbers.stream().forEach(x -> {
        counter.count(x);
      });
      scoreSheet.addSection(sectionName, 5, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "KBestCounter.kbest(): returned x largest elements (x < k) after x counts";
    sectionScoreMax = 3;
    try {
      List<Integer> kbestStudent = counter.kbest();
      List<Integer> kbestGold = Arrays.asList(new Integer[] { 3, 2, 1 });
      if (compareCollections(kbestStudent, kbestGold)) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + kbestGold + "\nActual: " + kbestStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "KBestCounter.count(): added x counts without exceptions thrown (x > k)";
    sectionScoreMax = 5;
    try {
      for (int i = 4; i < 100; i++) {
        counter.count(i);
      }
      scoreSheet.addSection(sectionName, 5, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "KBestCounter.kbest(): kbest() still returns correctly after adding more elements";
    sectionScoreMax = 4;
    try {
      List<Integer> kbestStudent = counter.kbest();
      List<Integer> kbestGold = Arrays.asList(new Integer[] { 99, 98, 97, 96, 95 });
      if (compareCollections(kbestStudent, kbestGold)) {
        scoreSheet.addSection(sectionName, 4, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + kbestGold + "\nActual: " + kbestStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "KBestCounter.kbest(): Added two more elements, kbest() should return new list";
    sectionScoreMax = 3;
    try {
      counter.count(101);
      counter.count(102);
      counter.count(100);
      List<Integer> kbestStudent = counter.kbest();
      List<Integer> kbestGold = Arrays.asList(new Integer[] { 102, 101, 100, 99, 98 });
      if (compareCollections(kbestStudent, kbestGold)) {
        scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + kbestGold + "\nActual: " + kbestStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testIterativeMergeSort(ScoreSheet scoreSheet) {
    String sectionName;
    int sectionScoreMax;

    sectionName = "MergeSort.mergeSortB(): correct result for power 2 sized input array";
    sectionScoreMax = 10;
    try {
      List<Integer> inputList = IntStream.range(1, 9).boxed().collect(Collectors.toList());
      Collections.shuffle(inputList);
      Integer[] input = new Integer[8];
      inputList.toArray(input);
      MergeSort.mergeSort(input);
      Integer[] gold = new Integer[8];
      IntStream.range(1, 9).boxed().collect(Collectors.toList()).toArray(gold);
      if (Arrays.equals(input, gold)) {
        scoreSheet.addSection(sectionName, 10, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + gold + "\nActual: " + input);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "MergeSort.mergeSortB(): correct result for non-power 2 sized input array";
    sectionScoreMax = 10;
    try {
      List<Integer> inputList = IntStream.range(1, 12).boxed().collect(Collectors.toList());
      Collections.shuffle(inputList);
      Integer[] input = new Integer[11];
      inputList.toArray(input);
      MergeSort.mergeSort(input);
      Integer[] gold = new Integer[11];
      IntStream.range(1, 12).boxed().collect(Collectors.toList()).toArray(gold);
      if (Arrays.equals(input, gold)) {
        scoreSheet.addSection(sectionName, 10, sectionScoreMax, "");
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + gold + "\nActual: " + input);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testMergeSortList(ScoreSheet scoreSheet) {
    String sectionName;
    int sectionScoreMax;

    sectionName = "MergeSort.mergeLists(): merges two ascending lists, each size 1, correctly";
    sectionScoreMax = 3;
    try {
      List<Integer> left = Arrays.asList(new Integer[] { 1 });
      List<Integer> leftGold = new LinkedList<Integer>(left);
      List<Integer> right = Arrays.asList(new Integer[] { 2 });
      List<Integer> rightGold = new LinkedList<Integer>(right);
      List<Integer> resultGold = Arrays.asList(new Integer[] { 1, 2 });

      List<Integer> resultStudent = MergeSort.mergeLists(left, right);

      if (compareCollections(resultStudent, resultGold)) {
        if (compareCollections(left, leftGold) && compareCollections(right, rightGold)) {
          scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
        } else {
          scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Original left and right lists modified during merge");
        }
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + resultGold + "\nActual: " + resultStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "MergeSort.mergeLists(): merges two ascending lists of same sizes correctly";
    sectionScoreMax = 4;
    try {
      List<Integer> left = Arrays.asList(new Integer[] { 1, 3 });
      List<Integer> leftGold = new LinkedList<Integer>(left);
      List<Integer> right = Arrays.asList(new Integer[] { 2, 4 });
      List<Integer> rightGold = new LinkedList<Integer>(right);
      List<Integer> resultGold = Arrays.asList(new Integer[] { 1, 2, 3, 4 });

      List<Integer> resultStudent = MergeSort.mergeLists(left, right);

      if (compareCollections(resultStudent, resultGold)) {
        if (compareCollections(left, leftGold) && compareCollections(right, rightGold)) {
          scoreSheet.addSection(sectionName, 4, sectionScoreMax, "");
        } else {
          scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Original left and right lists modified during merge");
        }
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + resultGold + "\nActual: " + resultStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "MergeSort.mergeLists(): merges two ascending lists of different sizes correctly";
    sectionScoreMax = 3;
    try {
      List<Integer> left = Arrays.asList(new Integer[] { 1, 3, 5, 6 });
      List<Integer> leftGold = new LinkedList<Integer>(left);
      List<Integer> right = Arrays.asList(new Integer[] { 2, 4 });
      List<Integer> rightGold = new LinkedList<Integer>(right);
      List<Integer> resultGold = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });

      List<Integer> resultStudent = MergeSort.mergeLists(left, right);

      if (compareCollections(resultStudent, resultGold)) {
        if (compareCollections(left, leftGold) && compareCollections(right, rightGold)) {
          scoreSheet.addSection(sectionName, 3, sectionScoreMax, "");
        } else {
          scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Original left and right lists modified during merge");
        }
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + resultGold + "\nActual: " + resultStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "MergeSort.sortList(): correct result for power 2 sized list";
    sectionScoreMax = 5;
    try {
      List<Integer> inputList = IntStream.range(1, 9).boxed().collect(Collectors.toList());
      Collections.shuffle(inputList);
      List<Integer> inputGold = new LinkedList<Integer>(inputList);
      List<Integer> resultGold = IntStream.range(1, 9).boxed().collect(Collectors.toList());

      List<Integer> resultStudent = MergeSort.sortList(inputList);

      if (compareCollections(resultStudent, resultGold)) {
        if (compareCollections(inputList, inputGold)) {
          scoreSheet.addSection(sectionName, 5, sectionScoreMax, "");
        } else {
          scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Original left and right lists modified during merge");
        }
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + resultGold + "\nActual: " + resultStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

    sectionName = "MergeSort.sortList(): correct result for non-power 2 sized list";
    sectionScoreMax = 5;
    try {
      List<Integer> inputList = IntStream.range(1, 12).boxed().collect(Collectors.toList());
      Collections.shuffle(inputList);
      List<Integer> inputGold = new LinkedList<Integer>(inputList);
      List<Integer> resultGold = IntStream.range(1, 12).boxed().collect(Collectors.toList());

      List<Integer> resultStudent = MergeSort.sortList(inputList);

      if (compareCollections(resultStudent, resultGold)) {
        if (compareCollections(inputList, inputGold)) {
          scoreSheet.addSection(sectionName, 5, sectionScoreMax, "");
        } else {
          scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Original left and right lists modified during merge");
        }
      } else {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax,
            "List incorrect\nExpected: " + resultGold + "\nActual: " + resultStudent);
      }
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
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
