package com.unblu.uneedswork.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Author;
import org.gitlab4j.api.models.Note;
import org.gitlab4j.api.models.User;

import com.unblu.uneedswork.model.NoteEventSimple;
import com.unblu.uneedswork.model.UNeedsWorkNote;
import com.unblu.uneedswork.model.UNeedsWorkResult;

import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
public class GitLabService {

	public static final String NOTE_EVENT = "note-event";

	@Inject
	EventBus resultsBus;

	@ConfigProperty(name = "gitlab.host", defaultValue = "https://gitlab.com")
	String gitLabHost;

	@ConfigProperty(name = "gitlab.api.token")
	String apiToken;

	@ConfigProperty(name = "build.commit", defaultValue = "n/a")
	String buildCommit;

	@ConfigProperty(name = "build.timestamp", defaultValue = "n/a")
	String buildTimestamp;

	private GitLabApi gitlab;
	private Long uneedsworkUser;

	@PostConstruct
	void init() throws GitLabApiException {
		gitlab = new GitLabApi(gitLabHost, apiToken);
		uneedsworkUser = gitlab.getUserApi().getCurrentUser().getId();
	}

	@Blocking // Will be called on a worker thread
	@ConsumeEvent(NOTE_EVENT)
	public UNeedsWorkResult handleEvent(NoteEventSimple event) {
		String gitlabEventUUID = event.getGitlabEventUUID();

		UNeedsWorkResult result = createResult(gitlabEventUUID);

		Long mrIid = event.getMrIid();
		Long projectId = event.getProjectId();
		Long noteId = event.getNoteId();
		Long noteAuthorId = event.getNoteAuthorId();
		String userName = event.getUserName();

		if (mrIid != null) {
			if (isNoteRelevant(event)) {
				try {
					postNeedsWorkComment(event, result);
				} catch (Exception e) {
					Log.warnf(e, "GitlabEvent: '%s' | Exception while posting the needs work comment. Project: '%d', NoteId: '%d', NoteAuthorId: '%d', UserName: '%s', MrIid: '%d'",
							gitlabEventUUID, projectId, noteId, noteAuthorId, userName, mrIid);
					result.setNeedsWorkNoteError(e.getMessage());
				}

			} else {
				Log.infof("GitlabEvent: '%s' | Skip event (not relevant) for Project: '%d', NoteId: '%d', NoteAuthorId: '%d', UserName: '%s', MrIid: '%d', NoteType: '%s'",
						gitlabEventUUID, projectId, noteId, noteAuthorId, userName, mrIid, event.getNoteType());
			}
		} else {
			Log.infof("GitlabEvent: '%s' | Skip event (no MR) for Project: '%d', NoteId: '%d', NoteAuthorId: '%d', UserName: '%s', MrIid: '%d'",
					gitlabEventUUID, projectId, noteId, noteAuthorId, userName, mrIid);
		}

		Log.infof("GitlabEvent: '%s' | Finished handling event with result %s",
				gitlabEventUUID, result);
		return result;
	}

