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

import au.aklein.metajava.ClassElement;
import au.aklein.metajava.exception.ClassFileException;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the file IO and parsing of .class files
 */
public final class ClassFileReader implements Closeable, ClassFile {
    private ClassFileBuffer classReader;
    private Path path;
    private ClassFileException lastError;

    private Map<Short, ConstantPoolEntry> constantPool;
    private Map<Short, Short> classes;
    private Map<Short, Constant<?>> literalConstants;

    private ClassData classFileData;

    private ClassFileReader(boolean keepBuffer) {
        this.constantPool = new HashMap<>();
        this.classes = new HashMap<>();
        this.literalConstants = new HashMap<>();
        this.classFileData = new ClassData();
    }

    /**
     * Data structure used to store raw class data.
     */
    public final class ClassData {
        private byte[] rawClass;

        private short accessFlags;
        private String thisClass;
        private String superClass;

        private List<String> interfaces;
        private List<FieldMethodInfo> fields;
        private List<FieldMethodInfo> methods;
        private List<AttributeInfo> attributes;

        public ClassData() {
            this.interfaces = new ArrayList<>();
            this.fields = new ArrayList<>();
            this.methods = new ArrayList<>();
            this.attributes = new ArrayList<>();
        }

        public String getClassName() {
            return thisClass;
        }

        public String getSuperClassName() {
            return superClass;
        }

        public short getAccessFlags() {
            return accessFlags;
        }

        public List<String> getInterfaces() {
            return interfaces;
        }

        public List<FieldMethodInfo> getFields() {
            return fields;
        }

        public List<FieldMethodInfo> getMethods() {
            return methods;
        }

        public List<AttributeInfo> getAttributes() {
            return attributes;
        }

        public boolean hasClassBuffer() {
            return rawClass != null;
        }

        public byte[] getClassByteArray() {
            return rawClass;
        }



    }


    @Override
    public boolean readClassFile(Path path, boolean preserveClassBuffer) {
        try(ClassFileBuffer classBuffer = new ClassFileBuffer(path, false)) {
            this.classReader = classBuffer;
            this.path = path;
            this.scan();
        } catch (IOException e) {
            lastError = new ClassFileException("IOException in class file: "+path+" - "+e.getMessage());
            return false;
        } catch (ClassFileException e) {
            lastError = e;
            return false;
        }
        return true;

    }

    public static ClassFileReader newClassFileReader() {
        return new ClassFileReader(false);
    }


    @Override
    public ClassElement.ClassElementBuilder generateClassElementBuilder() {
        return ClassElement.ClassElementBuilder.beginNewClassElement(path, classFileData);
    }

    @Override
    public ClassFileException getLastError() {
        return lastError;
    }

    private void resolveConstantPool() {
        for(Map.Entry<Short, ConstantPoolEntry> indexedEntry : constantPool.entrySet()) {
            ConstantPoolEntry entry = indexedEntry.getValue();
            Short index = indexedEntry.getKey();

            switch(entry.getTag()) {
                case 7:
                    classes.put(index, (Short) entry.getFirst());
                    break;
                case 8:
                    literalConstants.put(index, new Constant<>(entry.getTag(), resolveConstantPoolString((Short) entry.getFirst(), false)));
                    break;
                case 9:
                case 10:
                case 11:

                default:
                    break;
            }
        }
    }

    private String resolveClass(Short index, boolean allowNull) {
        if(classes.containsKey(index)) {
            if(classes.get(index) != null) return resolveConstantPoolString(classes.get(index), allowNull);
        }
        if(allowNull) return null;
        throw new ClassFileException("Invalid class constant pool entry, "+index+" in file: "+ path);
    }

    private String resolveConstantPoolString(Short index, boolean allowNull) {
        if(literalConstants.containsKey(index)) {
            Constant strConst = literalConstants.get(index);
            if(!strConst.isNumeric()) return (String) strConst.getValue();
        }
        if(allowNull) return null;
        throw new ClassFileException("Invalid constant pool entry, "+index+" in file: "+ path);
    }

    private Constant resolveConstantPoolLiteral(Short index) {
        if(literalConstants.containsKey(index)) {
            return literalConstants.get(index);
        }
        throw new ClassFileException("Invalid constant pool entry, "+index+" in file: "+ path);
    }

