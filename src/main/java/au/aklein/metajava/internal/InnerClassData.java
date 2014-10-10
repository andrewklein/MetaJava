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
 * Holds the attribute information of a single inner class.
 * {@link au.aklein.metajava.internal.InnerClassAttribute}
 *  @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.6">Class File Specification
 *  - InnerClasses Attribute</a>
 */
public final class InnerClassData {
    private final String innerClassInfo;
    private final String outerClassInfo;
    private final String innerName; //Null if class is anonymous
    private short innerAccessFlags;

    public InnerClassData(String innerClassInfo, String outerClassInfo,
                          String innerName, short innerAccessFlags) {
        this.innerClassInfo = innerClassInfo;
        this.outerClassInfo = outerClassInfo;
        this.innerName = innerName;
        this.innerAccessFlags = innerAccessFlags;
    }

    public String getInnerClassInfo() {
        return innerClassInfo;
    }

    public String getOuterClassInfo() {
        return outerClassInfo;
    }

    public boolean isAnonymous() {
        return innerName == null;
    }

    public boolean isInnerClass() {
        return outerClassInfo != null;
    }
}
