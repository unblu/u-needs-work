package com.unblu.uneedswork.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gitlab4j.api.utils.JacksonJson;
import org.gitlab4j.api.webhook.NoteEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.unblu.uneedswork.model.NoteEventSimple;
import com.unblu.uneedswork.model.UNeedsWorkNote;
import com.unblu.uneedswork.model.UNeedsWorkResult;
import com.unblu.uneedswork.model.UNeedsWorkResult.NoteType;
import com.unblu.uneedswork.service.JsonUtils;
import com.unblu.uneedswork.util.GitlabMockUtil.GitlabAction;

class JsonUtilsTest {

	@Test
	void testParseReplayInput() throws Exception {
		NoteEventSimple expected = GitlabMockUtil.createDefaultNoteEventSimple();
		checkJsonFile(expected, NoteEventSimple.class, "_documentation/src/docs/documentation/u-needs-work-replay.json");
	}

	@Test
	void testUNeedsWorkNonBlockingResponseFile() throws Exception {
		UNeedsWorkResult expected = createUNeedsWorkResult();
		checkJsonFile(expected, UNeedsWorkResult.class, "_documentation/src/docs/documentation/u-needs-work-non-blocking-response.json");
	}

	@Test
	void testUNeedsWorkBlockingResponseFile() throws Exception {
		NoteEventSimple event = GitlabMockUtil.createDefaultNoteEventSimple();
		UNeedsWorkNote note = new UNeedsWorkNote();
		note.setProjectId(event.getProjectId());
		note.setMrIid(event.getMrIid());
		note.setMrLastCommitSha(event.getMrLastCommitSha());
		note.setMrWebUrl(event.getMrWebUrl());
		note.setNoteId(42395L);
		note.setNoteContent("[John Smith](https://gitlab.example.com/jsmith) [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR.\n\n:eye_in_speech_bubble: [New changes](https://gitlab.example.com/a_project/-/merge_requests/34/diffs?start_sha=e54b4d028af6509e3b97467b153e16753c35747d) since the comment.");
		note.setNoteAuthorId(139L);

		UNeedsWorkResult expected = createUNeedsWorkResult();
		expected.setNeedsWorkNote(note);
		expected.setNeedsWorkNoteType(NoteType.ADDED);

		checkJsonFile(expected, UNeedsWorkResult.class, "_documentation/src/docs/documentation/u-needs-work-blocking-response.json");
	}

	private UNeedsWorkResult createUNeedsWorkResult() {
		UNeedsWorkResult result = new UNeedsWorkResult();
		result.setBuildCommit("6af21ad");
		result.setBuildTimestamp("2022-01-01T07:21:58.378413Z");
		result.setGitlabEventUUID("62940263-b495-4f7e-b0e8-578c7307f13d");
		return result;
	}

	private <T> void checkJsonFile(T expected, Class<T> cls, String filePath) throws IOException, JsonProcessingException, JsonMappingException {
		// Due to https://github.com/docToolchain/docToolchain/issues/898 we need a copy inside the _documentation project because it can't access the java project
		Path file = Path.of(filePath);

		// Read the current content to see if the parser works:
		String content = Files.readString(file);

		// Update the file derived from the Java model, so that we are sure it stays up-to-date in the docs:
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		T actual;
		try {
			actual = mapper.readValue(content, cls);
		} catch (Exception e) {
			actual = null;
		}
		String expectedContent = mapper.writeValueAsString(expected);
		Files.writeString(file, expectedContent);

		Assertions.assertEquals(expected, actual);
		Assertions.assertEquals(expectedContent, content);
	}

	@Test
	void testToSimpleEvent() throws Exception {
		String content = GitlabMockUtil.get(GitlabAction.EVENT_NOTE);
		NoteEvent event = new JacksonJson().getObjectMapper().readValue(content, NoteEvent.class);
		NoteEventSimple result = JsonUtils.toSimpleEvent(event, GitlabMockUtil.GITLAB_EVENT_UUID);

		NoteEventSimple expected = GitlabMockUtil.createDefaultNoteEventSimple();
		Assertions.assertEquals(expected, result);
	}

}
