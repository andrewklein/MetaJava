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
 * Default attribute class, used for attributes which are not otherwise handled.
 */
public final class DefaultAttribute implements AttributeInfo<AttributeVisitor, Void> {
    private final String attrName;

    public DefaultAttribute(String attrName) {
        this.attrName = attrName;
    }

    @Override
    public String getName() {
        return attrName;
    }

    @Override
    public Void accept(AttributeVisitor visitor) {
        visitor.visitAttribute(this);
        return null;
    }


}