    private void scan() throws IOException {

        if(classReader.nextInt() != 0xCAFEBABE) {
            throw new ClassFileException("File: "+ path +", is not a valid .class file");
        }

        short minorVer = classReader.nextShort();
        short majorVer = classReader.nextShort();
        short constantCount = classReader.nextShort();

        //Scan constant pool entries
        for(short i=1; i < constantCount; i++) {

            ConstantPoolEntry entry = readConstantPoolEntry(i);
            if(entry != null) constantPool.put(i, entry);
        }
        resolveConstantPool();
        classFileData.accessFlags = classReader.nextShort();

        classFileData.thisClass = resolveClass(classReader.nextShort(), false);
        classFileData.superClass = resolveClass(classReader.nextShort(), true);

        short interfaceCount = classReader.nextShort();

        //Scan for interfaces implemented
        for(int i=0; i < interfaceCount; i++) {
            classFileData.interfaces.add(resolveClass(classReader.nextShort(), false));
        }
        //Scan for class fields
        short fieldsCount= classReader.nextShort();
        for(int i=0; i < fieldsCount; i++) {
            classFileData.fields.add(readFieldMethodInfoEntry());
        }

        //Scan for methods
        short methodsCount = classReader.nextShort();
        for(int i=0; i < methodsCount; i++) {
            classFileData.methods.add(readFieldMethodInfoEntry());
        }

        //Scan for class attributes
        short attributesCount = classReader.nextShort();
        for(int i=0; i < attributesCount; i++) {
            classFileData.attributes.add(readAttributeInfoEntry());
        }
    }

    private FieldMethodInfo readFieldMethodInfoEntry() throws IOException {
        short accessFlags = classReader.nextShort();


        String fieldName = resolveConstantPoolString(classReader.nextShort(), false);
        String fieldDesc = resolveConstantPoolString(classReader.nextShort(), false);

        short attrCount = classReader.nextShort();
        List<AttributeInfo> attributes = new ArrayList<>();

        for(int i=0; i < attrCount; i++) {
            attributes.add(readAttributeInfoEntry());
        }

        return new FieldMethodInfo(accessFlags, fieldName, fieldDesc,
                attributes);
    }

    private AttributeInfo readAttributeInfoEntry() throws IOException {
        String attrName = resolveConstantPoolString(classReader.nextShort(), false);

        int attrLength = classReader.nextInt();
        switch (attrName) {
            case "Code": return readCodeAttribute(attrName);
            case "InnerClasses": return readInnerClassAttribute(attrName);
            case "Signature": return readSignatureAttribute(attrName);
            case "RuntimeVisibleAnnotations": return readAnnotationsAttribute(attrName, true);
            case "RuntimeInvisibleAnnotations": return readAnnotationsAttribute(attrName, false);
            case "RuntimeVisibleParameterAnnotations": return readParameterAnnotationsAttribute(attrName, true);
            case "RuntimeInvisibleParameterAnnotations": return readParameterAnnotationsAttribute(attrName, false);
            default :
                for(int i=0; i < attrLength; i++) {
                    classReader.nextByte();
                }
                return new DefaultAttribute(attrName);
        }
    }

    private AnnotationAttribute readAnnotationsAttribute(String attrName,
                                                         boolean runtimeVisible) throws IOException {
        short annotationCount = classReader.nextShort();
        List<AnnotationData> annotations = new ArrayList<>();

        for(int i=0; i < annotationCount; i++) {
            annotations.add(readAnnotation());
        }

        return new AnnotationAttribute(attrName, annotations, runtimeVisible);
    }

    private ParameterAnnotationAttribute readParameterAnnotationsAttribute(String attrName,
                                                         boolean runtimeVisible) throws IOException {

        byte parameterCount = classReader.nextByte();
        List<List<AnnotationData>> parameterAnnotations = new ArrayList<>();

        for(int i=0; i < parameterCount; i++) {
            short annotationCount = classReader.nextShort();
            List<AnnotationData> annotations = new ArrayList<>();

            for(int y=0; y < annotationCount; y++) {
                annotations.add(readAnnotation());
            }
            parameterAnnotations.add(annotations);
        }

        return new ParameterAnnotationAttribute(attrName, parameterAnnotations, runtimeVisible);
    }

    private AnnotationData readAnnotation() throws IOException {
        String annotationType = resolveConstantPoolString(classReader.nextShort(), false);

        short elementValueCount = classReader.nextShort();
        Map<String, ElementValue> elementValuePairs = new HashMap<>();
        for(int i=0; i < elementValueCount; i++) {
            String elementName = resolveConstantPoolString(classReader.nextShort(), false);
            elementValuePairs.put(elementName, readElementValue());
        }

        return new AnnotationData(annotationType, elementValuePairs);
    }

