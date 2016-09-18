package me.oldjing.myapi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.oldjing.myapi.EncryptVo.CipherDataVo;
import me.oldjing.myapi.converter.ApiConverterFactory;
import me.oldjing.myapi.util.CgiEncryption;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import syno.WebApi;
import syno.api.Auth;
import syno.api.Encryption;
import syno.api.Info;
import syno.api.SyApi_Auth;
import syno.api.SyApi_Encryption;
import syno.api.SyApi_Info;
import syno.core.upgrade.server.Server;
import syno.core.upgrade.server.SyApi_Server;
import syno.entry.Request;
import syno.entry.SyApi_Request;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = (TextView) findViewById(R.id.text);

		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		logging.setLevel(Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(logging)
				.build();

		HttpUrl baseUrl = HttpUrl.parse("http://localhost");
		Retrofit retrofit = new Retrofit.Builder()
				.callFactory(client)
				.baseUrl(baseUrl)
				.addConverterFactory(ApiConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.build();
		final ApiService apiService = retrofit.create(ApiService.class);

		Observable
				.defer(new Func0<Observable<JsonObject>>() {
					@Override
					public Observable<JsonObject> call() {
						Info info = new SyApi_Info()
								.query("all")
								.build(Info.QUERY, 1);
						return apiService.query(info);
					}
				})
				.flatMap(new Func1<JsonObject, Observable<EncryptVo>>() {
					@Override
					public Observable<EncryptVo> call(JsonObject jsonObject) {
						Encryption encryption = new SyApi_Encryption()
								.build(Encryption.GET_INFO, 1);
						return apiService.encrypt("encryption.cgi", encryption);
					}
				})
				.flatMap(new Func1<EncryptVo, Observable<JsonObject>>() {
					@Override
					public Observable<JsonObject> call(EncryptVo encryptVo) {
						final CipherDataVo cipherDataVo = encryptVo.data;
						final int timeBias = cipherDataVo.server_time - (int) (System.currentTimeMillis() / 1000);
						final CgiEncryption encrypt = new CgiEncryption(
								cipherDataVo.publicKey, cipherDataVo.cipherToken, cipherDataVo.cipherKey, timeBias);

						Map<String, String> params = new HashMap<>();
						params.put("account", "user");
						params.put("passwd", "password");
						params = encrypt.encryptFromParams(params);

						Auth auth = new SyApi_Auth()
								.clientTime(System.currentTimeMillis() / 1000)
								.session("dsm")
								.params(params)
								.build(Auth.LOGIN, 3);
						return apiService.auth("auth.cgi", auth);
					}
				})
				.flatMap(new Func1<JsonObject, Observable<JsonObject>>() {
					@Override
					public Observable<JsonObject> call(JsonObject jsonObject) {
						Server server1 = new SyApi_Server()
								.build(Server.CHECK, 1);
						Server server2 = new SyApi_Server()
								.build(Server.CHECK, 1);
						List<WebApi> compound = new ArrayList<>();
						compound.add(server1);
						compound.add(server2);
						Request request = new SyApi_Request()
								.compound(compound)
								.build(Request.REQUEST, 1);
						return apiService.compound("entry.cgi", request);
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeOn(Schedulers.io())
				.subscribe(new Action1<JsonObject>() {
					@Override
					public void call(JsonObject jsonObject) {
						String result = jsonObject.toString();
						Log.e(TAG, result);

						mTextView.setText(result);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable e) {
						Log.e(TAG, "login failed:", e);
					}
				});
	}
}
