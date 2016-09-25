package syno.api;

import com.squareup.moshi.Json;

import java.util.Map;

import annotation.Api;
import annotation.Method;
import annotation.Param;
import annotation.ParamMap;
import annotation.Version;
import syno.WebApi;

@Api("SYNO.API.Auth")
// @Api(value = "SYNO.API.Info", version = 1)
public class Auth implements WebApi {
	public static final String LOGIN = "login";
	public static final String LOGOUT = "logout";

	@Method public String method;

	@Version public int version;

	@Param public String session;
	@Json(name = "client_time") @Param public long clientTime;

	@ParamMap public Map<String, String> params;
}
