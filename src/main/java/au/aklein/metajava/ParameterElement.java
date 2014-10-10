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
 * An object representing a parameter for a {@link au.aklein.metajava.MethodElement}
 */
public final class ParameterElement implements Element {

    private final String paramType;
    private final boolean isArray;
    private final int dimensions;
    private List<AnnotationElement> parameterAnnotations;
    private final MethodElement declaringMethod;

    public ParameterElement(String paramType, int dim, List<AnnotationElement> parameterAnnotations,
                            MethodElement declaringMethod) {
        this.paramType = paramType;
        this.isArray = dim > 0;
        this.dimensions = dim;
        this.parameterAnnotations = parameterAnnotations;
        this.declaringMethod = declaringMethod;
    }

    /**
     * Gets the name of the type associated with this parameter based on the class file au.aklein.metajava.internal format.
     *
     * @return - A String with the name of the parameter type
     */
    public String getInternalName() {
        if(isArray) return arrayStr(false);
        return paramType;
    }

    /**
     * Gets the name of the type associated with this parameter. Emulates the formatting behavior of {@link java.lang
     * .reflect.Parameter#toString()}
     * @return - A String with the name of the parameter type
     */
    @Override
    public String getName() {
        if(isArray) return arrayStr(true).replace('/', '.');
        return formatType().replace('/', '.');
    }

    /**
     * Gets the name of the type associated with this parameter. Emulates the formatting behavior of {@link java.lang
     * .Class#getName() }
     * @return - A String with the name of the parameter type
     */
    public String getTypeName() {
        if(isArray) return arrayStr(false).replace('/', '.');
        return formatType().replace('/', '.');
    }

    /**
     * Returns the {@link au.aklein.metajava.MethodElement} representing the type which has declared this parameter.
     * @return - A {@link au.aklein.metajava.MethodElement} object of the declaring type.
     */
    public MethodElement getDeclaringMethod() {
        return declaringMethod;
    }


    @Override
    public boolean has(Element element) {
        switch (element.type()) {
            case ANNOTATION:
                for(AnnotationElement annotationElement : parameterAnnotations) {
                    if(element.equals(annotationElement)) return true;
                }
                break;
            default: return false;
        }
        return false;
    }

    @Override
    public ElementType type() {
        return ElementType.PARAMETER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> List<T> getAssociated(ElementType returnType) {
        switch (returnType) {
            case METHOD: return new ArrayList<T>(){{ add((T) declaringMethod);}};
            case TYPE: return new ArrayList<T>(){{ add((T) declaringMethod.getDeclaringClass()); }};
            default:
                final List<T> elements = new ArrayList<>();
                elements.add((T) this);
                return elements;
        }
    }

    /**
     * Determines if the current parameter is an array instance.
     * @return - True if the parameter is an array, otherwise false.
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * Gets the number of array dimensions specified by this parameter.
     * @return - The number of array dimensions.
     */
    public int getDimensionCount() {
        return dimensions;
    }


    /**
     * Returns {@link au.aklein.metajava.AnnotationElement AnnotationElements} which are present on the parameter.
     * @return - A list of AnnotationElements
     */
    public List<AnnotationElement> getAnnotations() {
        return parameterAnnotations;
    }


    private String arrayStr(boolean format) {
        StringBuilder array = new StringBuilder();

        if(format) array.append(formatType());

        for(int i=0; i < dimensions; i++ ) {
            if(format) array.append("[]");
            else array.append('[');
        }

        if(!format) array.append(paramType);

        return array.toString();
    }

    private String formatType() {
        switch(paramType.charAt(0)) {
            case 'B': return "byte";
            case 'C': return "char";
            case 'D': return "double";
            case 'F': return "float";
            case 'I': return "int";
            case 'J': return "long";
            case 'S': return "short";
            case 'Z': return "boolean";
            case 'V': return "void";
            default:
                return paramType.substring(1, paramType.length() - 1);
        }
    }


}
