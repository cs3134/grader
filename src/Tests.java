import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
		scoreSheet.studentMax = Integer.parseInt(properties
				.getProperty("studentMax"));
		int timeLimitSeconds = Integer.parseInt(properties
				.getProperty("timeLimitSeconds"));

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
			System.out.println(scoreSheet.toJSONString());
			System.exit(1);
		} catch (ExecutionException e) {
			scoreSheet.errorMessage = "Timed out";
			System.out.println(scoreSheet.toJSONString());
			System.exit(1);
		} catch (TimeoutException e) {
			scoreSheet.errorMessage = "Timed out";
			System.out.println(scoreSheet.toJSONString());
			System.exit(1);
		}

		if (!executor.isTerminated()) {
			executor.shutdownNow();
		}

		System.out.println(scoreSheet.toJSONString());

		String postUrl = "http://jarvis.xyz/webhook/curl";
		HttpPost post = new HttpPost(postUrl);
		StringEntity postingString = new StringEntity(scoreSheet.toJSONString());
		post.setEntity(postingString);
		post.setHeader("Content-type", "application/json");
		HttpClient httpClient = HttpClientBuilder.create().build();
		httpClient.execute(post);

		return scoreSheet;
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
		// SimpleLinkedList indexOf()
		indexOfTest(scoreSheet);

		// SimpleLinkedList reverse()
		reverseTest(scoreSheet);

		// SimpleLinkedList removeDuplicates()
		removeDuplicatesTest(scoreSheet);

		// SimpleLinkedList interleave()
		interleaveTest(scoreSheet);

		// Range positive increment
		rangePositiveTest(scoreSheet);

		// Range negative increment
		rangeNegativeTest(scoreSheet);

		return scoreSheet;
	}

	private static void rangePositiveTest(ScoreSheet scoreSheet) {
		String testName = "Range positive increment";
		boolean failed = false;
		try {
			Integer[] list1 = new Integer[] { 1, 2, 3, 4 };
			if (!checkIterationEqual(new Range(1, 5, 1), Arrays.asList(list1))) {
				failed = true;
			}

			Integer[] list2 = new Integer[] { 1, 2, 3, 4 };
			if (!checkIterationEqual(new Range(1, 5), Arrays.asList(list2))) {
				failed = true;
			}

			Integer[] list3 = new Integer[] { -3, 0, 3 };
			if (!checkIterationEqual(new Range(-3, 6, 3), Arrays.asList(list3))) {
				failed = true;
			}
			if (!failed) {
				scoreSheet.addSection(testName, 15, 15, "");
			} else {
				scoreSheet.addSection(testName, 0, 15, "Incorrect output");
			}
		} catch (Exception e) {
			scoreSheet.addSection(testName, 0, 15, stackTraceToString(e));
		}
	}

	private static void rangeNegativeTest(ScoreSheet scoreSheet) {
		String testName = "Range negative increment";
		boolean failed = false;
		try {
			Integer[] list1 = new Integer[] { 5, 4, 3, 2 };
			if (!checkIterationEqual(new Range(5, 1, -1), Arrays.asList(list1))) {
				failed = true;
			}

			Integer[] list2 = new Integer[] { 6, 3, 0 };
			if (!checkIterationEqual(new Range(6, -3, -3), Arrays.asList(list2))) {
				failed = true;
			}

			if (!failed) {
				scoreSheet.addSection(testName, 15, 15, "");
			} else {
				scoreSheet.addSection(testName, 0, 15, "Incorrect output");
			}
		} catch (Exception e) {
			scoreSheet.addSection(testName, 0, 15, stackTraceToString(e));
		}
	}

	private static boolean checkIterationEqual(Iterable<Integer> student,
			Iterable<Integer> gold) {
		Iterator<Integer> studentIterator = student.iterator();
		Iterator<Integer> goldIterator = gold.iterator();
		while (goldIterator.hasNext()) {
			if (studentIterator.next() != goldIterator.next()) {
				return false;
			}
		}
		return true;
	}

	private static void indexOfTest(ScoreSheet scoreSheet) {
		String testName = "SimpleLinkedList indexOf()";
		try {
			SimpleLinkedList<Integer> studentList = new SimpleLinkedList<Integer>();
			LinkedList<Integer> javaList = new LinkedList<Integer>();

			int size = 100;
			for (int i = 0; i < size; i++) {
				int element = (int) (size * Math.random());
				studentList.add(element);
				javaList.add(element);
			}

			int testSize = 10;

			boolean failed = false;

			for (int i = 0; i < testSize; i++) {
				int element = (int) (size * Math.random());
				if (studentList.indexOf(element) != javaList.indexOf(element)) {
					failed = true;
				}
			}
			if (!failed) {
				scoreSheet.addSection(testName, 14, 14, "");
			} else {
				scoreSheet.addSection(testName, 0, 14, "Incorrect output");
			}
		} catch (Exception e) {
			scoreSheet.addSection(testName, 0, 14, stackTraceToString(e));
		}
	}

	private static void reverseTest(ScoreSheet scoreSheet) {
		String testName = "SimpleLinkedList reverse()";
		try {
			SimpleLinkedList<Integer> studentList = new SimpleLinkedList<Integer>();
			LinkedList<Integer> javaList = new LinkedList<Integer>();

			int size = 100;
			for (int i = 0; i < size; i++) {
				int element = (int) (size * Math.random());
				studentList.add(element);
				javaList.add(element);
			}

			Collections.reverse(javaList);
			studentList.reverse();

			boolean failed = false;

			Iterator<Integer> iteJavaList = javaList.iterator();
			Iterator<Integer> iteStudentList = studentList.iterator();

			while (iteJavaList.hasNext()) {
				if (iteJavaList.next() != iteStudentList.next()) {
					failed = true;
				}
			}

			if (!failed) {
				scoreSheet.addSection(testName, 16, 16, "");
			} else {
				scoreSheet.addSection(testName, 0, 16, "Incorrect output");
			}
		} catch (Exception e) {
			scoreSheet.addSection(testName, 0, 16, stackTraceToString(e));
		}
	}

	private static void removeDuplicatesTest(ScoreSheet scoreSheet) {
		String testName = "SimpleLinkedList removeDuplicates()";
		try {
			SimpleLinkedList<Integer> studentList = new SimpleLinkedList<Integer>();
			LinkedList<Integer> javaList = new LinkedList<Integer>();

			int size = 100;
			int modFactor = 20;

			for (int i = 0; i < size; i++) {
				int element = ((int) (size * Math.random())) % modFactor;
				studentList.add(element);
				javaList.add(element);
			}

			LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<Integer>(
					javaList);
			javaList.clear();
			javaList.addAll(linkedHashSet);
			studentList.removeDuplicates();

			boolean failed = false;

			Iterator<Integer> iteJavaList = javaList.iterator();
			Iterator<Integer> iteStudentList = studentList.iterator();

			while (iteJavaList.hasNext()) {
				if (iteJavaList.next() != iteStudentList.next()) {
					failed = true;
				}
			}

			if (!failed) {
				scoreSheet.addSection(testName, 20, 20, "");
			} else {
				scoreSheet.addSection(testName, 0, 20, "Incorrect output");
			}
		} catch (Exception e) {
			scoreSheet.addSection(testName, 0, 20, stackTraceToString(e));
		}
	}

	private static void interleaveTest(ScoreSheet scoreSheet) {
		String testName = "SimpleLinkedList interleave()";
		try {
			SimpleLinkedList<Integer> studentList1 = new SimpleLinkedList<Integer>();
			SimpleLinkedList<Integer> studentList2 = new SimpleLinkedList<Integer>();

			for (int i = 1; i <= 3; i++) {
				studentList1.add(i);
			}

			for (int i = 10; i >= 6; i--) {
				studentList2.add(i);
			}

			List<Integer> targetList1 = java.util.Arrays.asList(1, 10, 2, 9, 3, 8, 7,
					6);
			List<Integer> targetList2 = java.util.Arrays.asList(10, 1, 9, 2, 8, 3, 7,
					6);

			boolean failed = false;

			studentList1.interleave(studentList2);
			Iterator<Integer> studentListIter = studentList1.iterator();
			for (Integer i : targetList1) {
				if (i != studentListIter.next()) {
					failed = true;
				}
			}

			studentList1 = new SimpleLinkedList<Integer>();
			for (int i = 1; i <= 3; i++) {
				studentList1.add(i);
			}

			studentList2.interleave(studentList1);
			studentListIter = studentList2.iterator();
			for (Integer i : targetList2) {
				if (i != studentListIter.next()) {
					failed = true;
				}
			}

			if (!failed) {
				scoreSheet.addSection(testName, 20, 20, "");
			} else {
				scoreSheet.addSection(testName, 0, 20, "Incorrect output");
			}
		} catch (Exception e) {
			scoreSheet.addSection(testName, 0, 20, stackTraceToString(e));
		}
	}
}
