package com.unblu.uneedswork.service;

import java.util.Random;

import javax.inject.Inject;

import org.gitlab4j.api.webhook.NoteEvent;

import com.unblu.uneedswork.model.NoteEventSimple;
import com.unblu.uneedswork.model.UNeedsWorkResult;

import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Header;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HandlerType;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.eventbus.EventBus;

@RouteBase(produces = "application/json")
public class NoteEventController {

	@Inject
	GitLabService gitLabService;

	@Inject
	EventBus eventsBus;

	@Route(path = "/u-needs-work/event", order = 1, methods = HttpMethod.POST, type = HandlerType.NORMAL)
	public UNeedsWorkResult handleEvent(@Header("X-Gitlab-Event-UUID") String gitlabEventUUID, @Body NoteEvent noteEvent, HttpServerResponse response) {
		Log.infof("GitlabEvent: '%s' | Received", gitlabEventUUID);
		NoteEventSimple simpleEvent = JsonUtils.toSimpleEvent(noteEvent, gitlabEventUUID);
		// consumed by GitLabService class
		eventsBus.send(GitLabService.NOTE_EVENT, simpleEvent);
		response.setStatusCode(202);
		return gitLabService.createResult(gitlabEventUUID);
	}

	@Route(path = "/u-needs-work/event-blocking", order = 1, methods = HttpMethod.POST, type = HandlerType.BLOCKING)
	public UNeedsWorkResult handleEventBlocking(@Header("X-Gitlab-Event-UUID") String gitlabEventUUID, @Body NoteEvent mrEvent) {
		Log.infof("GitlabEvent: '%s' | Received (blocking)", gitlabEventUUID);
		NoteEventSimple simpleEvent = JsonUtils.toSimpleEvent(mrEvent, gitlabEventUUID);
		return gitLabService.handleEvent(simpleEvent);
	}

	@Route(path = "/u-needs-work/replay", order = 1, methods = HttpMethod.POST, type = HandlerType.BLOCKING)
	public UNeedsWorkResult replay(@Body NoteEventSimple mrSimple) {
		String gitlabEventUUID = mrSimple.getGitlabEventUUID();
		if (gitlabEventUUID == null) {
			gitlabEventUUID = "replay-" + new Random().nextInt(1000, 10000);
		}
		Log.infof("GitlabEvent: '%s' | Replay", gitlabEventUUID);
		return gitLabService.handleEvent(mrSimple);
	}

	@Route(path = "/*", order = 2)
	public void other(@Header("X-Gitlab-Event-UUID") String gitlabEventUUID, RoutingContext rc) {
		String path = rc.request().path();
		if (path.equals("/q/health/live") || path.equals("/q/health/ready")) {
			// the module 'quarkus-smallrye-health' will answer:
			rc.next();
		} else {
			Log.infof("GitlabEvent: '%s' | Invalid path '%s' ", gitlabEventUUID, path);

			UNeedsWorkResult result = gitLabService.createResult(gitlabEventUUID);
			result.setNeedsWorkNoteError("Invalid path: " + path);
			String body = Json.encode(result);
			rc.response()
					.setStatusCode(202)
					.end(body);
		}
	}

	@Route(path = "/*", order = 3, type = HandlerType.FAILURE)
	public UNeedsWorkResult error(@Header("X-Gitlab-Event-UUID") String gitlabEventUUID, RoutingContext rc) {
		Throwable t = rc.failure();
		Log.warnf(t, "GitlabEvent: '%s' | Failed to handle request on path '%s' ", gitlabEventUUID, rc.request().path());

		UNeedsWorkResult result = gitLabService.createResult(gitlabEventUUID);
		if (t != null) {
			result.setNeedsWorkNoteError(t.getMessage());
		} else {
			result.setNeedsWorkNoteError("Unknown error");
		}
		rc.response().setStatusCode(202);
		return result;
	}
}
