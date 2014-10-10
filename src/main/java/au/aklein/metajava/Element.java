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

import java.util.List;

/**
 * An element which is represented in a class file (either a {@link au.aklein.metajava.ClassElement},
 * a {@link au.aklein.metajava.MethodElement}, a {@link au.aklein.metajava.FieldElement} or an {@link au.aklein.metajava.AnnotationElement}
 */

public interface Element {

    /**
     * Get the name of the element being represented.
     * As this is based off the au.aklein.metajava.internal representation of the element's binary name,
     * names are delimited by forward slashes (/) instead of periods as described in the JLS.
     *
     * @return A string containing the element's name
     */
    public String getName();

    /**
     * Check if the current element contains the specified element.
     * @param element - The element to check.
     * @return - True if the current element contains the specified element, otherwise false.
     */
    public boolean has(Element element);

    /**
     * Report the {@link au.aklein.metajava.ElementType} of the current element.
     * @return - An ElementType object indicating the type of the element.
     */
    public ElementType type();

    /**
     * Get a list of associated Element objects of a specific {@link au.aklein.metajava.ElementType} from the current
     * element.
     *
     * This is a general interface
     * @param returnType - The type of Elements to return.
     * @return - A list of Element objects.
     */
    public <T extends Element> List<T> getAssociated(ElementType returnType);

    /**
     * Static factory method for creating {@link au.aklein.metajava.AnnotationElement} objects,
     * for the purposes of comparison.
     * @param annotationClass The annotation class to compare.
     * @return The resulting {@link au.aklein.metajava.AnnotationElement} instance.
     */
    public static AnnotationElement Annotation(Class<?> annotationClass) {
        return new AnnotationElement(annotationClass.getName().replace(".","/"));
    }

    /**
     * Static factory method for creating {@link au.aklein.metajava.MethodElement} objects for the purposes of
     * comparison.
     * @param methodName The name of the method to compare against.
     * @param parameterTypes - An array of parameter types, matching the target method signature.
     * @return The resulting {@link au.aklein.metajava.MethodElement} instance.
     */
    public static MethodElement Method(String methodName, Class<?>... parameterTypes) {
        StringBuilder signature = new StringBuilder();
        for(Class<?> parameter : parameterTypes) {
            signature.append(parameter.getName());
        }

        return new MethodElement(methodName, signature.toString());
    }


}
