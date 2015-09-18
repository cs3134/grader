import java.io.IOException;

public class Grader {
	public static void main(String[] args) throws IOException {
		ScoreSheet scoreSheet = new ScoreSheet();
		Tests.runTests(scoreSheet);
	}
}
