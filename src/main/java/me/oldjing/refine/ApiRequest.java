package me.oldjing.refine;

import java.lang.reflect.Type;

public class ApiRequest {
	private final String api;
	private final String method;
	private final int version;
	private final transient Type resultT;

	public ApiRequest(String api, String method, int version, Type resultT) {
		this.api = api;
		this.method = method;
		this.version = version;
		this.resultT = resultT;
	}
}
