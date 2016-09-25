package webapi;

import com.squareup.moshi.JsonAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ByteString;
import retrofit2.Converter;

final class MoshiResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	private static final ByteString UTF8_BOM = ByteString.decodeHex("EFBBBF");

	private final JsonAdapter<T> adapter;

	MoshiResponseBodyConverter(JsonAdapter<T> adapter) {
		this.adapter = adapter;
	}

	@Override
	public T convert(ResponseBody value) throws IOException {
		BufferedSource source = value.source();
		try {
			// Moshi has no document-level API so the responsibility of BOM skipping falls to whatever
			// is delegating to it. Since it's a UTF-8-only library as well we only honor the UTF-8 BOM.
			if (source.rangeEquals(0, UTF8_BOM)) {
				source.skip(UTF8_BOM.size());
			}
			return adapter.fromJson(source);
		} finally {
			value.close();
		}
	}
}
