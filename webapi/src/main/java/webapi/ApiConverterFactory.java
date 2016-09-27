package webapi;

import com.google.common.reflect.TypeToken;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import annotation.Api;
import annotation.Method;
import annotation.Param;
import annotation.ParamMap;
import annotation.Version;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import syno.WebApi;

import static java.util.Collections.unmodifiableSet;

public class ApiConverterFactory extends Converter.Factory {
	/**
	 * Create an instance using a default {@link Moshi} instance for conversion. Encoding to JSON and
	 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
	 */
	public static ApiConverterFactory create() {
		Moshi moshi = new Moshi.Builder().build();
		return create(moshi);
	}

	/**
	 * Create an instance using {@code gson} for conversion. Encoding to JSON and
	 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
	 */
	public static ApiConverterFactory create(Moshi moshi) {
		if (moshi == null) throw new NullPointerException("moshi == null");
		return new ApiConverterFactory(moshi, false, false);
	}

	private final Moshi moshi;
	private final boolean lenient;
	private final boolean serializeNulls;

	private ApiConverterFactory(Moshi moshi, boolean lenient, boolean serializeNulls) {
		this.moshi = moshi.newBuilder()
				.add(new WebApiAdapter(moshi))
				.build();
		this.lenient = lenient;
		this.serializeNulls = serializeNulls;
	}

	/** Return a new factory which uses {@linkplain JsonAdapter#lenient() lenient} adapters. */
	public ApiConverterFactory asLenient() {
		return new ApiConverterFactory(moshi, true, serializeNulls);
	}

	/** Return a new factory which includes null values into the serialized JSON. */
	public ApiConverterFactory withNullSerialization() {
		return new ApiConverterFactory(moshi, lenient, true);
	}

	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
	                                                        Retrofit retrofit) {
		JsonAdapter<?> adapter = moshi.adapter(type, jsonAnnotations(annotations));
		return new MoshiResponseBodyConverter<>(adapter);
	}

	@Override
	public Converter<?, RequestBody> requestBodyConverter(Type type,
	                                                      Annotation[] parameterAnnotations,
														  Annotation[] methodAnnotations,
														  Retrofit retrofit) {
		JsonAdapter<?> adapter = moshi.adapter(type, jsonAnnotations(parameterAnnotations));
		return new ApiRequestBodyConverter<>(moshi, adapter, serializeNulls);
	}

	private static Set<? extends Annotation> jsonAnnotations(Annotation[] annotations) {
		Set<Annotation> result = null;
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().isAnnotationPresent(JsonQualifier.class)) {
				if (result == null) result = new LinkedHashSet<>();
				result.add(annotation);
			}
		}
		return result != null ? unmodifiableSet(result) : Collections.<Annotation>emptySet();
	}

	public class WebApiAdapter {
		private JsonAdapter<Map> adapter;

		public WebApiAdapter(Moshi moshi) {
			final Type type = new TypeToken<Map<String, String>>() {}.getType();
			adapter = moshi.adapter(type);
		}

		@ToJson String toJson(WebApi webApi) {
			Map<String, String> map = new HashMap<>();

			final Class cls = webApi.getClass();
			final Api api = webApi.getClass().getAnnotation(Api.class);
			map.put("api", api.value());

			final Field[] fields = cls.getDeclaredFields();
			for (Field myField : fields) {
				if (isSyApi(myField)) {
					final Pair<String, Object> param = param(webApi, myField);
					if (param != null) {
						final String name = param.first;
						final Object result = param.second;
						map.put(name, result.toString());
					}
				}

				if (myField.getAnnotation(ParamMap.class) != null) {
					final Map<String, Object> params = paramMap(webApi, myField);
					if (params != null) {
						for (Map.Entry<String, Object> entry : params.entrySet()) {
							final String name = entry.getKey();
							final Object result = entry.getValue();
							map.put(name, result.toString());
						}
					}
				}
			}
			return adapter.toJson(map);
		}

		@FromJson WebApi fromJson(String json) {
			return null;
		}

		private Pair<String, Object> param(Object value, Field field) {
			try {
				Json json = field.getAnnotation(Json.class);
				final String name = (json != null) ? json.name() : field.getName();
				final Object result = field.get(value);

				// FIXME: requestFormat issue

				return new Pair<>(name, result);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return null;
		}

		private Map<String, Object> paramMap(Object value, Field field) {
			Map<String, Object> map = new HashMap<>();

			try {
				Map<String, Object> params = (Map<String, Object>) field.get(value);
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					final String name = entry.getKey();
					final Object result = entry.getValue();

					// FIXME: requestFormat issue

					map.put(name, result);
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return map;
		}

		private boolean isSyApi(Field field) {
			return field.getAnnotation(Method.class) != null ||
					field.getAnnotation(Version.class) != null ||
					field.getAnnotation(Param.class) != null ||
					field.getAnnotation(ParamMap.class) != null;
		}
	}
}
