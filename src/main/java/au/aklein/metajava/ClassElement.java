/*
 * Copyright 2014 Andrew Klein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.aklein.metajava;

import au.aklein.metajava.internal.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * An object representing a type {@link au.aklein.metajava.Element} (class/interface/enum) inside a class file.
 */
public final class ClassElement implements Element {
    private final Path classPath;
    private final short accessFlags;

    private final List<MethodElement> methods;
    private final List<FieldElement> fields;
    private final List<AnnotationElement> annotations;

    private final List<ClassElement> innerClasses;
    private final List<String> interfaces;

    private final String className;
    private final String parentClass;

    private final String outerClass;

    private final boolean anonymous;

    private ClassElement(ClassElementBuilder builder) {
        this.methods = new ArrayList<>();


        for(MethodElement.MethodElementBuilder methodBuilder : builder.methods) {
            methods.add(methodBuilder.build(this));
        }
        this.fields = new ArrayList<>();
        for(FieldElement.FieldElementBuilder fieldBuilder : builder.fields) {
            fields.add(fieldBuilder.build(this));
        }

        this.annotations = builder.annotations;
        this.classPath = builder.classPath;
        this.innerClasses = builder.innerClasses;
        this.interfaces = builder.interfaces;
        this.className = builder.className;
        this.parentClass = builder.parentClass;
        this.outerClass = builder.outerClass;
        this.anonymous = builder.isAnonymous;
        this.accessFlags = builder.accessFlags;
    }

    @Override
    public String getName() {
        return className;
    }

    /**
     * Return a {@link java.nio.file.Path} object pointing to the physical location of the class file on disk.
     * @return - A Path object of the class file.
     */
    public Path getClassPath() {
        return classPath;
    }

    /**
     * Returns a class instance after loading the class with the specified {@link ClassLoader}
     * @param classLoader - The ClassLoader to load the class with
     * @return - An instance of the current class
     * @throws ClassNotFoundException
     */
    public Class<?> getClassType(ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader.loadClass(className.replace("/", "."));
    }

    /**
     * Returns a period (.) delimited representation of the class's name.
     * @return A string containing the class name.
     */
    public String getClassName() {
        return className.replace("/", ".");
    }

    /**
     * Gets a list of ClassElement objects representing the current class's inner classes.
     * @return A list of ClassElements
     */
    public List<ClassElement> getInnerClasses() {
        return innerClasses;
    }

    /**
     * Gets a list of {@link au.aklein.metajava.MethodElement} objects representing the current class's member methods.
     * @return A list of {@link au.aklein.metajava.MethodElement} objects
     */
    public List<MethodElement> getMethods() {
        return methods;
    }

    /**
     * Gets a {@link au.aklein.metajava.MethodElement} object representing the current class's default constructor
     * @return A {@link au.aklein.metajava.MethodElement} object
     */
    public MethodElement getDefaultConstructorElement() {
        for(MethodElement methodElement : methods) {
            if(methodElement.getName().equals("<init>")) return methodElement;
        }
        return null;
    }

    /**
     * Gets a list of {@link au.aklein.metajava.FieldElement} objects representing the current class's member fields.
     * @return A list of {@link au.aklein.metajava.FieldElement} objects
     */
    public List<FieldElement> getFields() {
        return fields;
    }

    @Override
    public int hashCode() {
        return 31*className.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if (!(other instanceof ClassElement)) return false;
        return this.className.equals(((ClassElement) other).getName());
    }

    @Override
    public boolean has(Element element) {
        switch (element.type()) {
            case ANNOTATION:
                for(AnnotationElement annotationElement : annotations) {
                    if(element.equals(annotationElement)) return true;
                }
                break;
            case TYPE:
                for(ClassElement innerClass : innerClasses) {
                    if(element.equals(innerClass)) return true;
                }
                break;
            case METHOD:
                for(MethodElement method : methods) {
                    if(element.equals(method)) return true;
                }
                break;
            case FIELD:
                for(FieldElement field : fields) {
                    if(element.equals(field)) return true;
                }
                break;
            default: return false;
        }
        return false;
    }

