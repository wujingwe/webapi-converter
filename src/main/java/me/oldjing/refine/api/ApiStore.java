package me.oldjing.refine.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface ApiStore {

	void add(URI uri, String name, ApiCookie cookie);

	Map<String, ApiCookie> get(URI uri);

	List<String> getAuthorities();

	boolean remove(URI uri, String name);

	boolean removeAll();
}
