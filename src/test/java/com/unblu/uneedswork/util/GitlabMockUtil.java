package com.unblu.uneedswork.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.stream.Collectors;

import com.unblu.uneedswork.model.NoteEventSimple;

public class GitlabMockUtil {

	public static final String GITLAB_EVENT_UUID = "test-8902345";

	/**
	 * Gitlab API actions
	 */
	public static enum GitlabAction {
		GET_CURRENT_USER,
		GET_USER,
		GET_MERGE_REQUEST_NOTES_EMPTY,
		GET_MERGE_REQUEST_NOTES_EXISTING,
		CREATE_NOTE,
		UPDATE_NOTE,
		EVENT_NOTE;
	}

	private static final EnumMap<GitlabAction, String> jsonTemplatesLocation = initJsonTemplatesLocationMap();

	/**
	 * Loads the JSON file content for a given action, possibly customizing some properties.
	 *
	 * @param action The action for which the corresponding JSON response is required.
	 * @return String containing the contents of the JSON file for the given action, with its properties customized accordingly to the provided map.
	 */
	public static String get(GitlabAction action) {
		return readFromResources(jsonTemplatesLocation.get(action));
	}

	private static String readFromResources(String name) {
		try (InputStream is = GitlabMockUtil.class.getResourceAsStream(name)) {
			return new BufferedReader(new InputStreamReader(is))
					.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			throw new RuntimeException("Could not read resource " + name, e);
		}
	}

	private static EnumMap<GitlabAction, String> initJsonTemplatesLocationMap() {
		EnumMap<GitlabAction, String> templates = new EnumMap<>(GitlabAction.class);
		templates.put(GitlabAction.GET_CURRENT_USER, "/gitlab_template_json/api/getCurrentUserResponse.json");
		templates.put(GitlabAction.GET_USER, "/gitlab_template_json/api/getUserResponse.json");
		templates.put(GitlabAction.GET_MERGE_REQUEST_NOTES_EMPTY, "/gitlab_template_json/api/getMergeRequestNotesEmpty.json");
		templates.put(GitlabAction.GET_MERGE_REQUEST_NOTES_EXISTING, "/gitlab_template_json/api/getMergeRequestNotesExisting.json");
		templates.put(GitlabAction.CREATE_NOTE, "/gitlab_template_json/api/createNoteResponse.json");
		templates.put(GitlabAction.UPDATE_NOTE, "/gitlab_template_json/api/updateNoteResponse.json");
		templates.put(GitlabAction.EVENT_NOTE, "/gitlab_template_json/webhook/noteEvent.json");
		return templates;
	}

	public static NoteEventSimple createDefaultNoteEventSimple() {
		NoteEventSimple event = new NoteEventSimple();
		event.setGitlabEventUUID(GITLAB_EVENT_UUID);
		event.setProjectId(56L);
		event.setNoteAuthorId(37L);
		event.setUserName("John Smith");
		event.setNoteContent("This needs work!");
		event.setMrIid(34L);
		event.setMrLastCommitSha("e54b4d028af6509e3b97467b153e16753c35747d");
		event.setMrWebUrl("https://gitlab.example.com/a_project/-/merge_requests/34");
		event.setNoteId(42333L);
		event.setNoteWebUrl("https://gitlab.example.com/a_project/-/merge_requests/34#note_42333");
		return event;
	}
}
