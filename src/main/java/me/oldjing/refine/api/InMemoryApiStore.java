package me.oldjing.refine.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryApiStore implements ApiStore {

	// In memory
	private Map<String, Map<String, ApiCookie>> allCookies;

	public InMemoryApiStore() {
		allCookies = new HashMap<>();
	}

	@Override
	public void add(URI uri, String name, ApiCookie cookie) {
		String authority = uri.getAuthority();

		Map<String, ApiCookie> targetCookies = allCookies.get(authority);
		if (targetCookies == null) {
			targetCookies = new HashMap<>();
			allCookies.put(authority, targetCookies);
		}
		targetCookies.remove(name);
		targetCookies.put(name, cookie);
	}

	@Override
	public Map<String, ApiCookie> get(URI uri) {
		Map<String, ApiCookie> targetCookies = new HashMap<>();
		for (Map.Entry<String, Map<String, ApiCookie>> entry : allCookies.entrySet()) {
			String authority = entry.getKey();
			if (authority.equalsIgnoreCase(uri.getAuthority())) {
				targetCookies.putAll(allCookies.get(authority));
			}
		}
		return targetCookies;
	}

	@Override
	public List<String> getAuthorities() {
		return new ArrayList<>(allCookies.keySet());
	}

	@Override
	public boolean remove(URI uri, String name) {
		String authority = uri.getAuthority();
		Map<String, ApiCookie> targetCookies = allCookies.get(authority);
		if (targetCookies != null) {
			targetCookies.remove(name);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll() {
		allCookies.clear();
		return true;
	}
}
