package syno.core.upgrade.server;

import annotation.Api;
import annotation.Method;
import annotation.Version;
import syno.WebApi;

@Api("SYNO.Core.Upgrade.Server")
public class Server implements WebApi {
	public static final String CHECK = "check";

	@Method public String method;

	@Version public int version;
}
