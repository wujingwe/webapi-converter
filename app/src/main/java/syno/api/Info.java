package syno.api;

import annotation.Api;
import annotation.Method;
import annotation.Param;
import annotation.Version;
import syno.WebApi;

@Api("SYNO.API.Info")
// @Api(value = "SYNO.API.Info", version = 1)
public class Info implements WebApi {
	public static final String QUERY = "query";

	@Method public String method;

	@Version public int version;

	@Param public String query;
}
