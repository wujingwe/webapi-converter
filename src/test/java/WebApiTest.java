import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.oldjing.refine.*;
import me.oldjing.refine.cookie.ApiCookie;
import me.oldjing.refine.cookie.ApiHandler;
import me.oldjing.refine.cookie.ApiManager;
import me.oldjing.refine.vos.ApiMapVo;
import me.oldjing.refine.vos.ApiMapVo.ApiVo;
import me.oldjing.refine.vos.AuthVo;
import me.oldjing.refine.vos.AuthVo.SidVo;
import me.oldjing.refine.vos.BasicVo;
import me.oldjing.refine.vos.CompoundVo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.oldjing.refine.cookie.BasicError.WEBAPI_ERR_NOT_SUPPORTED_VERSION;
import static me.oldjing.refine.cookie.BasicError.WEBAPI_ERR_NO_SUCH_METHOD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class WebApiTest {

	interface ApiService {
		@WebAPI(api="SYNO.API.Info", method="query", version=1)
		ApiMapVo query();

		@WebAPI(api="SYNO.API.Info", method="unsupported", version=1)
		ApiMapVo unsupportedMethod();

		@WebAPI(api="SYNO.API.Info", method="query")
		ApiMapVo runtimeVersion(@Version() int version);

		@WebAPI(api="SYNO.Entry.Request", method="request", version=1)
		CompoundVo compound(@Compound List<ApiRequest> apiRequests);
	}

	@Rule
	public final MockWebServer server = new MockWebServer();

	private Gson gson = new Gson();

	@Test
	public void query() throws Exception {
		Map<String, ApiVo> map = new HashMap<>();
		map.put("SYNO.API.Info", new ApiVo(1, 1, "query.cgi", null));
		ApiMapVo apiMapVo = new ApiMapVo(map);
		server.enqueue(new MockResponse().setBody(gson.toJson(apiMapVo)));

		Refine refine = new Refine.Builder()
				.baseUrl(server.url("/"))
				.build();

		ApiService service = refine.create(ApiService.class);
		ApiMapVo mapVo = service.query();

		assertNotNull(mapVo);
		assertNotNull(mapVo.data);
		assertTrue(mapVo.success);
		assertEquals(mapVo.data, map);
	}

	@Test
	public void unsupportedMethod() throws Exception {
		BasicVo result = new BasicVo(false, new BasicVo.ErrorCodeVo(WEBAPI_ERR_NO_SUCH_METHOD));
		server.enqueue(new MockResponse().setBody(gson.toJson(result)));

		Refine refine = new Refine.Builder()
				.baseUrl(server.url("/"))
				.build();

		ApiService service = refine.create(ApiService.class);
		BasicVo basicVo = service.unsupportedMethod();

		assertNotNull(basicVo);
		assertNotNull(basicVo.error);
		assertFalse(basicVo.success);
		assertEquals(basicVo.error.code, WEBAPI_ERR_NO_SUCH_METHOD); // no such method
	}

	@Test
	public void runtimeVersion() throws Exception {
		Map<String, ApiVo> map = new HashMap<>();
		map.put("SYNO.API.Info", new ApiVo(1, 1, "query.cgi", null));
		ApiMapVo apiMapVo = new ApiMapVo(map);
		server.enqueue(new MockResponse().setBody(gson.toJson(apiMapVo)));

		Refine refine = new Refine.Builder()
				.baseUrl(server.url("/"))
				.build();

		ApiService service = refine.create(ApiService.class);
		ApiMapVo mapVo = service.runtimeVersion(1);

		assertNotNull(mapVo);
		assertNotNull(mapVo.data);
		assertTrue(mapVo.success);
		assertEquals(mapVo.data, map);

		mapVo = service.runtimeVersion(3);
		assertNotNull(mapVo);
		assertNotNull(mapVo.error);
		assertFalse(mapVo.success);
		assertEquals(mapVo.error.code, WEBAPI_ERR_NOT_SUPPORTED_VERSION); // no such version
	}

	@Test
	public void compound() throws Exception {
		Refine refine = new Refine.Builder()
				                .baseUrl(server.url("/"))
				                .build();

		ApiManager apiManager = (ApiManager) ApiHandler.getDefault();
		apiManager.put(server.url("/").uri(), "SYNO.Entry.Request", new ApiCookie(1, 1, "entry.cgi", true));
		ApiHandler.setDefault(apiManager);

		JsonParser parser = new JsonParser();
		List<JsonObject> result = new ArrayList<>();
		result.add(parser.parse(gson.toJson(new BasicVo(true, null))).getAsJsonObject());
		result.add(parser.parse(gson.toJson(new AuthVo(new SidVo("_sid", false, "_did")))).getAsJsonObject());
		CompoundVo expected = new CompoundVo(true, result);
		server.enqueue(new MockResponse().setBody(gson.toJson(expected)));

		List<ApiRequest> requests = new ArrayList<>();
		requests.add(new ApiRequest("SYNO.API.Info", "query", 1, BasicVo.class));
		requests.add(new ApiRequest("SYNO.API.Info", "query", 2, AuthVo.class));
		ApiService service = refine.create(ApiService.class);
		CompoundVo compoundVo = service.compound(requests);

		assertNotNull(compoundVo);
		assertNotNull(compoundVo.result);

		BasicVo basicVo = gson.fromJson(compoundVo.result.get(0), BasicVo.class);
		assertNotNull(basicVo);
		assertTrue(basicVo.success);
		AuthVo authVo = gson.fromJson(compoundVo.result.get(1), AuthVo.class);
		assertNotNull(authVo);
		assertTrue(authVo.success);
		assertNotNull(authVo.data);
		assertThat(authVo.data.sid, is("_sid"));
	}
}
