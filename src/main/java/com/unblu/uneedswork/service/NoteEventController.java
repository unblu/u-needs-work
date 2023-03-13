package com.unblu.uneedswork.service;

import java.util.Random;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gitlab4j.api.webhook.NoteEvent;
import org.jboss.resteasy.reactive.RestHeader;

import com.unblu.uneedswork.model.NoteEventSimple;
import com.unblu.uneedswork.model.UNeedsWorkResult;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import io.vertx.mutiny.core.eventbus.EventBus;

@Path("/u-needs-work")
public class NoteEventController {

	@Inject
	GitLabService gitLabService;

	@Inject
	EventBus eventsBus;

	@POST
	@NonBlocking
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/event")
	public Response handleEvent(@RestHeader("X-Gitlab-Event-UUID") String gitlabEventUUID, NoteEvent noteEvent) {
		Log.infof("GitlabEvent: '%s' | Received", gitlabEventUUID);
		NoteEventSimple simpleEvent = JsonUtils.toSimpleEvent(noteEvent, gitlabEventUUID);
		// consumed by GitLabService class
		eventsBus.send(GitLabService.NOTE_EVENT, simpleEvent);
		UNeedsWorkResult result = gitLabService.createResult(gitlabEventUUID);
		return Response.accepted().entity(result).build();
	}

	@POST
	@Blocking
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/event-blocking")
	public UNeedsWorkResult handleEventBlocking(@RestHeader("X-Gitlab-Event-UUID") String gitlabEventUUID, NoteEvent mrEvent) {
		Log.infof("GitlabEvent: '%s' | Received (blocking)", gitlabEventUUID);
		NoteEventSimple simpleEvent = JsonUtils.toSimpleEvent(mrEvent, gitlabEventUUID);
		return gitLabService.handleEvent(simpleEvent);
	}

	@POST
	@Blocking
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/replay")
	public UNeedsWorkResult replay(NoteEventSimple mrSimple) {
		String gitlabEventUUID = mrSimple.getGitlabEventUUID();
		if (gitlabEventUUID == null) {
			gitlabEventUUID = "replay-" + new Random().nextInt(1000, 10000);
		}
		Log.infof("GitlabEvent: '%s' | Replay", gitlabEventUUID);
		return gitLabService.handleEvent(mrSimple);
	}
}
