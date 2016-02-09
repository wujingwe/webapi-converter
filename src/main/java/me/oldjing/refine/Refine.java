package me.oldjing.refine;

import com.google.gson.Gson;
import me.oldjing.refine.api.ApiCookie;
import me.oldjing.refine.api.ApiHandler;
import me.oldjing.refine.api.ApiManager;
import me.oldjing.refine.api.BasicError;
import me.oldjing.refine.vos.ApiMapVo;
import me.oldjing.refine.vos.ApiMapVo.ApiVo;
import me.oldjing.refine.vos.BasicVo;
import me.oldjing.refine.vos.BasicVo.ErrorCodeVo;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

public class Refine {

	interface WebApiService {
		@FormUrlEncoded
		@POST("{path}")
		Call<ResponseBody> invokeWebApi(@Path("path") String path,
		                                @Field("api") String api,
		                                @Field("method") String method,
		                                @Field("version") int version,
		                                @FieldMap Map<String, String> names);
	}

	// TODO: support cache
	private final Map<Method, MethodHandler> methodMethodHandlerMap = new LinkedHashMap<>();
	private HttpUrl baseUrl;
	private Gson gson;
	private WebApiService apiService;

	private Refine(Builder builder) {
		this.baseUrl = builder.baseUrl;

		gson = new Gson();

		ApiManager apiManager = new ApiManager();
		apiManager.put(baseUrl.uri(), "SYNO.API.Info", new ApiCookie(1, 1, "query.cgi", false));
		ApiHandler.setDefault(apiManager);
	}

	@SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
	public <T> T create(final Class<T> service) {
		if (!service.isInterface()) {
			throw new IllegalArgumentException("API declarations must be interfaces.");
		}
		if (service.getInterfaces().length > 0) {
			throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
//				.addConverterFactory(GsonConverterFactory.create())
				.build();
		apiService = retrofit.create(WebApiService.class);

		return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// If the method is a method from Object then defer to normal invocation.
						if (method.getDeclaringClass() == Object.class
								|| !method.isAnnotationPresent(WebAPI.class)) {
							return method.invoke(this, args);
						}
//						return loadMethodHandler(method).invoke(args);

						ResponseBody responseBody = invokeMethod(method, args);
						Object result = gson.fromJson(responseBody.charStream(), method.getGenericReturnType());
						if (result instanceof BasicVo) {
							// TODO: check WebAPI error
							BasicVo basicVo = (BasicVo) result;
							if (!basicVo.success) {
							}

							if (result instanceof ApiMapVo) {
								ApiManager apiManager = (ApiManager) ApiHandler.getDefault();
								Map<String, ApiVo> data = ((ApiMapVo) result).data;
								if (data != null) {
									for (Map.Entry<String, ApiVo> entry : data.entrySet()) {
										String name = entry.getKey();
										ApiVo apiVo = entry.getValue();
										apiManager.put(baseUrl.uri(), name,
												new ApiCookie(apiVo.maxVersion, apiVo.minVersion, apiVo.path, apiVo.jsonEncode()));
									}
								}
							}
						}
						return result;
					}
				});
	}

	private ResponseBody invokeMethod(Method method, Object[] args) throws IOException {
		if (!method.isAnnotationPresent(WebAPI.class)) {
			throw new IOException("No WebAPI annotation!");
		}

		WebAPI api = method.getAnnotation(WebAPI.class);
		ApiManager apiManager = (ApiManager) ApiHandler.getDefault();
		ApiCookie apiCookie = apiManager.get(baseUrl.uri()).get(api.api());

		// check api
		if (apiCookie == null) {
			BasicVo errorVo = new BasicVo(false, new ErrorCodeVo(BasicError.WEBAPI_ERR_NO_SUCH_API));
			return ResponseBody.create(MediaType.parse("text/plain"), gson.toJson(errorVo));
		}

		// fields
		Map<String, String> fieldMap = new LinkedHashMap<>();

		int version = api.version();
		Annotation[][] parameterAnnotationsArray = method.getParameterAnnotations();
		if (parameterAnnotationsArray != null) {
			for (int i = 0; i < parameterAnnotationsArray.length; i++) {
				Annotation[] parameterAnnotations = parameterAnnotationsArray[i];
				for (Annotation parameterAnnotation : parameterAnnotations) {
					if (parameterAnnotation instanceof Version) {
						version = (Integer) args[i];
					} else if (parameterAnnotation instanceof Field) {
						String key = ((Field) parameterAnnotation).value();
						String value = (String) args[i];
						fieldMap.put(key, apiCookie.jsonEncode() ? gson.toJson(value) : value);
					} else if (parameterAnnotation instanceof FieldMap) {
					}
				}
			}
		}

		// check version
		if (version > apiCookie.getMaxVersion()
				|| version < apiCookie.getMinVersion()) {
			BasicVo errorVo = new BasicVo(false, new ErrorCodeVo(BasicError.WEBAPI_ERR_NOT_SUPPORTED_VERSION));
			return ResponseBody.create(MediaType.parse("text/plain"), gson.toJson(errorVo));
		}

		String path = apiCookie.getPath();
		Call<ResponseBody> call = apiService.invokeWebApi(path, api.api(), api.method(), api.version(), fieldMap);
		Response<ResponseBody> response = call.execute();
		return response.body();
	}

	public static final class Builder {
		private HttpUrl baseUrl;

		public Builder baseUrl(HttpUrl baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}

		public Refine build() {
			if (baseUrl == null) {
				throw new IllegalStateException("Base URL required");
			}

			return new Refine(this);
		}
	}
}
