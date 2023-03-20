package com.unblu.uneedswork;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.unblu.uneedswork.model.NoteEventSimple;
import com.unblu.uneedswork.util.GitlabMockUtil;
import com.unblu.uneedswork.util.GitlabMockUtil.GitlabAction;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(WireMockGitlabProxy.class)
class UNeedsWorkTest {

	final static String API_PREFIX = "/api/v4/";
	final static String API_AUTH_KEY_NAME = "PRIVATE-TOKEN";

	@InjectWireMock
	WireMockServer wireMockServer;

	@ConfigProperty(name = "gitlab.api.token")
	String apiToken;

	@BeforeEach
	void init() throws IOException {
		wireMockServer.resetAll();
		setupGetCurrentUserStub();
	}

	@Test
	void testEndpointRapidReturn() throws Exception {
		setupDefaultStubs();

		given().when()
				.header("Content-Type", "application/json")
				.header("X-Gitlab-Event-UUID", GitlabMockUtil.GITLAB_EVENT_UUID)
				.body(GitlabMockUtil.get(GitlabAction.EVENT_NOTE))
				.post("/u-needs-work/event")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", equalTo(GitlabMockUtil.GITLAB_EVENT_UUID))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"));

		verifyRequests(3);
	}

	@Test
	void testEndpointBlocking() {
		setupDefaultStubs();

		given().when()
				.header("Content-Type", "application/json")
				.header("X-Gitlab-Event-UUID", "test-8921247")
				.body(GitlabMockUtil.get(GitlabAction.EVENT_NOTE))
				.post("/u-needs-work/event-blocking")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", equalTo("test-8921247"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("needs_work_note", notNullValue())
				.body("needs_work_note_type", equalTo("ADDED"))
				.body("needs_work_note_error", nullValue());
	}

	@Test
	void testSuccessCase() throws Exception {
		setupDefaultStubs();
		NoteEventSimple simpleEvent = GitlabMockUtil.createDefaultNoteEventSimple();

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleEvent)
				.post("/u-needs-work/replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", equalTo(GitlabMockUtil.GITLAB_EVENT_UUID))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("needs_work_note", notNullValue())
				.body("needs_work_note_type", equalTo("ADDED"))
				.body("needs_work_note_error", nullValue());

		verifyRequests(3);
	}

	private void setupDefaultStubs() {
		setupGetUserStub();
		setupGetMrCommentsEmpty();
		setupCreateComment();
	}

	@Test
	void testWithExistingComment() throws Exception {
		setupGetUserStub();
		setupGetMrCommentsExisting();
		setupUpdateComment();

		NoteEventSimple simpleEvent = GitlabMockUtil.createDefaultNoteEventSimple();

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleEvent)
				.post("/u-needs-work/replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", equalTo(GitlabMockUtil.GITLAB_EVENT_UUID))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("needs_work_note", notNullValue())
				.body("needs_work_note_type", equalTo("UPDATED"))
				.body("needs_work_note_error", nullValue());

		verifyRequests(3);
	}

	@Test
	void testDiffNote() throws Exception {
		NoteEventSimple simpleEvent = GitlabMockUtil.createDefaultNoteEventSimple();
		simpleEvent.setNoteType("DiffNote");

		given().when()
				.header("Content-Type", "application/json")
				.body(simpleEvent)
				.post("/u-needs-work/replay")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", equalTo(GitlabMockUtil.GITLAB_EVENT_UUID))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("needs_work_note", nullValue())
				.body("needs_work_note_type", nullValue())
				.body("needs_work_note_error", nullValue());

		verifyRequests(0);
	}

	@Test
	void testEndpointRapidReturnMalformedRequest() throws Exception {
		String json = """
				{
				    "foo" : "bar",
				    "baz" : 43
				}
				""";
		given().when()
				.header("Content-Type", "application/json")
				.body(json)
				.post("/u-needs-work/event")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", nullValue())
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("needs_work_note_error", startsWith("Could not resolve subtype of"));

		verifyRequests(0);
	}

	@Test
	void testInvalidEndpoint() throws Exception {
		String json = """
				{
				    "foo" : "bar",
				    "baz" : 43
				}
				""";
		given().when()
				.header("Content-Type", "application/json")
				.header("X-Gitlab-Event-UUID", "test-1289369")
				.body(json)
				.post("/foo")
				.then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode())
				.body(startsWith("{\n"))
				.body(endsWith("\n}"))
				.body("gitlab_event_uuid", equalTo("test-1289369"))
				.body("build_commit", equalTo("6af21ad"))
				.body("build_timestamp", equalTo("2022-01-01T07:21:58.378413Z"))
				.body("needs_work_note_error", equalTo("Invalid path: /foo"));

		verifyRequests(0);
	}

	private void verifyRequests(int expectedRequestNumber) throws InterruptedException {
		List<ServeEvent> allServeEvents = waitForRequests(expectedRequestNumber);

		//If getUser() was called, expect one more call:
		int expected;
		if (allServeEvents.stream().anyMatch(e -> isGetUserStub(e.getStubMapping()))) {
			expected = expectedRequestNumber + 1;
		} else {
			expected = expectedRequestNumber;
		}
		Assertions.assertEquals(expected, allServeEvents.size(), "Number of requests to GitLab");

		//Verify that all stubs where called at least once. getUser() is defined as stub, but unused if the GitLabService was already initialized for a given test
		List<StubMapping> usedStubs = allServeEvents.stream().map(e -> e.getStubMapping()).toList();
		List<StubMapping> stubMappings = wireMockServer.getStubMappings();
		List<String> unused = stubMappings.stream()
				.filter(s -> !usedStubs.contains(s))
				.filter(s -> !isGetUserStub(s))
				.map(e -> e.getRequest().toString())
				.toList();
		if (!unused.isEmpty()) {
			Assertions.fail("Some defined stubs were not called by the GitLab client:\n" + unused);
		}
	}

	private List<ServeEvent> waitForRequests(int minimalNumberOfRequestsToWaitFor) throws InterruptedException {
		int countDown = 30;
		List<ServeEvent> allServeEvents = wireMockServer.getAllServeEvents();
		while (allServeEvents.size() < minimalNumberOfRequestsToWaitFor && countDown-- > 0) {
			TimeUnit.SECONDS.sleep(1);
			allServeEvents = wireMockServer.getAllServeEvents();
		}
		return allServeEvents;
	}

	private boolean isGetUserStub(StubMapping stub) {
		return Objects.equals("/api/v4/user", stub.getRequest().getUrlPath());
	}

	private void setupCreateComment() {
		wireMockServer.stubFor(
				post(urlPathEqualTo(API_PREFIX + "projects/56/merge_requests/34/notes"))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(apiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(GitlabMockUtil.get(GitlabAction.CREATE_NOTE))));
	}

	private void setupUpdateComment() {
		wireMockServer.stubFor(
				put(urlPathEqualTo(API_PREFIX + "projects/56/merge_requests/34/notes/32146"))
						.withHeader(API_AUTH_KEY_NAME, WireMock.equalTo(apiToken))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(GitlabMockUtil.get(GitlabAction.UPDATE_NOTE))));
	}

	private void setupGetMrCommentsEmpty() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(API_PREFIX + "projects/56/merge_requests/34/notes"))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(GitlabMockUtil.get(GitlabAction.GET_MERGE_REQUEST_NOTES_EMPTY))));
	}

	private void setupGetMrCommentsExisting() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(API_PREFIX + "projects/56/merge_requests/34/notes"))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(GitlabMockUtil.get(GitlabAction.GET_MERGE_REQUEST_NOTES_EXISTING))));
	}

	private void setupGetCurrentUserStub() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(API_PREFIX + "user"))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(GitlabMockUtil.get(GitlabAction.GET_CURRENT_USER))));
	}

	private void setupGetUserStub() {
		wireMockServer.stubFor(
				get(urlPathEqualTo(API_PREFIX + "users/37"))
						.withQueryParam("with_custom_attributes", WireMock.equalTo("false"))
						.willReturn(aResponse()
								.withHeader("Content-Type", "application/json")
								.withBody(GitlabMockUtil.get(GitlabAction.GET_USER))));
	}
}
