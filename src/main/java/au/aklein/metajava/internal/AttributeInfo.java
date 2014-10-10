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
 *
 * Attribute, used to store data relating to {@link ClassFileReader},
 * {@link au.aklein.metajava.internal.FieldMethodInfo} and {@link au.aklein.metajava.internal.CodeAttribute} structures.
 *
 * @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7">Class File Specification -
 * Attributes</a>
 */
public interface AttributeInfo<VisitorType extends AttributeVisitor, ReturnType> {

    public String getName();

    public ReturnType accept(VisitorType visitor);

}

