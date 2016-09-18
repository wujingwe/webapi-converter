package me.oldjing.myapi.converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class ApiConverterFactory extends Converter.Factory {
	/**
	 * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
	 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
	 */
	public static ApiConverterFactory create() {
		return create(new Gson());
	}

	/**
	 * Create an instance using {@code gson} for conversion. Encoding to JSON and
	 * decoding from JSON (when no charset is specified by a header) will use UTF-8.
	 */
	public static ApiConverterFactory create(Gson gson) {
		return new ApiConverterFactory(gson);
	}

	private final Gson gson;

	private ApiConverterFactory(Gson gson) {
		if (gson == null) throw new NullPointerException("gson == null");
		this.gson = gson;
	}

	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
	                                                        Retrofit retrofit) {
		TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
		return new GsonResponseBodyConverter<>(gson, adapter);
	}

	@Override
	public Converter<?, RequestBody> requestBodyConverter(Type type,
	                                                      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
		TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
		return new ApiRequestBodyConverter<>(gson, adapter);
	}
}
