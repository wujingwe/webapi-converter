package me.oldjing.myapi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.oldjing.myapi.EncryptVo.CipherDataVo;
import me.oldjing.myapi.util.CgiEncryption;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
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
import syno.api.SyApiAuth;
import syno.api.SyApiEncryption;
import syno.api.SyApiInfo;
import syno.core.upgrade.server.Server;
import syno.core.upgrade.server.SyApiServer;
import syno.entry.Request;
import syno.entry.SyApiRequest;

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

		HttpUrl baseUrl = HttpUrl.parse("http://192.168.32.102:25000");
		final ApiService apiService = ApiService.Creator.newService(client, baseUrl);

		Observable
				.defer(new Func0<Observable<Object>>() {
					@Override
					public Observable<Object> call() {
						Info info = new SyApiInfo()
								.query("all")
								.build(Info.QUERY, 1);
						return apiService.query(info);
					}
				})
				.flatMap(new Func1<Object, Observable<EncryptVo>>() {
					@Override
					public Observable<EncryptVo> call(Object object) {
						Encryption encryption = new SyApiEncryption()
								.build(Encryption.GET_INFO, 1);
						return apiService.encrypt("encryption.cgi", encryption);
					}
				})
				.flatMap(new Func1<EncryptVo, Observable<Object>>() {
					@Override
					public Observable<Object> call(EncryptVo encryptVo) {
						final CipherDataVo cipherDataVo = encryptVo.data;
						final int timeBias = cipherDataVo.server_time - (int) (System.currentTimeMillis() / 1000);
						final CgiEncryption encrypt = new CgiEncryption(
								cipherDataVo.publicKey, cipherDataVo.cipherToken, cipherDataVo.cipherKey, timeBias);

						Map<String, String> params = new HashMap<>();
						params.put("account", "admin");
						params.put("passwd", "w3025156");
						params = encrypt.encryptFromParams(params);

						Auth auth = new SyApiAuth()
								.clientTime(System.currentTimeMillis() / 1000)
								.session("dsm")
								.params(params)
								.build(Auth.LOGIN, 3);
						return apiService.auth("auth.cgi", auth);
					}
				})
				.flatMap(new Func1<Object, Observable<Object>>() {
					@Override
					public Observable<Object> call(Object object) {
						Server server1 = new SyApiServer()
								.build(Server.CHECK, 1);
						Server server2 = new SyApiServer()
								.build(Server.CHECK, 1);
						List<WebApi> compound = new ArrayList<>();
						compound.add(server1);
						compound.add(server2);
						Request request = new SyApiRequest()
								.compound(compound)
								.build(Request.REQUEST, 1);
						return apiService.compound("entry.cgi", request);
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeOn(Schedulers.io())
				.subscribe(new Action1<Object>() {
					@Override
					public void call(Object object) {
						String result = object.toString();
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
