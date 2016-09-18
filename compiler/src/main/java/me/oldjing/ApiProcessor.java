package me.oldjing;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import annotation.Api;
import annotation.Method;
import annotation.Param;
import annotation.ParamMap;
import annotation.Version;

import static java.util.Collections.singleton;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class ApiProcessor extends AbstractProcessor {

	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		messager = processingEnv.getMessager();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
//		Set<String> set = new HashSet<>();
//		set.add(Api.class.getCanonicalName());
//		set.add(Method.class.getCanonicalName());
//		set.add(Version.class.getCanonicalName());
//		return set;

		return singleton(Api.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		messager.printMessage(Kind.OTHER, "process()");

		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Api.class)) {
			// Our annotation is defined with @Target(value=TYPE). Therefore, we can assume that
			// this annotatedElement is a TypeElement.
			TypeElement annotatedClass = (TypeElement) annotatedElement;
			if (!isValidClass(annotatedClass)) {
				return true;
			}

			try {
				genApiBuilderCls(annotatedClass);
			} catch (IOException ex) {
				messager.printMessage(ERROR,
						"Couldn't generate class: " + annotatedClass.getSimpleName());
			}
		}
		return true;
	}

	private boolean isValidClass(TypeElement annotatedClass) {
		if (!ClassValidator.isPublic(annotatedClass)) {
			String message = String.format("Classes annotated with %s must be public.",
					annotatedClass.getSimpleName());
			messager.printMessage(ERROR, message, annotatedClass);
			return false;
		}

		if (ClassValidator.isAbstract(annotatedClass)) {
			String message = String.format("Classes annotated with %s must not be abstract.",
					annotatedClass.getSimpleName());
			messager.printMessage(ERROR, message, annotatedClass);
			return false;
		}
		return true;
	}

	private boolean isApiField(Element element) {
		return (element instanceof VariableElement) &&
				(element.getAnnotation(Method.class) != null ||
				 element.getAnnotation(Version.class) != null ||
				 element.getAnnotation(Param.class) != null ||
				 element.getAnnotation(ParamMap.class) != null);
	}

	private void genApiBuilderCls(TypeElement annotatedClass) throws IOException {
		final String packageName = getPackageName(processingEnv.getElementUtils(), annotatedClass);
		final String className = "SyApi_" + annotatedClass.getSimpleName();

		TypeSpec.Builder builder = TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		// Add static fields
		genConstantFields(annotatedClass, builder);

		// Add private fields
		genPrivateFields(annotatedClass, builder, packageName, className);

		// build() method
		MethodSpec buildMethod = genBuildMethod(annotatedClass);
		builder.addMethod(buildMethod);

		TypeSpec typeSpec = builder.build();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
				.build();
		javaFile.writeTo(processingEnv.getFiler());
	}

	private String getPackageName(Elements elementUtils, TypeElement typeElement) throws IOException {
		PackageElement pkg = elementUtils.getPackageOf(typeElement);
		if (pkg.isUnnamed()) {
			throw new IOException("The package of " + typeElement.getSimpleName() + " has no name");
		}
		return pkg.getQualifiedName().toString();
	}

	private void genConstantFields(TypeElement annotatedClass, TypeSpec.Builder builder) {
		final Api api = annotatedClass.getAnnotation(Api.class);

		// name
		FieldSpec name = FieldSpec.builder(String.class, "NAME")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
				.initializer("$S", api.value())
				.build();
		builder.addField(name);

		// version
		FieldSpec version = FieldSpec.builder(TypeName.INT, "VERSION")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
				.initializer("$L", api.version())
				.build();
		builder.addField(version);
	}

	private void genPrivateFields(TypeElement annotatedClass, TypeSpec.Builder builder,
	                              String packageName, String className) {
		boolean hasMethod = false;
		boolean hasVersion = false;

		for (Element element : annotatedClass.getEnclosedElements()) {
			if (!isApiField(element)) {
				continue;
			}

			final VariableElement variableElement = (VariableElement) element;
			final String fieldName = variableElement.getSimpleName().toString();
			final TypeName typeName = TypeName.get(variableElement.asType());

			FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(
					typeName, fieldName, Modifier.PRIVATE);
			for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors()) {
				AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
				fieldSpecBuilder.addAnnotation(annotationSpec);
			}
			FieldSpec fieldSpec = fieldSpecBuilder.build();
			builder.addField(fieldSpec);

			MethodSpec methodSpec = MethodSpec.methodBuilder(fieldName)
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(packageName, className))
					.addParameter(typeName, fieldName)
					.addStatement("this.$L = $L", fieldName, fieldName)
					.addStatement("return this")
					.build();
			builder.addMethod(methodSpec);

			if (element.getAnnotation(Method.class) != null) {
				hasMethod = true;
			}
			if (element.getAnnotation(Version.class) != null) {
				hasVersion = true;
			}
		}

		if (!hasMethod) {
			final TypeName typeName = TypeName.get(String.class);
			FieldSpec method = FieldSpec.builder(typeName, "method")
					.addModifiers(Modifier.PRIVATE)
					.addAnnotation(Method.class)
					.build();
			builder.addField(method);

			MethodSpec methodSpec = MethodSpec.methodBuilder("method")
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(packageName, className))
					.addParameter(typeName, "method")
					.addStatement("this.method = method")
					.addStatement("return this")
					.build();
			builder.addMethod(methodSpec);
		}

		if (!hasVersion) {
			final TypeName typeName = TypeName.INT;
			FieldSpec version = FieldSpec.builder(typeName, "version")
					.addModifiers(Modifier.PRIVATE)
					.addAnnotation(Version.class)
					.build();
			builder.addField(version);

			MethodSpec methodSpec = MethodSpec.methodBuilder("version")
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(packageName, className))
					.addParameter(typeName, "version")
					.addStatement("this.version = version")
					.addStatement("return this")
					.build();
			builder.addMethod(methodSpec);
		}
	}

	private MethodSpec genBuildMethod(TypeElement annotatedClass) {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("build")
				.addModifiers(Modifier.PUBLIC)
				.returns(TypeName.get(annotatedClass.asType()))
				.addParameter(TypeName.get(String.class), "method")
				.addParameter(TypeName.INT, "version")
				.addStatement("$T instance = new $T()", TypeName.get(annotatedClass.asType()), TypeName.get(annotatedClass.asType()));

		for (Element element : annotatedClass.getEnclosedElements()) {
			if (!isApiField(element)) {
				continue;
			}

			final VariableElement variableElement = (VariableElement) element;
			final String fieldName = variableElement.getSimpleName().toString();
			builder.addStatement("instance.$L = $L", fieldName, fieldName);
		}

		builder.addStatement("return instance");
		return builder.build();
	}
}
