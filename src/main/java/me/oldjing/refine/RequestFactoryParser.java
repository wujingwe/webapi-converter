package me.oldjing.refine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

final class RequestFactoryParser {
	// Upper and lower characters, digits, underscores, and hyphens, starting with a character.
	private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
	private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
	private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

	static RequestFactoryParser parse(Method method, Type responseType, Refine refine) {
		RequestFactoryParser parser = new RequestFactoryParser(method);

		Annotation[] methodAnnotations = method.getAnnotations();
		parser.parseMethodAnnotation(responseType, methodAnnotations);
//		parser.parseParameters(refine, methodAnnotations);

//		return parser.toRequestFactory(refine.baseUrl());
		return null;
	}

	private final Method method;

	private String httpMethod;
	private boolean hasBody;

	private RequestFactoryParser(Method method) {
		this.method = method;
	}

//	private RequestFactoryParser toRequestFactory() {
//
//	}

	private void parseMethodAnnotation(Type responseType, Annotation[] methodAnnotations) {
		for (Annotation annotation : methodAnnotations) {
		}
	}

	private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {

	}
}
