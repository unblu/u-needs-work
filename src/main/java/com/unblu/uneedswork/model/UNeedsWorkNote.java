package com.unblu.uneedswork.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UNeedsWorkNote {

	@JsonProperty("project_id")
	private Long projectId;
	@JsonProperty("note_id")
	private Long noteId;
	@JsonProperty("note_author_id")
	private Long noteAuthorId;
	@JsonProperty("note_content")
	private String noteContent;
	@JsonProperty("mr_iid")
	private Long mrIid;
	@JsonProperty("mr_last_commit_sha")
	private String mrLastCommitSha;
	@JsonProperty("mr_web_url")
	private String mrWebUrl;

	public UNeedsWorkNote() {
		super();
	}

	public UNeedsWorkNote(Long projectId, Long noteId, String noteWebUrl, Long noteAuthorId, String noteType, String noteContent, Long mrIid, String mrLastCommitSha, String mrWebUrl, String userName, String userWebUrl, String gitlabEventUUID) {
		super();
		this.projectId = projectId;
		this.noteId = noteId;
		this.noteAuthorId = noteAuthorId;
		this.noteContent = noteContent;
		this.mrIid = mrIid;
		this.mrLastCommitSha = mrLastCommitSha;
		this.mrWebUrl = mrWebUrl;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getNoteId() {
		return noteId;
	}

	public void setNoteId(Long noteId) {
		this.noteId = noteId;
	}

	public Long getNoteAuthorId() {
		return noteAuthorId;
	}

	public void setNoteAuthorId(Long noteAuthorId) {
		this.noteAuthorId = noteAuthorId;
	}

	public String getNoteContent() {
		return noteContent;
	}

	public void setNoteContent(String noteContent) {
		this.noteContent = noteContent;
	}

	public Long getMrIid() {
		return mrIid;
	}

	public void setMrIid(Long mrIid) {
		this.mrIid = mrIid;
	}

	public String getMrLastCommitSha() {
		return mrLastCommitSha;
	}

	public void setMrLastCommitSha(String mrLastCommitSha) {
		this.mrLastCommitSha = mrLastCommitSha;
	}

	public String getMrWebUrl() {
		return mrWebUrl;
	}

	public void setMrWebUrl(String mrWebUrl) {
		this.mrWebUrl = mrWebUrl;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mrIid, mrLastCommitSha, mrWebUrl, noteAuthorId, noteContent, noteId, projectId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UNeedsWorkNote other = (UNeedsWorkNote) obj;
		return Objects.equals(mrIid, other.mrIid) && Objects.equals(mrLastCommitSha, other.mrLastCommitSha) && Objects.equals(mrWebUrl, other.mrWebUrl) && Objects.equals(noteAuthorId, other.noteAuthorId) && Objects.equals(noteContent, other.noteContent) && Objects.equals(noteId, other.noteId) && Objects.equals(projectId, other.projectId);
	}

	@Override
	public String toString() {
		return "UNeedsWorkNote [projectId=" + projectId + ", noteId=" + noteId + ", noteAuthorId=" + noteAuthorId + ", noteContent=" + noteContent + ", mrIid=" + mrIid + ", mrLastCommitSha=" + mrLastCommitSha + ", mrWebUrl=" + mrWebUrl + "]";
	}

}