    private ElementValue readElementValue() throws IOException {
        byte tag = classReader.nextByte();
        if(ElementValue.tagIsPrimative(tag)) {
            Constant primitiveType = resolveConstantPoolLiteral(classReader.nextShort());
            return new ElementValue(tag, primitiveType);

        } else if(ElementValue.tagIsEnum(tag)) {
            String enumType = resolveConstantPoolString(classReader.nextShort(), false);
            String enumName = resolveConstantPoolString(classReader.nextShort(), false);

            return new ElementValue(tag, enumType, enumName);

        } else if(ElementValue.tagIsClass(tag)) {
            String className = resolveConstantPoolString(classReader.nextShort(), false);

            return new ElementValue(tag, className);

        } else if(ElementValue.tagIsAnnotation(tag)) {
            AnnotationData annotation = readAnnotation();
            return new ElementValue(tag, annotation);
        } else if(ElementValue.tagIsArray(tag)) {
            short arrayElementCount = classReader.nextShort();
            List<ElementValue> elementValues = new ArrayList<>();
            for(int i=0; i < arrayElementCount; i++) {
                elementValues.add(readElementValue());
            }
            return new ElementValue(tag, elementValues);
        }

        throw new ClassFileException("Invalid element value tag in file "+ path);
    }


    private CodeAttribute readCodeAttribute(String attrName) throws IOException {
        short maxStack = classReader.nextShort();
        short maxLocals = classReader.nextShort();
        int codeLength = classReader.nextInt();

        for(int i=0; i < codeLength; i++) {
            classReader.nextByte();
        }


        short exceptionTableLength = classReader.nextShort();

        for(int i=0; i < exceptionTableLength; i++) {
            classReader.nextShort();
            classReader.nextShort();
            classReader.nextShort();
            classReader.nextShort();
        }

        short attrCount = classReader.nextShort();

        List<AttributeInfo> attributes = new ArrayList<>();

        for(int i=0; i < attrCount; i++) {
            attributes.add(readAttributeInfoEntry());
        }

        return new CodeAttribute(attrName, maxStack, maxLocals, null, null, attributes);
    }

    private InnerClassAttribute readInnerClassAttribute(String attrName) throws IOException {
        short classCount = classReader.nextShort();
        List<InnerClassData> innerClasses = new ArrayList<>();
        for(int i=0; i < classCount; i++) {

            String innerClassIndex = resolveClass(classReader.nextShort(), false);
            String outerClassIndex = resolveClass(classReader.nextShort(), true);
            String innerClassName = resolveConstantPoolString(classReader.nextShort(), true);

            short innerClassAccessFlags = classReader.nextShort();

            innerClasses.add(new InnerClassData(innerClassIndex,
                    outerClassIndex, innerClassName, innerClassAccessFlags));
        }

        return new InnerClassAttribute(attrName, innerClasses);
    }

    private SignatureAttribute readSignatureAttribute(String attrName) throws IOException {
        return new SignatureAttribute(attrName, resolveConstantPoolString(classReader.nextShort(), false));
    }

