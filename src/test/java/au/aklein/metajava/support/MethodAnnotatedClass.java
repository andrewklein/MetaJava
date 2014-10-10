package au.aklein.metajava.support;


public class MethodAnnotatedClass {

    @TestAnnotation
    public MethodAnnotatedClass() {

    }

    public MethodAnnotatedClass(int x) {

    }

    @TestAnnotation
    public boolean exampleMethod() {
        return false;
    }
}
