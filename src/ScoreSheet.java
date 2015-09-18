import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class ScoreSheet {

	public String homeworkName;
	public String className;
	private List<Section> sections;
	public String errorMessage;
	public int studentMax;

	public ScoreSheet() {
		this.sections = new LinkedList<Section>();
		errorMessage = "";
		studentMax = 0;
	}

	public void addSection(String sectionName, int sectionScoreStudent,
			int sectionScoreMax, String sectionErrorMessage) {
		Section section = new Section(sectionName, sectionScoreStudent,
				sectionScoreMax, sectionErrorMessage);
		sections.add(section);
	}

	public String toJSONString() {
		JSONObject object = new JSONObject();
		object.put("reponame", System.getenv("CIRCLE_PROJECT_REPONAME"));
		object.put("homeworkName", homeworkName);
		object.put("className", className);

		int studentScore = sections.stream()
				.mapToInt((Section section) -> section.sectionScoreStudent).sum();

		object.put("studentScore", studentScore);
		object.put("studentMax", studentMax);

		object.put("sections", getSections());

		object.put("errorMessage", errorMessage);

		return object.toJSONString();
	}

	private JSONArray getSections() {
		JSONArray array = new JSONArray();
		for (Section section : sections) {
			array.add(section.getJSONObject());
		}
		return array;
	}

	private class Section {
		public String sectionName;
		public int sectionScoreStudent;
		public int sectionScoreMax;
		public String sectionErrorMessage;

		public Section(String sectionName, int sectionScoreStudent,
				int sectionScoreMax, String sectionErrorMessage) {
			this.sectionName = sectionName;
			this.sectionScoreStudent = sectionScoreStudent;
			this.sectionScoreMax = sectionScoreMax;
			this.sectionErrorMessage = sectionErrorMessage;
		}

		public JSONObject getJSONObject() {
			JSONObject obj = new JSONObject();
			obj.put("sectionName", sectionName);
			obj.put("sectionScoreMax", sectionScoreMax);
			obj.put("sectionScoreStudent", sectionScoreStudent);
			obj.put("sectionErrorMessage", sectionErrorMessage);
			return obj;
		}
	}
}
