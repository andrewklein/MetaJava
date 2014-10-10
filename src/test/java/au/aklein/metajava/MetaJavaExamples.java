package au.aklein.metajava;


import au.aklein.metajava.support.TestAnnotation;

import java.util.List;

public class MetaJavaExamples {





    public static void main(String[] args)
    {
        MetaJava metaJava = MetaJava.scanClassPath("au.aklein.metajava", false);

        List<ClassElement> methods = metaJava
                .where(ElementType.METHOD)
                .has(Element.Annotation(TestAnnotation.class))
                .get(ElementType.TYPE);

        System.out.println(methods);

    }


}
