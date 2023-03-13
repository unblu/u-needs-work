package com.unblu.uneedswork.configuration;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapperConfiguration {

	@ServerExceptionMapper
	public RestResponse<String> mapException(Exception e) {
		return RestResponse.status(Response.Status.EXPECTATION_FAILED, wrapMsg(e.getMessage()));
	}

	private String wrapMsg(String msg) {
		return "{\"message\":\"" + msg + "\"}";
	}
}