package me.oldjing.refine;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class MethodHandler {
	static MethodHandler create(Refine refine, Method method) {
		Type responseType = method.getGenericReturnType();
//		RequestFactory requestFactory = RequestFactoryParser.parse(method, responseType, refine);
		return new MethodHandler();
	}

	private MethodHandler() {

	}

	Object invoke(Object... args) {
		return null;
	}
}