    @Override
    public ElementType type() {
        return ElementType.TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> List<T> getAssociated(ElementType returnType) {
        switch (returnType) {
            case TYPE: return (List<T>) innerClasses;
            case ANNOTATION: return (List<T>) annotations;
            case METHOD: return (List<T>) methods;
            case FIELD: return (List<T>) fields;
            default:
                final List<T> elements = new ArrayList<>();
                elements.add((T) this);
                return elements;
        }
    }


    /**
     * Determines if the class is an inner class.
     * @return True if the class is an inner class, otherwise false.
     */
    public boolean isInner() {
        return outerClass!=null;
    }

    /**
     * Determines if the class represents an anonymous class
     * @return True if the class element is an anonymous class, otherwise false
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Determines if the class is scoped as public.
     * @return True if the class element is public, otherwise false.
     */
    public boolean isPublic() {
        return (accessFlags & 0x0001) == 1;
    }

    /**
     * Determines if the class has been declared final.
     * @return True if the class element is final, otherwise false.
     */
    public boolean isFinal() {
        return (accessFlags & 0x0010) != 0;
    }

    /**
     * Determines if the class represents and interface
     * @return True if the class element is an interface, otherwise false
     */
    public boolean isInterface() {
        return (accessFlags & 0x0200) != 0;
    }

    /**
     * Determines if the class is an abstract class
     * @return True if the class element is abstract, otherwise false
     */
    public boolean isAbstract() {
        return (accessFlags & 0x0400) != 0;
    }

    /**
     * Determines if the class is a synthetic class, not present in the source code.
     * @return True if the class element is synthetic, otherwise false.
     */
    public boolean isSynthetic() {
        return (accessFlags & 0x1000) != 0;
    }

    /**
     * Determines if the class is an annotation type
     * @return True if the class element is an annotation, otherwise false
     */
    public boolean isAnnotation() {
        return (accessFlags & 0x2000) != 0;
    }

    /**
     * Determines if the class is an enum type
     * @return True if the class element is an enum, otherwise false.
     */
    public boolean isEnum() {
        return (accessFlags & 0x4000) != 0;
    }


    /**
     * Builder class used to construct ClassElement objects from class file data
     */
    public final static class ClassElementBuilder {
        private Path classPath;
        private short accessFlags;
        private ClassFileReader.ClassData classFileData;

        private List<MethodElement.MethodElementBuilder> methods;
        private List<FieldElement.FieldElementBuilder> fields;
        private List<AnnotationElement> annotations;
        private List<ClassElement> innerClasses;
        private List<String> interfaces;

        private String className;
        private String parentClass;

        private String outerClass;

        private List<String> innerClassNames;
        private boolean isInner;
        private boolean isAnonymous;

        private ClassElementBuilder(Path classPath, ClassFileReader.ClassData classData) {
            this.classPath = classPath;
            this.classFileData = classData;
        }

        public static ClassElementBuilder beginNewClassElement(Path classPath, ClassFileReader.ClassData classData) {
            return new ClassElementBuilder(classPath,classData).generateElements();
        }

        @SuppressWarnings("unchecked")
        private ClassElementBuilder generateElements() {
            this.className = classFileData.getClassName();
            this.parentClass = classFileData.getSuperClassName();
            this.accessFlags = classFileData.getAccessFlags();
            this.interfaces = classFileData.getInterfaces();

            this.innerClasses = new ArrayList<>();

            List<MethodElement.MethodElementBuilder> methodElements = new ArrayList<>();
            List<FieldElement.FieldElementBuilder> fieldElements = new ArrayList<>();

            ClassAttributeVisitor classAttributes = new ClassAttributeVisitor(className);
            for(AttributeInfo attribute : classFileData.getAttributes()) {
                attribute.accept(classAttributes);
            }

            this.isAnonymous = classAttributes.isAnonymous();
            this.outerClass = classAttributes.getOuterClass();
            this.isInner = classAttributes.isInner();
            this.innerClassNames = classAttributes.getInnerClassNames();

            for(FieldMethodInfo field : classFileData.getFields()) {
                FieldAttributeVisitor fieldAttributes = new FieldAttributeVisitor();
                for(AttributeInfo attribute : field.getAttributes()) {
                    attribute.accept(fieldAttributes);
                }
                fieldElements.add(new FieldElement.FieldElementBuilder(field, fieldAttributes));
            }

            for(FieldMethodInfo method : classFileData.getMethods()) {
                MethodAttributeVisitor methodAttributes = new MethodAttributeVisitor();
                for(AttributeInfo attribute : method.getAttributes()) {
                    attribute.accept(methodAttributes);
                }
                methodElements.add(new MethodElement.MethodElementBuilder(method, methodAttributes));
            }

            this.annotations = classAttributes.getAnnotations();
            this.methods = methodElements;
            this.fields = fieldElements;

            return this;
        }

        public ClassElement construct() {
            return new ClassElement(this);
        }

        public boolean isInner() {
            return isInner;
        }

        public ClassElementBuilder addInner(ClassElement innerClass) {
            innerClasses.add(innerClass);
            return this;
        }

        public boolean hasInner(ClassElement innerClass) {
            for(String className : innerClassNames) {
                if(className.equals(innerClass.getName())) return true;
            }
            return false;
        }
    }





}
