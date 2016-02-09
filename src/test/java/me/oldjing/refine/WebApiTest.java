package me.oldjing.refine;

import com.google.gson.Gson;
import me.oldjing.refine.vos.ApiMapVo;
import me.oldjing.refine.vos.ApiMapVo.ApiVo;
import me.oldjing.refine.vos.BasicVo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static me.oldjing.refine.api.BasicError.WEBAPI_ERR_NOT_SUPPORTED_VERSION;
import static me.oldjing.refine.api.BasicError.WEBAPI_ERR_NO_SUCH_METHOD;
import static org.junit.Assert.*;

public class WebApiTest {

	interface ApiService {
		@WebAPI(api="SYNO.API.Info", method="query", version=1)
		ApiMapVo query();

		@WebAPI(api="SYNO.API.Info", method="unsupported", version=1)
		ApiMapVo unsupportedMethod();

		@WebAPI(api="SYNO.API.Info", method="query")
		ApiMapVo runtimeVersion(@Version() int version);
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
}
