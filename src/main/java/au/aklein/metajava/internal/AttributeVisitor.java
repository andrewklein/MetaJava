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
 * Process an attribute to obtain relevant information
 */
public abstract class AttributeVisitor {
    private List<AnnotationElement> annotations;
    private String signature;

    public AttributeVisitor() {
        this.annotations = new ArrayList<>();
    }

    public List<AnnotationElement> getAnnotations() {
        return annotations;
    }

    public void visitAnnotation(AnnotationAttribute annotationsAttribute) {
        for(AnnotationData annotation : annotationsAttribute.getAnnotations()) {
            String annotationName = annotation.getType();
            annotations.add(new AnnotationElement(annotationName.substring(1, annotationName.length()-1)));
        }
    }

    public void visitAttribute(AttributeInfo attribute) {
        //System.out.println("\t\tAttr: "+attribute.getName());
    }

    public boolean hasSignature() {
        return signature != null;
    }

    public String getSignature() {
        return signature;
    }

    public void visitSignature(SignatureAttribute signatureAttribute) {
        this.signature = signatureAttribute.getSignature();
    }
}
