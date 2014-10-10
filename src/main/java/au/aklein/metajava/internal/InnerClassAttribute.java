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
 *  Holds the attribute information containing all of the {@link au.aklein.metajava.internal.InnerClassData} entries
 *  within a class.
 *  @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.6">Class File Specification
 *  - InnerClasses Attribute</a>
 */
public final class InnerClassAttribute implements AttributeInfo<ClassAttributeVisitor, Void> {
    private final String attrName;
    private final List<InnerClassData> innerClasses;

    public InnerClassAttribute(String attrName, List<InnerClassData> innerClasses) {
        this.attrName = attrName;
        this.innerClasses = innerClasses;
    }

    @Override
    public String getName() {
        return attrName;
    }

    @Override
    public Void accept(ClassAttributeVisitor visitor) {
        visitor.visitInnerClasses(this);
        return null;
    }

    public List<InnerClassData> getInnerClasses() {
        return innerClasses;
    }



}

