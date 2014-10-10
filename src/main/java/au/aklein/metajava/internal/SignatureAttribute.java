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

/**
 * Holds the attribute information for "generic signature information for any class, interface,
 * constructor or member whose generic signature in the Java programming language would include references to type
 * variables or parameterized types."
 *
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9">Class File Specification -
 * Signature</a>
 */
public final class SignatureAttribute implements AttributeInfo<AttributeVisitor,Void> {
    private final String attrName;
    private final String signature;


    public SignatureAttribute(String attrName, String signature) {
        this.attrName = attrName;
        this.signature = signature;
    }

    @Override
    public String getName() {
        return attrName;
    }

    @Override
    public Void accept(AttributeVisitor visitor) {
        visitor.visitSignature(this);
        return null;
    }

    public String getSignature() {
        return signature;
    }

}
