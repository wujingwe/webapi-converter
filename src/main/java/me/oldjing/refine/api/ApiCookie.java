package me.oldjing.refine.api;

public class ApiCookie {

	private final int maxVersion;
	private final int minVersion;
	private final String path;
	private final boolean jsonEncode;

	public ApiCookie(int maxVersion, int minVersion, String path, boolean jsonEncode) {
		this.maxVersion = maxVersion;
		this.minVersion = minVersion;
		this.path = path;
		this.jsonEncode = jsonEncode;
	}

	public int getMaxVersion() {
		return maxVersion;
	}

	public int getMinVersion() {
		return minVersion;
	}

	public String getPath() {
		return path;
	}

	public boolean jsonEncode() {
		return jsonEncode;
	}
}
