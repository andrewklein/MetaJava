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
 * Holds data for a single reference type entry in a class' constant pool
 */
public final class ConstantPoolEntry<T, U> {
    private byte tag;

    T firstData;
    U secondData;

    public ConstantPoolEntry(byte tag, T data) {
        this.tag = tag;
        this.firstData = data;
    }

    public ConstantPoolEntry(byte tag, T firstData, U secondData) {
        this.tag = tag;
        this.firstData = firstData;
        this.secondData = secondData;
    }


    public byte getTag() {
        return tag;
    }

    public T getFirst() {
        return firstData;
    }

    public U getSecond() {
        return secondData;
    }
}
