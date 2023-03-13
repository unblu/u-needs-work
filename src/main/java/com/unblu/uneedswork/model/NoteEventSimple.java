package com.unblu.uneedswork.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoteEventSimple {

	@JsonProperty("project_id")
	private Long projectId;
	@JsonProperty("note_id")
	private Long noteId;
	@JsonProperty("note_web_url")
	private String noteWebUrl;
	@JsonProperty("note_author_id")
	private Long noteAuthorId;
	@JsonProperty("note_type")
	private String noteType;
	@JsonProperty("note_content")
	private String noteContent;
	@JsonProperty("mr_iid")
	private Long mrIid;
	@JsonProperty("mr_last_commit_sha")
	private String mrLastCommitSha;
	@JsonProperty("mr_web_url")
	private String mrWebUrl;
	@JsonProperty("user_name")
	private String userName;
	@JsonProperty("gitlab_event_uuid")
	private String gitlabEventUUID;

	public NoteEventSimple() {
		super();
	}

	public NoteEventSimple(Long projectId, Long noteId, String noteWebUrl, Long noteAuthorId, String noteType, String noteContent, Long mrIid, String mrLastCommitSha, String mrWebUrl, String userName, String userWebUrl, String gitlabEventUUID) {
		super();
		this.projectId = projectId;
		this.noteId = noteId;
		this.noteWebUrl = noteWebUrl;
		this.noteAuthorId = noteAuthorId;
		this.noteType = noteType;
		this.noteContent = noteContent;
		this.mrIid = mrIid;
		this.mrLastCommitSha = mrLastCommitSha;
		this.mrWebUrl = mrWebUrl;
		this.userName = userName;
		this.gitlabEventUUID = gitlabEventUUID;
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

	public String getNoteWebUrl() {
		return noteWebUrl;
	}

	public void setNoteWebUrl(String noteWebUrl) {
		this.noteWebUrl = noteWebUrl;
	}

	public Long getNoteAuthorId() {
		return noteAuthorId;
	}

	public void setNoteAuthorId(Long noteAuthorId) {
		this.noteAuthorId = noteAuthorId;
	}

	public String getNoteType() {
		return noteType;
	}

	public void setNoteType(String noteType) {
		this.noteType = noteType;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGitlabEventUUID() {
		return gitlabEventUUID;
	}

	public void setGitlabEventUUID(String gitlabEventUUID) {
		this.gitlabEventUUID = gitlabEventUUID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(gitlabEventUUID, mrIid, mrLastCommitSha, mrWebUrl, noteAuthorId, noteContent, noteId, noteType, noteWebUrl, projectId, userName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NoteEventSimple other = (NoteEventSimple) obj;
		return Objects.equals(gitlabEventUUID, other.gitlabEventUUID) && Objects.equals(mrIid, other.mrIid) && Objects.equals(mrLastCommitSha, other.mrLastCommitSha) && Objects.equals(mrWebUrl, other.mrWebUrl) && Objects.equals(noteAuthorId, other.noteAuthorId) && Objects.equals(noteContent, other.noteContent) && Objects.equals(noteId, other.noteId) && Objects.equals(noteType, other.noteType) && Objects.equals(noteWebUrl, other.noteWebUrl) && Objects.equals(projectId, other.projectId) && Objects.equals(userName, other.userName);
	}

	@Override
	public String toString() {
		return "NoteEventSimple [projectId=" + projectId + ", noteId=" + noteId + ", noteWebUrl=" + noteWebUrl + ", noteAuthorId=" + noteAuthorId + ", noteType=" + noteType + ", noteContent=" + noteContent + ", mrIid=" + mrIid + ", mrLastCommitSha=" + mrLastCommitSha + ", mrWebUrl=" + mrWebUrl + ", userName=" + userName + ", gitlabEventUUID=" + gitlabEventUUID + "]";
	}

}
