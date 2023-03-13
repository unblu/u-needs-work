package com.unblu.uneedswork.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UNeedsWorkResult {

	@JsonProperty("build_commit")
	private String buildCommit;

	@JsonProperty("build_timestamp")
	private String buildTimestamp;

	@JsonProperty("gitlab_event_uuid")
	private String gitlabEventUUID;

	@JsonProperty("needs_work_note")
	private UNeedsWorkNote needsWorkNote;

	@JsonProperty("needs_work_note_type")
	private NoteType needsWorkNoteType;

	@JsonProperty("needs_work_note_error")
	private String needsWorkNoteError;

	public enum NoteType {
		ADDED,
		UPDATED
	}

	public String getBuildCommit() {
		return buildCommit;
	}

	public void setBuildCommit(String buildCommit) {
		this.buildCommit = buildCommit;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public void setBuildTimestamp(String buildTimestamp) {
		this.buildTimestamp = buildTimestamp;
	}

	public String getGitlabEventUUID() {
		return gitlabEventUUID;
	}

	public void setGitlabEventUUID(String gitlabEventUUID) {
		this.gitlabEventUUID = gitlabEventUUID;
	}

	public UNeedsWorkNote getNeedsWorkNote() {
		return needsWorkNote;
	}

	public void setNeedsWorkNote(UNeedsWorkNote needsWorkNote) {
		this.needsWorkNote = needsWorkNote;
	}

	public NoteType getNeedsWorkNoteType() {
		return needsWorkNoteType;
	}

	public void setNeedsWorkNoteType(NoteType needsWorkNoteType) {
		this.needsWorkNoteType = needsWorkNoteType;
	}

	public String getNeedsWorkNoteError() {
		return needsWorkNoteError;
	}

	public void setNeedsWorkNoteError(String needsWorkNoteError) {
		this.needsWorkNoteError = needsWorkNoteError;
	}

	@Override
	public int hashCode() {
		return Objects.hash(buildCommit, buildTimestamp, gitlabEventUUID, needsWorkNote, needsWorkNoteError, needsWorkNoteType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UNeedsWorkResult other = (UNeedsWorkResult) obj;
		return Objects.equals(buildCommit, other.buildCommit) && Objects.equals(buildTimestamp, other.buildTimestamp) && Objects.equals(gitlabEventUUID, other.gitlabEventUUID) && Objects.equals(needsWorkNote, other.needsWorkNote) && Objects.equals(needsWorkNoteError, other.needsWorkNoteError) && needsWorkNoteType == other.needsWorkNoteType;
	}

	@Override
	public String toString() {
		return "UNeedsWorkResult [buildCommit=" + buildCommit + ", buildTimestamp=" + buildTimestamp + ", gitlabEventUUID=" + gitlabEventUUID + ", needsWorkNote=" + needsWorkNote + ", needsWorkNoteType=" + needsWorkNoteType + ", needsWorkNoteError=" + needsWorkNoteError + "]";
	}
}
