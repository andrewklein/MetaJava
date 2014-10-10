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

import java.util.List;

/**
 * Holds the attribute information for a parameter annotation.
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.18">Class File Specification
 * - RuntimeVisibleParameterAnnotations</a>
 */
public final class ParameterAnnotationAttribute implements AttributeInfo<MethodAttributeVisitor, Void> {
    private final String attrName;
    private final List<List<AnnotationData>> annotations;
    private final boolean runtimeVisible;

    public ParameterAnnotationAttribute(String attrName,
                               List<List<AnnotationData>> annotations,
                               boolean runtimeVisible) {
        this.attrName = attrName;
        this.annotations = annotations;
        this.runtimeVisible = runtimeVisible;
    }

    @Override
    public String getName() {
        return attrName;
    }

    @Override
    public Void accept(MethodAttributeVisitor visitor) {
        visitor.visitParameterAnnotation(this);
        return null;
    }

    public List<List<AnnotationData>> getParameterAnnotations() {
        return annotations;
    }

    public int numParameters() {
        return annotations.size();
    }

    public boolean isRuntimeVisible() {
        return runtimeVisible;
    }


}
