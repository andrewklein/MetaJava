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

import au.aklein.metajava.internal.FieldMethodInfo;
import au.aklein.metajava.internal.MethodAttributeVisitor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An element type representing a method contained inside a class.
 *
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.6">Class File Specification,
 * 4.6 - Methods</a>
 */
public final class MethodElement implements Element {
    private final List<AnnotationElement> annotations;
    private final List<List<AnnotationElement>> parameterAnnotations;
    private List<ParameterElement> parameters;
    private final String signature;
    private final String name;
    private final short accessFlags;
    private final ClassElement declaringClass;

    private MethodElement(MethodElementBuilder builder, ClassElement declaringClass) {
        this.annotations = builder.annotations;
        this.parameterAnnotations = builder.parameterAnnotations;
        this.name = builder.name;
        this.signature = builder.signature;
        this.accessFlags = builder.accessFlags;
        this.declaringClass = declaringClass;
    }

    public MethodElement(String methodName, String signature) {
        this.annotations = new ArrayList<>();
        this.parameterAnnotations = new ArrayList<>();
        this.name = methodName;
        this.signature = signature;
        this.accessFlags = 0;
        this.declaringClass = null;
    }

    /**
     * Return a list of the current method's parameters
     * @return - A list of {@link au.aklein.metajava.ParameterElement} objects.
     */
    public List<ParameterElement> getParameters() {
        if(parameters==null) parameters = SignatureParser.getParameters(signature, this);
        return parameters;
    }

