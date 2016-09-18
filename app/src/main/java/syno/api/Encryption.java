package syno.api;

import annotation.Api;
import annotation.Method;
import annotation.Version;
import syno.WebApi;

@Api("SYNO.API.Encryption")
public class Encryption implements WebApi {

	public static final String GET_INFO = "getinfo";

	@Method
	public String method;

	@Version
	public int version;
}
