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

import java.util.ArrayList;
import java.util.List;

/**
 * An object representing an annotation on a type, method or field.
 */
public final class AnnotationElement implements Element {

    private final String annotationClass;
    private final boolean paramaterAnnotation;

    public AnnotationElement(String annotationClass) {
        this.annotationClass = annotationClass;
        this.paramaterAnnotation = false;
    }

    public Class<?> getType(ClassLoader classloader) throws ClassNotFoundException {
        return classloader.loadClass(annotationClass);
    }

    public boolean isParamaterAnnotation() {
        return paramaterAnnotation;
    }

    @Override
    public String getName() {
        return annotationClass;
    }

    @Override
    public boolean has(Element element) {
        return false;
    }

    @Override
    public ElementType type() {
        return ElementType.ANNOTATION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> List<T> getAssociated(ElementType returnType) {
        switch (returnType) {
            default:
                final List<T> elements = new ArrayList<>();
                elements.add((T) this);
                return elements;
        }
    }

    @Override
    public int hashCode() {
        return 31*annotationClass.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if(!(other instanceof AnnotationElement)) return false;
        return this.annotationClass.equals(((AnnotationElement) other).getName());
    }



}
