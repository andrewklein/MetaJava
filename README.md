MetaJava
========

Scan classes in the CLASSPATH for information similar to that obtainable through the Java reflection API.

Features
========
* Classes are not loaded unless explicitly called by the user.
* No external dependencies.
* Attempts to expose the same amount of functionality as the reflection API

Example
=======
Classes can be scanned by specifying a package. Below are some example use cases.

```
MetaJava metaJava = MetaJava.scanClassPath("au.aklein.metajava");


//Return a list of classes which have a specific annotation
List<ClassElement> methods = metaJava
        .where(ElementType.TYPE)
        .has(Element.Annotation(TestAnnotation.class))
        .get(ElementType.TYPE);

//Return a list of classes which have an method that is annotated with a specific annotation
List<ClassElement> methods = metaJava
        .where(ElementType.METHOD)
        .has(Element.Annotation(TestAnnotation.class))
        .get(ElementType.TYPE);
```
