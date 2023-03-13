package com.unblu.uneedswork.service;

import org.gitlab4j.api.models.User;
import org.gitlab4j.api.webhook.EventMergeRequest;
import org.gitlab4j.api.webhook.EventProject;
import org.gitlab4j.api.webhook.NoteEvent;
import org.gitlab4j.api.webhook.NoteEvent.ObjectAttributes;

import com.unblu.uneedswork.model.NoteEventSimple;

public class JsonUtils {

	public static NoteEventSimple toSimpleEvent(NoteEvent event, String gitlabEventUUID) {
		NoteEventSimple result = new NoteEventSimple();

		ObjectAttributes objectAttributes = event.getObjectAttributes();
		if (objectAttributes == null) {
			throw new IllegalStateException(String.format("GitlabEvent: '%s' | NoteEvent.ObjectAttributes is null. Possible cause: error in the deserializing process", gitlabEventUUID));
		}

		result.setGitlabEventUUID(gitlabEventUUID);
		result.setNoteId(objectAttributes.getId());
		result.setNoteAuthorId(objectAttributes.getAuthorId());
		result.setNoteType(objectAttributes.getType());
		result.setNoteWebUrl(objectAttributes.getUrl());
		result.setNoteContent(objectAttributes.getNote());
		EventProject project = event.getProject();
		if (project != null) {
			result.setProjectId(project.getId());
		}
		EventMergeRequest mr = event.getMergeRequest();
		if (mr != null) {
			result.setMrIid(mr.getIid());
			result.setMrWebUrl(mr.getUrl());
			result.setMrLastCommitSha(mr.getLastCommit().getId());
		}
		User user = event.getUser();
		if (user != null) {
			result.setUserName(user.getName());
		}
		return result;
	}

	private JsonUtils() {
	}
}
