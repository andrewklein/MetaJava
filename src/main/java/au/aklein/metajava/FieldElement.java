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

import au.aklein.metajava.internal.FieldAttributeVisitor;
import au.aklein.metajava.internal.FieldMethodInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * An object representing a field {@link au.aklein.metajava.Element} from a type or method,
 * inside a class file.
 */
public final class FieldElement implements Element {
    private final List<AnnotationElement> annotations;
    private final String name;
    private final short accessFlags;
    private final ClassElement declaringClass;

    private FieldElement(FieldElementBuilder builder, ClassElement declaringClass) {
        this.annotations = builder.annotations;
        this.name = builder.name;
        this.accessFlags = builder.accessFlags;
        this.declaringClass = declaringClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean has(Element element) {
        switch (element.type()) {
            case ANNOTATION:
                for(AnnotationElement annotationElement : annotations) {
                    if(element.equals(annotationElement)) return true;
                }
                break;
            default: return false;
        }
        return false;
    }

    @Override
    public ElementType type() {
        return ElementType.FIELD;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> List<T> getAssociated(ElementType returnType) {
        switch (returnType) {
            case TYPE: return new ArrayList<T>(){{ add((T) declaringClass);}};
            default:
                final List<T> elements = new ArrayList<>();
                elements.add((T) this);
                return elements;
        }
    }

    @Override
    public int hashCode() {
        return 31*name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if (!(other instanceof FieldElement)) return false;
        return this.name.equals(((FieldElement) other).getName());
    }

    /**
     * Returns the {@link au.aklein.metajava.ClassElement} representing the type which has declared this field.
     * @return - A {@link au.aklein.metajava.ClassElement} object of the declaring type.
     */
    public ClassElement getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Determines if the field is scoped as public.
     * @return True if the field element is public, otherwise false.
     */
    public boolean isPublic() {
        return (accessFlags & 0x0001) != 0;
    }

    /**
     * Determines if the field is scoped as private.
     * @return True if the field element is private, otherwise false.
     */
    public boolean isPrivate() {
        return (accessFlags & 0x0002) != 0;
    }

    /**
     * Determines if the field is scoped as protected.
     * @return True if the field element is protected, otherwise false.
     */
    public boolean isProtected() {
        return (accessFlags & 0x0004) != 0;
    }

    /**
     * Determines if the field is a static method.
     * @return True if the field element is static, otherwise false.
     */
    public boolean isStatic() {
        return (accessFlags & 0x0008) != 0;
    }

    /**
     * Determines if the field has been declared final.
     * @return True if the field element is final, otherwise false.
     */
    public boolean isFinal() {
        return (accessFlags & 0x0010) != 0;
    }

    /**
     * Determines if the field has been declared volatile.
     * @return True if the field element is volatile, otherwise false.
     */
    public boolean isVolatile() {
        return (accessFlags & 0x0040) != 0;
    }

    /**
     * Determines if the field is a synthetic field, not present in the source code.
     * @return True if the field element is synthetic, otherwise false.
     */
    public boolean isSynthetic() {
        return (accessFlags & 0x1000) != 0;
    }

    /**
     * Determines if the field is an element of an enum type.
     * @return True if the field element is an element of an enum, otherwise false.
     */
    public boolean isEnumConstant() {
        return (accessFlags & 0x4000) != 0;
    }

    /**
     * Builder class ensuring the FieldElement class is initialised with a fully resolved declaring class
     */
    static final class FieldElementBuilder {
        private final List<AnnotationElement> annotations;
        private final String name;
        private final short accessFlags;

        public FieldElementBuilder(FieldMethodInfo fieldMethodInfo, FieldAttributeVisitor methodAttributes) {
            this.annotations = methodAttributes.getAnnotations();
            this.name = fieldMethodInfo.getName();
            this.accessFlags = fieldMethodInfo.getAccessFlags();
        }

        public FieldElement build(ClassElement declaringClass) {
            return new FieldElement(this, declaringClass);
        }

    }


}
