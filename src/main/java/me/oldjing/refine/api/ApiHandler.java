package me.oldjing.refine.api;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public abstract class ApiHandler {
	private static ApiHandler apiHandler;

	public synchronized static ApiHandler getDefault() {
		return apiHandler;
	}

	public synchronized static void setDefault(ApiHandler handler) {
		apiHandler = handler;
	}

	public abstract Map<String, ApiCookie> get(URI uri) throws IOException;

	public abstract void put(URI uri, Map<String, ApiCookie> cookies);

	public abstract void put(URI uri, String name, ApiCookie cookie);
}