    private ConstantPoolEntry readConstantPoolEntry(short index) throws IOException {
        byte tag = classReader.nextByte();
        switch (tag) {
            case 1:
            //UTF-8 string prefixed by u2 indicating string length
                short strLength = classReader.nextShort();

                byte[] strBytes = new byte[strLength];
                for(int i=0; i < strLength; i++) {
                    strBytes[i] = classReader.nextByte();
                }
                String constantStr;
                try {
                    constantStr = new String(strBytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new ClassFileException("Constant pool string encoding is invalid: "+ path +" - "+e);
                }
                literalConstants.put(index, new Constant<>(tag, constantStr));
                return null;

            case 3:
            //u4 Integer
                //return new NumericConstant<>(classReader.nextInt(), tag);
                literalConstants.put(index, new Constant<>(tag, classReader.nextInt()));
                return null;
            case 4:
            //u4 Float
                //return new NumericConstant<>(classReader.nextFloat(), tag);
                literalConstants.put(index, new Constant<>(tag, classReader.nextFloat()));
                return null;
            case 5:
            //2*u4 Long
                //return new NumericConstant<>(classReader.nextLong(), tag);
                literalConstants.put(index, new Constant<>(tag, classReader.nextLong()));
                return null;
            case 6:
            //2*u4 Double
                //return new NumericConstant<>(classReader.nextDouble(), tag);
                literalConstants.put(index, new Constant<>(tag, classReader.nextDouble()));
                return null;
            case 7:
            //Class reference - u2 index pointing to a string with the fully qualified class name
                //return new ClassReference(classReader.nextShort());
                return new ConstantPoolEntry<>(tag, classReader.nextShort());
            case 8:
            //String reference - u2 index point to a string
                //return new StringReference(classReader.nextShort());
                return new ConstantPoolEntry<>(tag, classReader.nextShort());
            case 9:
            //Field reference - 2*u2 index pointing to class ref/name&data descriptor
                short fieldClsRef = classReader.nextShort();
                short fieldDesc = classReader.nextShort();
                //return new ClassElementReference(fieldClsRef, fieldDesc, tag);
                return new ConstantPoolEntry<>(tag, fieldClsRef, fieldDesc);
            case 10:
            //Method reference - 2*u2 index pointing to class ref/name&data descriptor
                short methodClsRef = classReader.nextShort();
                short methodDesc = classReader.nextShort();
                //return new ClassElementReference(methodClsRef, methodDesc, tag);
                return new ConstantPoolEntry<>(tag, methodClsRef, methodDesc);
            case 11:
            //Interface Method reference - 2*u2 index pointing to class ref/name&data descriptor
                short intfMethodClsRef = classReader.nextShort();
                short intfMethodDesc = classReader.nextShort();
                //return new ClassElementReference(intfMethodClsRef, intfMethodDesc, tag);
                return new ConstantPoolEntry<>(tag, intfMethodClsRef, intfMethodDesc);
            case 12:
            //Name and type descriptor - 2*u2 indexes pointing to UTF-8 strings of the name/data
                short nameDesc = classReader.nextShort();
                short typeDesc = classReader.nextShort();
                //return new NameTypeReference(nameDesc, typeDesc);
                return new ConstantPoolEntry<>(tag, nameDesc, typeDesc);
            case 15:
            //Method handle - u1 indicating the type of reference, and a u2 index pointing to a reference
                byte refType = classReader.nextByte();
                short refIndex = classReader.nextShort();
                return new ConstantPoolEntry<>(tag, refType, refIndex);
            case 16:
            //Method type - u2 index to string representing the method descriptor

                return new ConstantPoolEntry<>(tag, classReader.nextShort());
            case 18:
            //INVOKEDYNAMIC - u2 index to bootstrap_methods array, and u2 index to name and type ref
                short bootstrapMethodIndex = classReader.nextShort();
                short nameTypeIndex = classReader.nextShort();
                return new ConstantPoolEntry<>(tag, bootstrapMethodIndex, nameTypeIndex);
            default:
                return null;
        }
    }

    @Override
    public void close() throws IOException {
        classReader.close();
    }


    /**
     *  Helper File IO class for reading class files
     */
    private static final class ClassFileBuffer implements Closeable {
        private final Path classPath;
        private final SeekableByteChannel classChannel;
        private final ByteBuffer buffer;
        private final boolean keepBuffer;

        public ClassFileBuffer(Path classPath, boolean keepBuffer) throws IOException {
            this.classPath = classPath;
            this.classChannel = Files.newByteChannel(this.classPath);
            this.keepBuffer = keepBuffer;
            if(keepBuffer) {
                this.buffer = ByteBuffer.allocate((int) Files.size(this.classPath));
                classChannel.read(buffer);
                classChannel.close();
            } else {
                this.buffer = ByteBuffer.allocate(1024);
            }

        }

        public boolean read() throws IOException {

            if(!buffer.hasRemaining()) buffer.clear();
            else if(buffer.position() != 0 ){
                ByteBuffer temp = buffer.slice();
                buffer.clear();
                buffer.put(temp);
            }

            int num = classChannel.read(buffer);
            buffer.rewind();
            return num > 0;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public int nextInt() throws IOException  {

            if((buffer.position() > buffer.limit()-4 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.getInt();

        }

        public short nextShort() throws IOException  {
            if((buffer.position() > buffer.limit()-2 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.getShort();
        }

        public Double nextDouble() throws IOException  {
            if((buffer.position() > buffer.limit()-8 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.getDouble();
        }

        public Long nextLong() throws IOException  {
            if((buffer.position() > buffer.limit()-8 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.getLong();
        }

        public Float nextFloat() throws IOException  {
            if((buffer.position() > buffer.limit()-4 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.getFloat();
        }

        public byte nextByte() throws IOException  {
            if((buffer.position() > buffer.limit()-1 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.get();
        }

        public char nextChar() throws IOException  {
            if((buffer.position() > buffer.limit()-1 || buffer.position() == 0) && !keepBuffer) {
                read();
            }
            return buffer.getChar();
        }

        public void skipBytes(int n) throws IOException {
            for(int i=0; i < n; i++) {
                nextByte();
            }
        }

        @Override
        public void close() throws IOException {
            classChannel.close();
        }
    }


}
