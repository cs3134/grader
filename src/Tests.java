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
    testUndirectedEdges(scoreSheet);
    testReadMap(scoreSheet);
    testComputeEuclideanCost(scoreSheet);
    testComputeAllEuclideanCosts(scoreSheet);
    testBfs(scoreSheet);
    testDijkstra(scoreSheet);
    testPrim(scoreSheet);
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
    int sectionScoreMax = 30;

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
              scoreSheet.addSection(sectionName + fileName + " found", 30, sectionScoreMax, "");
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

  private static void testUndirectedEdges(ScoreSheet scoreSheet) {
    String sectionName = "addUndirectedEdge";
    int sectionScoreMax;
    sectionScoreMax = 8;
    try {
      Graph g = new Graph();
      g.addUndirectedEdge("a", "b", 5.0);
      Vertex v1 = g.vertices.get("a");
      Vertex v2 = g.vertices.get("b");
      boolean found1 = false;
      boolean found2 = false;
      for (Edge e : v1.getEdges())
        if (e.targetVertex == v2 && e.cost == 5.0)
          found1 = true;
      for (Edge e : v2.getEdges())
        if (e.targetVertex == v1 && e.cost == 5.0)
          found2 = true;
      if (!found1 || !found2) {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Missing edges or incorrect weights.");
        return;
      }
      g.addUndirectedEdge("a", "c", 2.0);
      if (g.vertices.values().size() != 3) {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Second edge on the same node incorrect.");
        return;
      }
      found1 = false;
      found2 = false;
      v1 = g.vertices.get("a");
      v2 = g.vertices.get("c");
      for (Edge e : v1.getEdges())
        if (e.targetVertex == v2 && e.cost == 2.0)
          found1 = true;
      for (Edge e : v2.getEdges())
        if (e.targetVertex == v1 && e.cost == 2.0)
          found2 = true;
      if (!found1 || !found2) {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Second edge on the same node incorrect.");
        return;
      }
      scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testReadMap(ScoreSheet scoreSheet) {
    String sectionName = "MapReader.readGraph";
    int sectionScoreMax;
    sectionScoreMax = 13;
    try {
      Graph g = MapReader.readGraph("ttrvertices_test.txt", "ttredges_test.txt");
      int notfound = 4;
      for (Vertex v : g.getVertices()) {
        if (v.name.equals("Denver")) {
          for (Edge e : v.getEdges()) {
            if (e.targetVertex.name.equals("Seattle") || e.targetVertex.name.equals("SanFrancisco")
                || e.targetVertex.name.equals("Nashville") || e.targetVertex.name.equals("Montreal"))
              notfound--;
            else {
              scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Found invalid edge in read graph.");
              return;
            }
          }
          break;
        }
      }
      if (notfound > 0) {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Missing edge in read graph.");
        return;
      }
      scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
      return;
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testComputeEuclideanCost(ScoreSheet scoreSheet) {
    String sectionName = "testComputeEuclideanCost";
    int sectionScoreMax;
    sectionScoreMax = 5;
    try {
      Graph g = new Graph();
      double result = g.computeEuclideanCost(12.1, 3.5, 7.0, 123.0);
      if (result < 119.60877894201578 || result > 119.60877894201580)
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Incorrect result.");
      else
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testComputeAllEuclideanCosts(ScoreSheet scoreSheet) {
    String sectionName = "testComputeAllEuclideanCost";
    int sectionScoreMax;
    sectionScoreMax = 5;
    try {
      Graph g = MapReader.readGraph("ttrvertices_test.txt", "ttredges_test.txt");
      g.computeAllEuclideanCosts();
      int incorrect = 3;
      for (Vertex v : g.getVertices()) {
        if (v.name.equals("Denver")) {
          for (Edge e : v.getEdges()) {
            if (e.targetVertex.name.equals("Seattle") && e.cost > 279.401145309034 && e.cost < 279.401145309035) {
              incorrect--;
            }
            if (e.targetVertex.name.equals("Montreal") && e.cost > 454.963734818501 && e.cost < 454.963734818502) {
              incorrect--;
            }
          }
        }
        if (v.name.equals("Seattle"))
          for (Edge e : v.getEdges())
            if (e.targetVertex.name.equals("Denver") && e.cost > 279.401145309034 && e.cost < 279.401145309035)
              incorrect--;
      }
      if (incorrect > 0)
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Incorrect euclidean edge costs.");
      else
        scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testBfs(ScoreSheet scoreSheet) {
    String sectionName = "doBfs";
    int sectionScoreMax = 13;

    try {
      Graph g = MapReader.readGraph("ttrvertices_test.txt", "ttredges_test.txt");

      StringBuilder errors = new StringBuilder();
      int score = 13;
      g.doBfs("Houston");
      // Test shortest path costs
      if (g.vertices.get("Houston").cost != 0.0 || g.vertices.get("Montreal").cost != 3.0
          || g.vertices.get("Seattle").cost != 3.0) {
        score = score - 8;
        errors.append("Invalid cost annotations.");
      }
      for (Vertex v : g.getVertices())
        if (!v.visited) {
          score = score - 8;
          errors.append(" Unvisited vertices.");
        }

      if (g.vertices.get("Houston").backpointer != null
          || !(g.vertices.get("Montreal").backpointer.name.equals("Denver"))
          || !(g.vertices.get("Denver").backpointer.name.equals("Nashville"))
          || (!g.vertices.get("Nashville").backpointer.name.equals("Houston"))) {
        score = score - 8;
        errors.append(" Incorrect backpointers.");
      }

      g = MapReader.readGraph("ttrvertices.txt", "ttredges.txt");
      g.doBfs("Calgary");
      LinkedList<String> path = new LinkedList<String>();
      Vertex u = g.vertices.get("NewYork");
      path.addFirst(u.name);
      while (u.backpointer != null) {
        u = u.backpointer;
        path.addFirst(u.name);
      }
      String[] goldList = { "Calgary", "Winnipeg", "SaultSaintMarie", "Montreal", "NewYork" };
      if (!compareCollections(path, Arrays.asList(goldList))) {
        score = score - 8;
        errors.append("Incorrect shortest path between Houston and Montreal. Should be " + Arrays.toString(goldList));
      }
      ;

      if (score < 0)
        score = 0;
      scoreSheet.addSection(sectionName, score, sectionScoreMax, errors.toString());
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }

  }

  private static void testDijkstra(ScoreSheet scoreSheet) {
    String sectionName = "doDijkstra";
    StringBuilder errors = new StringBuilder();
    int sectionScoreMax = 13;
    int score = 13;
    try {
      Graph g = MapReader.readGraph("ttrvertices_test.txt", "ttredges_test.txt");

      g.computeAllEuclideanCosts();
      g.doDijkstra("Houston");

      // Test shortest path costs
      if (g.vertices.get("Houston").cost != 0.0 || (g.vertices.get("Nashville").cost < 162.788)
          || (g.vertices.get("Nashville").cost > 162.789) || (g.vertices.get("Pittsburgh").cost < 305.966)
          || (g.vertices.get("Pittsburgh").cost > 305.967) || (g.vertices.get("NewYork").cost < 375.428)
          || (g.vertices.get("NewYork").cost > 375.429) || (g.vertices.get("Montreal").cost < 474.509)
          || (g.vertices.get("Montreal").cost > 474.510)) {

        score = score - 8;
        errors.append("Invalid cost annotations.");
      }
      for (Vertex v : g.getVertices())
        if (!v.visited) {
          score = score - 8;
          errors.append("Unvisited vertices.");
        }

      if (g.vertices.get("Houston").backpointer != null
          || !(g.vertices.get("Montreal").backpointer.name.equals("NewYork"))
          || !(g.vertices.get("NewYork").backpointer.name.equals("Pittsburgh"))
          || (!g.vertices.get("Pittsburgh").backpointer.name.equals("Nashville"))
          || (!g.vertices.get("Nashville").backpointer.name.equals("Houston"))) {
        score = score - 8;
        errors.append(" Incorrect backpointers.");
      }

      g = MapReader.readGraph("ttrvertices.txt", "ttredges.txt");
      g.computeAllEuclideanCosts();
      g.doDijkstra("Calgary");
      LinkedList<String> path = new LinkedList<String>();
      Vertex u = g.vertices.get("NewYork");
      path.addFirst(u.name);
      while (u.backpointer != null) {
        u = u.backpointer;
        path.addFirst(u.name);
      }
      String[] goldList = { "Calgary", "Winnipeg", "Duluth", "Chicago", "Pittsburgh", "NewYork" };
      if (!compareCollections(path, Arrays.asList(goldList))) {
        score = score - 8;
        errors.append("Incorrect shortest path between Houston and Montreal. Should be " + Arrays.toString(goldList));
      }
      ;

      if (score < 0)
        score = 0;
      scoreSheet.addSection(sectionName, score, sectionScoreMax, errors.toString());
    } catch (Exception e) {
      scoreSheet.addSection(sectionName, 0, sectionScoreMax, stackTraceToString(e));
    }
  }

  private static void testPrim(ScoreSheet scoreSheet) {
    String sectionName = "doPrim";
    StringBuilder errors = new StringBuilder();
    int sectionScoreMax = 13;
    try {
      Graph g = MapReader.readGraph("ttrvertices_test.txt", "ttredges_test.txt");

      g.computeAllEuclideanCosts();
      g.doPrim("Denver");

      double sum = 0.0;
      HashMap<String, HashSet<String>> tree = new HashMap<>();
      String vname;
      String pname;
      for (Vertex v : g.getVertices()) {
        vname = v.name;
        if (v.backpointer != null) {
          pname = v.backpointer.name;
          sum = sum + v.cost;
          if (!tree.containsKey(pname))
            tree.put(pname, new HashSet<>());
          tree.get(pname).add(vname);
        }
      }

      Set<String> seen = new HashSet();
      LinkedList<String> stack = new LinkedList<>();
      stack.push("Denver");
      String next;
      boolean has_cycle = false;

      while (!stack.isEmpty()) {
        next = stack.pop();
        seen.add(next);
        if (tree.containsKey(next))
          for (String child : tree.get(next)) {
            if (seen.contains(child))
              has_cycle = true;
            stack.push(child);
          }
      }
      if (has_cycle || (seen.size() < g.vertices.size())) {
        scoreSheet.addSection(sectionName, 0, sectionScoreMax, "Result is not a spanning tree.");
        return;
      }
      if (sum > 1675.40589624302) {
        scoreSheet.addSection(sectionName, 5, sectionScoreMax, "Spanning tree is not minimal.");
        return;
      }
      scoreSheet.addSection(sectionName, sectionScoreMax, sectionScoreMax, "");
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