    /**
     * Return the method signature as represented in the class file.
     * @return - The method signature as a String
     *
     * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3.3">Class File
     * Specification, 4.3.3 - Method Descriptors</a>
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Returns the {@link au.aklein.metajava.ClassElement} representing the type which has declared this method.
     * @return - A {@link au.aklein.metajava.ClassElement} object of the declaring type.
     */
    public ClassElement getDeclaringClass() {
        return declaringClass;
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
            case PARAMETER:
                for(ParameterElement parameterElement : getParameters()) {
                    if(element.equals(parameterElement)) return true;
                }
                break;
            default: return false;
        }
        return false;
    }

    @Override
    public ElementType type() {
        return ElementType.METHOD;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> List<T> getAssociated(ElementType returnType) {
        switch (returnType) {
            case TYPE: return new ArrayList<T>(){{ add((T) declaringClass);}};
            case PARAMETER: return (List<T>) parameters;
            default:
                final List<T> elements = new ArrayList<>();
                elements.add((T) this);
                return elements;
        }
    }

    @Override
    public int hashCode() {
        return 31*name.hashCode()*signature.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if(!(other instanceof MethodElement)) return false;
        return name.equals(((MethodElement) other).getName()) &&
               signature.equals(((MethodElement) other).getSignature());
    }

    public MethodHandle getMethodHandle(ClassLoader classLoader) throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        MethodType type = MethodType.fromMethodDescriptorString(signature, classLoader);
        if(this.isStatic()) {
            return MethodHandles.lookup().findStatic(declaringClass.getClassType(classLoader), name, type);
        } else {
            return MethodHandles.lookup().findVirtual(declaringClass.getClassType(classLoader), name, type);
        }
    }



    /**
     * Determines if the current method is a constructor for its declaring type.
     * @return - True if the class is a constructor, otherwise false.
     */
    public boolean isConstructor() {
        return name.equals("<init>");
    }

    /**
     * Determines if the method is scoped as public.
     * @return True if the method element is public, otherwise false.
     */
    public boolean isPublic() {
        return (accessFlags & 0x0001) != 0;
    }

    /**
     * Determines if the method is scoped as private.
     * @return True if the method element is private, otherwise false.
     */
    public boolean isPrivate() {
        return (accessFlags & 0x0002) != 0;
    }

    /**
     * Determines if the method is scoped as protected.
     * @return True if the method element is protected, otherwise false.
     */
    public boolean isProtected() {
        return (accessFlags & 0x0004) != 0;
    }

    /**
     * Determines if the method is a static method.
     * @return True if the method element is static, otherwise false.
     */
    public boolean isStatic() {
        return (accessFlags & 0x0008) != 0;
    }

    /**
     * Determines if the method has been declared final.
     * @return True if the method element is final, otherwise false.
     */
    public boolean isFinal() {
        return (accessFlags & 0x0010) != 0;
    }

    /**
     * Determines if the method is synchronized.
     * @return True if the method element is synchronized, otherwise false.
     */
    public boolean isSynchronized() {
        return (accessFlags & 0x0020) != 0;
    }

    /**
     * Determines if the method is a bridge method, generated by the compiler.
     * @return True if the method element is a bridge method, otherwise false.
     */
    public boolean isBridge() {
        return (accessFlags & 0x0040) != 0;
    }

    /**
     * Determines if the method has a variable number of parameters.
     * @return True if the method element is has a varargs parameter, otherwise false.
     */
    public boolean isVarArgs() {
        return (accessFlags & 0x0080) != 0;
    }

    /**
     * Determines if the method is a native method, implemented in a language other than Java.
     * @return True if the method element is native, otherwise false.
     */
    public boolean isNative() {
        return (accessFlags & 0x0100) != 0;
    }

    /**
     * Determines if the method is an abstract method.
     * @return True if the method element is abstract, otherwise false.
     */
    public boolean isAbstract() {
        return (accessFlags & 0x0400) != 0;
    }

    /**
     * Determines if the method is a synthetic method, not present in the source code.
     * @return True if the method element is synthetic, otherwise false.
     */
    public boolean isSynthetic() {
        return (accessFlags & 0x1000) != 0;
    }

    /**
     * Builder class ensuring the MethodElement class is initialised with a fully resolved declaring class
     */
     static final class MethodElementBuilder {
        private final List<AnnotationElement> annotations;
        private final List<List<AnnotationElement>> parameterAnnotations;
        private final String signature;
        private final String name;
        private final short accessFlags;

        public MethodElementBuilder(FieldMethodInfo fieldMethodInfo,
                                    MethodAttributeVisitor methodAttributes) {
            this.annotations = methodAttributes.getAnnotations();
            this.parameterAnnotations = methodAttributes.getParameterAnnotations();
            this.name = fieldMethodInfo.getName();
            this.signature = fieldMethodInfo.getDesc();
            this.accessFlags = fieldMethodInfo.getAccessFlags();
        }

        public MethodElement build(ClassElement declaringClass) {
            return new MethodElement(this, declaringClass);
        }
    }



    /**
     *  Helper inner class to parse method signatures and generate appropriate {@link au.aklein.metajava
     *  .ParameterElement} objects
     */
    private static final class SignatureParser {
        private final char[] signature;
        private final int parameterEnd;

        private SignatureParser(String signature) {
            this.signature = signature.toCharArray();
            this.parameterEnd = parameterLength(this.signature);
        }

        public static List<ParameterElement> getReturnValue(String sig) {
            return new SignatureParser(sig).parseReturnValue();
        }

        public static List<ParameterElement> getParameters(String sig, MethodElement declaringMethod) {
            return new SignatureParser(sig).parseParameters(declaringMethod);
        }

        private List<ParameterElement> parseReturnValue() {
            char[] returnDesc = Arrays.copyOfRange(signature, parameterEnd, signature.length);
            return parse(returnDesc, null);
        }

        private List<ParameterElement> parseParameters(MethodElement declaringMethod) {
            char[] parameters = Arrays.copyOfRange(signature, 1, parameterEnd);
            return parse(parameters, declaringMethod);
        }

        private List<ParameterElement> parse(char[] descriptors, MethodElement declaringMethod) {
            List<List<AnnotationElement>> parameterAnnotations = declaringMethod.parameterAnnotations;
            List<ParameterElement> signatureObjects = new ArrayList<>();
            int index=0;
            int param=0;
            while(index < descriptors.length) {
                Descriptor parameter = Descriptor.newDescriptor(descriptors, index);

                List<AnnotationElement> annotations = new ArrayList<>();
                if(parameterAnnotations.size()!=0) {
                    annotations = parameterAnnotations.get(param);
                }
                signatureObjects.add(new ParameterElement(parameter.getType(), parameter.getDimensions(),
                        annotations, declaringMethod));
                index = parameter.getIndex();
                param++;
            }
            return signatureObjects;
        }


        private int parameterLength(char[] signature) {
            int parameterEnd = 0;
            while(signature[parameterEnd] != ')') parameterEnd++;
            return parameterEnd;
        }

        /**
         * An individual descriptor element (Paramater Descriptor/Return Descriptor)
         */
        private static final class Descriptor {
            private int dimensions;
            private String type;
            private final char[] desc;
            private int index;

            private Descriptor(char[] desc, int index) {
                this.desc = desc;
                this.index = index;
            }

            public static Descriptor newDescriptor(char[] desc, int index) {
                Descriptor descriptor = new Descriptor(desc, index);
                descriptor.parse();
                return descriptor;
            }

            public String getType() {
                return type;
            }

            public int getDimensions() {
                return dimensions;
            }

            public int getIndex() {
                return index;
            }

            private void parse() {
                type = parseBaseType(0);
            }


            private String parseBaseType(int dim) {
                dimensions = dim;
                switch(desc[index]) {
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'F':
                    case 'I':
                    case 'J':
                    case 'S':
                    case 'Z':
                    case 'V':
                        String type = String.valueOf(desc[index]);
                        index++;
                        return type;
                    case 'L': return parseObjectType();
                    case '[': return parseArrayType(dim+1);
                    default: return null;
                }
            }

            private String parseArrayType(int dim) {
                index++;
                switch(desc[index]) {
                    case '[': return parseArrayType(dim+1);
                    default: return parseBaseType(dim);
                }
            }

            private String parseObjectType() {
                int classIndex = index;
                while (desc[index] != ';') index++;
                index++;
                return String.copyValueOf(Arrays.copyOfRange(desc, classIndex, index));
            }
        }
    }
}
