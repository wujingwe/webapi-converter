package me.oldjing.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.oldjing.model.Result;
import me.oldjing.presenter.Presenter;
import me.oldjing.webapi.ApiService;
import me.oldjing.webapi.R;
import me.oldjing.webapi.databinding.ActivityMainBinding;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private Result mResult = new Result();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		binding.setResult(mResult);

		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		logging.setLevel(Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(logging)
				.build();

		HttpUrl baseUrl = HttpUrl.parse("https://chat.synology.com");
		final ApiService apiService = ApiService.Creator.newService(client, baseUrl);

		Presenter presenter = new Presenter(apiService, mResult);
		binding.setPresenter(presenter);
	}
}
