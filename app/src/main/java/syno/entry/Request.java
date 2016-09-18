package syno.entry;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import annotation.Api;
import annotation.Compound;
import annotation.Method;
import annotation.Param;
import annotation.Version;
import syno.WebApi;

@Api("SYNO.Entry.Request")
@Compound
public class Request {
	public static final String REQUEST = "request";

	@Method public String method;

	@Version public int version;

	@Param public List<WebApi> compound;
	@SerializedName("stop_when_error") boolean stopWhenError;
}
