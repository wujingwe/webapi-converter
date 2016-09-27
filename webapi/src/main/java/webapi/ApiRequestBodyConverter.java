package webapi;

import com.google.common.reflect.TypeToken;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import annotation.Api;
import annotation.Compound;
import annotation.Method;
import annotation.Param;
import annotation.ParamMap;
import annotation.Version;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Converter;
import syno.WebApi;

final class ApiRequestBodyConverter<T> implements Converter<T, RequestBody> {
	private static final MediaType MEDIA_TYPE =
			MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private final Moshi moshi;
//	private final JsonAdapter<T> mAdapter;
	private final boolean serializeNulls;

	ApiRequestBodyConverter(Moshi moshi, JsonAdapter<T> adapter, boolean serializeNulls) {
		this.moshi = moshi;
//		mAdapter = adapter;
		this.serializeNulls = serializeNulls;
	}

	@Override
	public RequestBody convert(T value) throws IOException {
		final Class cls = value.getClass();
		final String api = api(cls);

		final Field[] fields = cls.getDeclaredFields();
		final String method = method(value, fields);
		final String version = version(value, fields);

		FormBody.Builder builder = new FormBody.Builder();
		builder.add("api", api);
		builder.add("method", method);
		builder.add("version", version);

		// handle compound separately
		if (cls.getAnnotation(Compound.class) != null) {
			for (Field field : fields) {
				if (field.getAnnotation(Param.class) != null) {
					List<String> jsonStrings = compound(value, field);

					final Type type = new TypeToken<List<String>>() {}.getType();
					JsonAdapter<List> adapter = moshi.adapter(type);
					builder.add("compound", adapter.toJson(jsonStrings));
				}
			}

		} else {
			for (Field field : fields) {
				if (field.getAnnotation(Param.class) != null) {
					final Pair<String, Object> param = param(value, field);
					if (param != null) {
						final String name = param.first;
						final String result = param.second.toString();
						builder.add(name, result);
					}
				}

				if (field.getAnnotation(ParamMap.class) != null) {
					final Map<String, Object> params = paramMap(value, field);
					if (params != null) {
						for (Entry<String, Object> entry : params.entrySet()) {
							final String name = entry.getKey();
							final String result = entry.getValue().toString();
							builder.add(name, result);
						}
					}
				}
			}
		}

		final RequestBody delegate = builder.build();
		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return MEDIA_TYPE;
			}

			@Override
			public long contentLength() throws IOException {
				return delegate.contentLength();
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				delegate.writeTo(sink);
			}
		};
	}


	private String api(Class cls) {
		Annotation annotation = cls.getAnnotation(Api.class);
		if (annotation == null) {
			throw new IllegalArgumentException("No @Api provided");
		}
		Api api = (Api) annotation;
		return api.value();
	}

	private String method(T value, Field[] fields) {
		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Method.class);
			if (annotation == null) {
				continue;
			}

			try {
				field.setAccessible(true);
				Object result = field.get(value);
				if (result != null) {
					return result.toString();
				}
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}
		throw new IllegalArgumentException("No @Method provided");
	}

	private String version(T value, Field[] fields) {
		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Version.class);
			if (annotation == null) {
				continue;
			}

			try {
				field.setAccessible(true);
				Object result = field.get(value);
				if (result != null) {
					return result.toString();
				}
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}
		throw new IllegalArgumentException("No @Version provided");
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
			for (Entry<String, Object> entry : params.entrySet()) {
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

	@SuppressWarnings("unchecked")
	private List<String> compound(T value, Field field) {
		List<String> jsonStrings = new ArrayList<>();

		try {
			Object object = field.get(value);
			if (object instanceof List) {
				List<WebApi> webApis = (List<WebApi>) object;

				final Type type = new TypeToken<Map<String, String>>() {}.getType();
				JsonAdapter<Map> adapter = moshi.adapter(type);

				for (WebApi webApi : webApis) {
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
								for (Entry<String, Object> entry : params.entrySet()) {
									final String name = entry.getKey();
									final Object result = entry.getValue();
									map.put(name, result.toString());
								}
							}
						}
					}
					jsonStrings.add(adapter.toJson(map));
				}
				return jsonStrings;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean isSyApi(Field field) {
		return field.getAnnotation(Method.class) != null ||
				field.getAnnotation(Version.class) != null ||
				field.getAnnotation(Param.class) != null ||
				field.getAnnotation(ParamMap.class) != null;
	}
}