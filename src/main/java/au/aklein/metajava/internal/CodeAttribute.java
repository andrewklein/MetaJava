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
 * Holds the attribute information containing the instructions and auxiliary information for a single method
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.3">Class File Specification -
 * Code</a>
 */
public final class CodeAttribute implements AttributeInfo<MethodAttributeVisitor, Void> {
    private final String attrName;
    private final short maxStack;
    private final short maxLocals;
    private final byte[] code;
    private final byte[] exceptionTable;
    private final List<AttributeInfo> attributes;

    public CodeAttribute(String attrName, short maxStack,
                          short maxLocals,
                          byte[] code,
                          byte[] exceptionTable,
                          List<AttributeInfo> attributes) {
        this.attrName = attrName;
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
        this.code = code;
        this.exceptionTable = exceptionTable;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return attrName;
    }

    @Override
    public Void accept(MethodAttributeVisitor visitor) {
        visitor.visitCodeAttribute(this);
        return null;
    }

    public List<AttributeInfo> getAttributes() {
        return attributes;
    }

}
