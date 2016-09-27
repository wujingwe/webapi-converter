package me.oldjing.webapi;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import syno.api.Auth;
import syno.api.Encryption;
import syno.api.Info;
import syno.entry.Request;
import webapi.ApiConverterFactory;

public interface ApiService {

	@POST("webapi/query.cgi")
	Observable<Object> query(@Body Info info);

	@POST("webapi/{path}")
	Observable<EncryptVo> encrypt(@Path("path") String path,
	                              @Body Encryption encryption);

	@POST("webapi/{path}")
	Observable<Object> auth(@Path("path") String path,
							@Body Auth auth);

	@POST("webapi/{path}")
	Observable<Object> compound(@Path("path") String path,
								@Body Request request);

	class Creator {
		public static ApiService newService(OkHttpClient client, HttpUrl baseUrl) {
			Retrofit retrofit = new Retrofit.Builder()
					.client(client)
					.baseUrl(baseUrl.resolve("/"))
					.addConverterFactory(ApiConverterFactory.create())
					.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
					.build();
			return retrofit.create(ApiService.class);
		}
	}
}
