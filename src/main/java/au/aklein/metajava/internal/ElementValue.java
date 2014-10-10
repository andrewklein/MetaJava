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
 *  A representation of the element_value structure - used to describe element values in annotation attributes.
 *  @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.16.1">Class File
 *  Specification - element_value structure</a>
 */
public final class ElementValue {
    private final byte tag;

    //Value union - used sections with depend on the tag
    private final Constant constValue;

    //Enum element
    private final String constType;
    private final String constName;

    //Class element
    private final String classInfo;

    //Nested annotation element
    private final AnnotationData annotationValue;

    //Array element
    private final List<ElementValue> elementValues;


    //Tag is B, C, D, F, I, J, S, Z or s
    public ElementValue(byte tag, Constant constValue) {
        this.tag = tag;
        this.constValue = constValue;
        this.constName = null;
        this.constType = null;
        this.classInfo = null;
        this.annotationValue = null;
        this.elementValues = null;
    }

    //Tag is enum
    public ElementValue(byte tag, String constType, String constName) {
        this.tag = tag;
        this.constValue = null;
        this.constType = constType;
        this.constName = constName;
        this.classInfo = null;
        this.annotationValue = null;
        this.elementValues = null;
    }

    //Tag is class
    public ElementValue(byte tag, String classInfo) {
        this.tag = tag;
        this.constValue = null;
        this.constType = null;
        this.constName = null;
        this.classInfo = classInfo;
        this.annotationValue = null;
        this.elementValues = null;
    }


    public ElementValue(byte tag, AnnotationData annotationValue) {
        this.tag = tag;
        this.constValue = null;
        this.constType = null;
        this.constName = null;
        this.classInfo = null;
        this.annotationValue = annotationValue;
        this.elementValues = null;
    }

    public ElementValue(byte tag, List<ElementValue> elementValues) {
        this.tag = tag;
        this.constValue = null;
        this.constType = null;
        this.constName = null;
        this.classInfo = null;
        this.annotationValue = null;
        this.elementValues = elementValues;
    }

    public boolean isPrimative() {
        return tag == 'B' || tag == 'C' || tag == 'D' || tag == 'F' ||
                tag == 'I' || tag == 'J' || tag == 'S' || tag == 'Z' ||
                tag == 's';
    }



    public boolean isClass() {
        return tag == 'c';
    }

    public boolean isEnum() {
        return tag == 'e';
    }

    public boolean isArray() {
        return tag == '[';
    }

    public boolean isAnnotation() {
        return tag == '@';
    }

    public static boolean tagIsPrimative(byte tag) {
        return tag == 'B' || tag == 'C' || tag == 'D' || tag == 'F' ||
                tag == 'I' || tag == 'J' || tag == 'S' || tag == 'Z' ||
                tag == 's';
    }

    public static boolean tagIsClass(byte tag) {
        return tag == 'c';
    }

    public static boolean tagIsEnum(byte tag) {
        return tag == 'e';
    }

    public static boolean tagIsArray(byte tag) {
        return tag == '[';
    }

    public static boolean tagIsAnnotation(byte tag) {
        return tag == '@';
    }

}
