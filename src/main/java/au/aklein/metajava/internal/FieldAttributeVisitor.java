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
 * Process a field_info attribute to obtain relevant information
 */
public final class FieldAttributeVisitor extends AttributeVisitor {

    private CodeAttributeVisitor methodCode;

    public FieldAttributeVisitor() {
        this.methodCode = new CodeAttributeVisitor();
    }
}
