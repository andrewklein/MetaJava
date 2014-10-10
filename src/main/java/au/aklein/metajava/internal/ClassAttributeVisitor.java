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


import java.util.ArrayList;
import java.util.List;

/**
 * Process a class attribute to obtain relevant information
 */
public final class ClassAttributeVisitor extends AttributeVisitor {

    private boolean inner;
    private boolean anonymous;
    private String outerClass;
    private String thisClass;

    private List<String> innerClassNames;

    public ClassAttributeVisitor(String thisClass) {
        this.innerClassNames = new ArrayList<>();
        this.thisClass = thisClass;
        this.inner = false;
        this.outerClass = null;
        this.anonymous = false;
    }

    public boolean isInner() {
        return inner;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public String getOuterClass() {
        return outerClass;
    }


    public List<String> getInnerClassNames() {
        return innerClassNames;
    }

    public void visitInnerClasses(InnerClassAttribute innerClassAttribute) {
        for(InnerClassData innerClass : innerClassAttribute.getInnerClasses()) {
            if(innerClass.getInnerClassInfo().equals(thisClass)) {
                inner = true;
                anonymous = innerClass.isAnonymous();
                if(innerClass.getOuterClassInfo()!=null) outerClass = innerClass.getOuterClassInfo();

            } else {
                innerClassNames.add(innerClass.getInnerClassInfo());
            }
        }
    }



}
