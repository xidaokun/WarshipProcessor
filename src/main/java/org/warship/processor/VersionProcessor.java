package org.warship.processor;


import com.google.auto.service.AutoService;

import org.warship.annotation.interfaces.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@SupportedAnnotationTypes({"org.warship.annotation.interfaces.Version",
		"org.warship.annotation.interfaces.Author",
		"org.warship.annotation.interfaces.Branch",
		"org.warship.annotation.interfaces.CommitId",
		"org.warship.annotation.interfaces.Date",
		"org.warship.annotation.interfaces.Description",
		"org.warship.annotation.interfaces.VersionCode"})
public class VersionProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

		System.out.println("process START===>");

		StringBuilder content = new StringBuilder();

		String classPackage = null;
		String className = null;
		for (Element element : roundEnvironment.getRootElements()) {
			className = element.getSimpleName().toString();
			classPackage = element.toString().replace("." + className, "");
		}
		System.out.println("className:" + className);
		System.out.println("classPackage:" + classPackage);

		if (className == null || classPackage == null) {
			return false;
		}

		content.append("package ")
				.append(classPackage)
				.append(";")
				.append("\n")
				.append("public final class ")
				.append(className)
				.append("{")
				.append("\n")
				.append("\n");

		for (TypeElement annotatedClass : ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(Version.class))) {

			boolean is = annotatedClass.getKind().isInterface();

			for (ExecutableElement executableElement : ElementFilter.methodsIn(annotatedClass.getEnclosedElements())) {
				String returnValue = executableElement.getReturnType().toString();
				String methodName = executableElement.toString();
				String annotation = null;
				for (AnnotationMirror mirror : executableElement.getAnnotationMirrors()) {
					annotation = mirror.getAnnotationType().asElement().getSimpleName().toString();
				}

				content.append("public static ")
						.append(returnValue)
						.append(" ")
						.append(methodName)
						.append(" ")
						.append("{")
						.append("\n")
						.append("\t")
						.append("return ")
						.append("\""+annotation+"\"")
						.append(";")
						.append("\n")
						.append("}")
						.append("\n")
						.append("\n");
			}
		}

		System.out.println(content.toString());

		try {
			String path = "./src/main/java/" + classPackage.replace(".", "/") + "/";
			System.out.println("path=====>" + path);
			content.append("}");
			writeFile(new File(path, className + ".java"), content.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("process END===>");

		return true;
	}

	private void writeFile(File file, String content) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(content.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
