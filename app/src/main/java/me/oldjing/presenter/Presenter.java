package me.oldjing.presenter;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.oldjing.model.Result;
import me.oldjing.webapi.ApiService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import syno.WebApi;
import syno.api.Encryption;
import syno.api.Info;
import syno.api.SyApiEncryption;
import syno.api.SyApiInfo;
import syno.entry.Request;
import syno.entry.SyApiRequest;

public class Presenter {
	private static final String TAG = Presenter.class.getSimpleName();

	private final ApiService service;
	private final Result result;

	public Presenter(ApiService service, Result result) {
		this.service = service;
		this.result = result;
	}

	public void onQueryAllClick() {
		Observable
				.defer(new Func0<Observable<Object>>() {
					@Override
					public Observable<Object> call() {
						Info info = new SyApiInfo()
								.query("all")
								.build(Info.QUERY, 1);
						return service.query(info);
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Object>() {
					@Override
					public void call(Object o) {
						result.content.set(o.toString());
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Log.e(TAG, "query all failed: ", throwable);
					}
				});
	}

	public void onCompoundClick() {
		Observable
				.defer(new Func0<Observable<Object>>() {
					@Override
					public Observable<Object> call() {
						Info info = new SyApiInfo()
								.query("all")
								.build(Info.QUERY, 1);
						Encryption encryption = new SyApiEncryption()
								.build(Encryption.GET_INFO, 1);
						List<WebApi> compound = new ArrayList<>();
						compound.add(info);
						compound.add(encryption);
						Request request = new SyApiRequest()
								.compound(compound)
								.build(Request.REQUEST, 1);
						return service.compound("entry.cgi", request);
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Object>() {
					@Override
					public void call(Object o) {
						result.content.set(o.toString());
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Log.e(TAG, "compound failed: ", throwable);
					}
				});
	}
}
