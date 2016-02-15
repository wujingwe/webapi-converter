package me.oldjing.refine.cookie;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiManager extends ApiHandler {

	private ApiStore apiJar = null;

	public ApiManager() {
		this(null);
	}

	public ApiManager(ApiStore store) {
		if (store == null) {
			apiJar = new InMemoryApiStore();
		} else {
			apiJar = store;
		}
	}

	@Override
	public Map<String, ApiCookie> get(final URI uri) throws IOException {
		// pre-condition check
		if (uri == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		Map<String, ApiCookie> apiMap = new HashMap<>();
		// if there's no default ApiStore, no way for us to get any API
		if (apiJar == null) {
			return Collections.unmodifiableMap(apiMap);
		}
		apiMap = apiJar.get(uri);
		return Collections.unmodifiableMap(apiMap);
	}

	@Override
	public void put(final URI uri, final Map<String, ApiCookie> cookies) {
		// pre-condition check
		if (uri == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		// if there's no default ApiStore, no need to remember any API
		if (apiJar == null) {
			return;
		}

		for (Map.Entry<String, ApiCookie> entry : cookies.entrySet()) {
			String name = entry.getKey();
			ApiCookie cookie = entry.getValue();
			apiJar.add(uri, name, cookie);
		}
	}

	@Override
	public void put(final URI uri, final String name, final ApiCookie cookie) {
		// pre-condition check
		if (uri == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		// if there's no default ApiStore, no need to remember any API
		if (apiJar == null) {
			return;
		}

		apiJar.add(uri, name, cookie);
	}
}
