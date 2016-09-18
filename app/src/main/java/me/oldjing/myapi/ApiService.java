package me.oldjing.myapi;

import com.google.gson.JsonObject;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import syno.api.Auth;
import syno.api.Encryption;
import syno.api.Info;
import syno.entry.Request;

public interface ApiService {

	@POST("webapi/query.cgi")
	Observable<JsonObject> query(@Body Info info);

	@POST("webapi/{path}")
	Observable<EncryptVo> encrypt(@Path("path") String path,
	                              @Body Encryption encryption);

	@POST("webapi/{path}")
	Observable<JsonObject> auth(@Path("path") String path,
	                            @Body Auth auth);

	@POST("webapi/{path}")
	Observable<JsonObject> compound(@Path("path") String path,
	                                @Body Request request);
}