	private void postNeedsWorkComment(NoteEventSimple event, UNeedsWorkResult result) throws GitLabApiException {
		String gitlabEventUUID = event.getGitlabEventUUID();
		Long projectId = event.getProjectId();
		Long mrIid = event.getMrIid();
		Long noteId = event.getNoteId();
		Long noteAuthorId = event.getNoteAuthorId();
		String userName = event.getUserName();

		//The note-event webhook does not contains all the user attributes
		User user = gitlab.getUserApi().getUser(event.getNoteAuthorId());
		String userBody = computeUserBody(user);
		String newChangesBody = computeNewChangesBody(event);

		List<Note> existingNotes = gitlab.getNotesApi().getMergeRequestNotes(projectId, mrIid);
		Optional<Note> findExisting = existingNotes.stream()
				.filter(n -> Objects.equals(uneedsworkUser, n.getAuthor().getId()))
				.filter(n -> n.getBody().startsWith(userBody) && n.getBody().endsWith(newChangesBody))
				.findAny();

		if (findExisting.isPresent()) {
			Note note = findExisting.get();
			Log.infof("GitlabEvent: '%s' | Updating existing comment for Project: '%d', NoteId: '%d', NoteAuthorId: '%d', UserName: '%s',  MrIid: '%d', Existing note id: '%d'",
					gitlabEventUUID, projectId, noteId, noteAuthorId, userName, mrIid, note.getId());
			String updatedBody = computeUpdateBody(event, userBody, newChangesBody, note.getBody());
			Note updatedNote = gitlab.getNotesApi().updateMergeRequestNote(projectId, mrIid, note.getId(), updatedBody);
			result.setNeedsWorkNote(fromNote(updatedNote, event));
			result.setNeedsWorkNoteType(UNeedsWorkResult.NoteType.UPDATED);
		} else {
			Log.infof("GitlabEvent: '%s' | Posting a new comment for Project: '%d', NoteId: '%d', NoteAuthorId: '%d', UserName: '%s', MrIid: '%d'",
					gitlabEventUUID, projectId, noteId, noteAuthorId, userName, mrIid);

			String body = computeCreateBody(event, userBody, newChangesBody);
			Note note = gitlab.getNotesApi()
					.createMergeRequestNote(projectId, mrIid, body);
			Log.infof("GitlabEvent: '%s' | Posted new comment for Project: '%d', NoteId: '%d', NoteAuthorId: '%d', UserName: '%s', MrIid: '%d', New note id: '%d'",
					gitlabEventUUID, projectId, noteId, noteAuthorId, userName, mrIid, note.getId());
			result.setNeedsWorkNote(fromNote(note, event));
			result.setNeedsWorkNoteType(UNeedsWorkResult.NoteType.ADDED);
		}
	}

	private UNeedsWorkNote fromNote(Note note, NoteEventSimple event) {
		UNeedsWorkNote result = new UNeedsWorkNote();
		result.setProjectId(event.getProjectId());
		result.setMrIid(event.getMrIid());
		result.setMrLastCommitSha(event.getMrLastCommitSha());
		result.setMrWebUrl(event.getMrWebUrl());
		result.setNoteId(note.getId());
		result.setNoteContent(note.getBody());
		Author author = note.getAuthor();
		if (author != null) {
			result.setNoteAuthorId(author.getId());
		}
		return result;
	}

	public static String computeUserBody(User user) {
		return "[" + user.getName() + "](" + user.getWebUrl() + ")";
	}

	public static String computeNewChangesBody(NoteEventSimple event) {
		return "\n\n:eye_in_speech_bubble: [New changes](" + event.getMrWebUrl() + "/diffs?start_sha=" + event.getMrLastCommitSha() + ") since the comment.";
	}

	public static String computeCreateBody(NoteEventSimple event, String userBody, String newChangesBody) {
		String body = userBody + " " + "[requested](" + event.getNoteWebUrl() + ") changes on this MR." + newChangesBody;
		return body;
	}

	public static String computeUpdateBody(NoteEventSimple event, String userBody, String newChangesBody, String currentBody) {
		String bodyFragment = currentBody.substring(userBody.length(), currentBody.length() - newChangesBody.length() - 1);
		String updatedBody;
		if (bodyFragment.endsWith(")")) {
			updatedBody = userBody + bodyFragment.substring(0, bodyFragment.length() - 1) + ", [here](" + event.getNoteWebUrl() + "))." + newChangesBody;
		} else {
			//first addition:
			updatedBody = userBody + bodyFragment + " (and [here](" + event.getNoteWebUrl() + "))." + newChangesBody;
		}
		return updatedBody;
	}

	public static boolean isNoteRelevant(NoteEventSimple event) {
		if (Objects.equals(event.getNoteType(), "DiffNote")) {
			return false;
		}
		return isNoteContentRelevant(event.getNoteContent());
	}

	public static boolean isNoteContentRelevant(String noteContent) {
		String lowerCase = noteContent.toLowerCase();
		return lowerCase.contains(":wrench:") || lowerCase.contains("needs work");
	}

	public UNeedsWorkResult createResult(String gitlabEventUUID) {
		UNeedsWorkResult result = new UNeedsWorkResult();
		result.setGitlabEventUUID(gitlabEventUUID);
		result.setBuildCommit(buildCommit);
		result.setBuildTimestamp(buildTimestamp);
		return result;
	}
}
