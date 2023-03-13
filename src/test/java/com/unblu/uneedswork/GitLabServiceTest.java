package com.unblu.uneedswork;

import org.assertj.core.api.Assertions;
import org.gitlab4j.api.models.User;
import org.junit.jupiter.api.Test;

import com.unblu.uneedswork.model.NoteEventSimple;
import com.unblu.uneedswork.service.GitLabService;
import com.unblu.uneedswork.util.GitlabMockUtil;

class GitLabServiceTest {

	@Test
	void testIsNoteRelevant() throws Exception {
		Assertions.assertThat(GitLabService.isNoteRelevant("foo")).isFalse();
		Assertions.assertThat(GitLabService.isNoteRelevant(":wrench: please update")).isTrue();
		Assertions.assertThat(GitLabService.isNoteRelevant("This needs work!")).isTrue();
	}

	@Test
	void testComputeUserBody() throws Exception {
		User user = new User();
		user.setName("John Smith");
		user.setWebUrl("https://gitlab.example.com/jsmith");
		Assertions.assertThat(GitLabService.computeUserBody(user)).isEqualTo("[John Smith](https://gitlab.example.com/jsmith)");
	}

	@Test
	void testComputeNewChangesBody() throws Exception {
		NoteEventSimple event = GitlabMockUtil.createDefaultNoteEventSimple();
		Assertions.assertThat(GitLabService.computeNewChangesBody(event)).isEqualTo("\n\n:eye_in_speech_bubble: [New changes](https://gitlab.example.com/a_project/-/merge_requests/34/diffs?start_sha=e54b4d028af6509e3b97467b153e16753c35747d) since the comment.");
	}

	@Test
	void testComputeCreateBody() throws Exception {
		NoteEventSimple event = GitlabMockUtil.createDefaultNoteEventSimple();
		Assertions.assertThat(GitLabService.computeCreateBody(event, "__userBody__", "__linkBody__")).isEqualTo("__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_42333) changes on this MR.__linkBody__");
	}

	@Test
	void testComputeUpdateFirst() throws Exception {
		NoteEventSimple event = GitlabMockUtil.createDefaultNoteEventSimple();
		String currentBody = "__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR.__linkBody__";
		Assertions.assertThat(GitLabService.computeUpdateBody(event, "__userBody__", "__linkBody__", currentBody)).isEqualTo("__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR (and [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_42333)).__linkBody__");
	}

	@Test
	void testComputeUpdateSecond() throws Exception {
		NoteEventSimple event = GitlabMockUtil.createDefaultNoteEventSimple();
		String currentBody = "__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR (and [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_2345)).__linkBody__";
		Assertions.assertThat(GitLabService.computeUpdateBody(event, "__userBody__", "__linkBody__", currentBody)).isEqualTo("__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR (and [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_2345), [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_42333)).__linkBody__");
	}

	@Test
	void testComputeUpdateThird() throws Exception {
		NoteEventSimple event = GitlabMockUtil.createDefaultNoteEventSimple();
		String currentBody = "__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR (and [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_2345), [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_3456)).__linkBody__";
		Assertions.assertThat(GitLabService.computeUpdateBody(event, "__userBody__", "__linkBody__", currentBody)).isEqualTo("__userBody__ [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR (and [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_2345), [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_3456), [here](https://gitlab.example.com/a_project/-/merge_requests/34#note_42333)).__linkBody__");
	}
}
