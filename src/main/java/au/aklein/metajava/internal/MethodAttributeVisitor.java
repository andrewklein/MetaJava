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

package au.aklein.metajava.internal;


import au.aklein.metajava.AnnotationElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Process a method_info attribute to obtain relevant information
 */
public final class MethodAttributeVisitor extends AttributeVisitor {

    private List<AnnotationElement> annotations;
    private List<List<AnnotationElement>> parameterAnnotations;
    private CodeAttributeVisitor methodCodeAttribute;

    public MethodAttributeVisitor() {
        this.parameterAnnotations = new ArrayList<>();
        this.methodCodeAttribute = new CodeAttributeVisitor();
    }


    public List<List<AnnotationElement>> getParameterAnnotations() {
        return parameterAnnotations;
    }

    public void visitParameterAnnotation(ParameterAnnotationAttribute annotationsAttribute) {
        for(List<AnnotationData> parameter : annotationsAttribute.getParameterAnnotations()) {
            List<AnnotationElement> annotationsOnParameter = new ArrayList<>();
            for(AnnotationData annotation : parameter) {
                String annotationName = annotation.getType();
                annotationsOnParameter.add(new AnnotationElement(annotationName.substring(1, annotationName.length()-1)));
            }
            parameterAnnotations.add(annotationsOnParameter);
        }
    }

    public CodeAttributeVisitor getMethodCodeVisitor() {
        return methodCodeAttribute;
    }

    @SuppressWarnings("unchecked")
    public void visitCodeAttribute(CodeAttribute codeAttribute) {
        for(AttributeInfo attribute : codeAttribute.getAttributes()) {
            attribute.accept(methodCodeAttribute);
        }
    }


}
