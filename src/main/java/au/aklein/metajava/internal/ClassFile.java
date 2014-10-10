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

import java.nio.file.Path;

/**
 * A service provider interface for classes which read and parse class files.
 */
public interface ClassFile {
    /**
     * Read and parse the given class file, returning if the operation is successful or not.
     *
     * @param path - The {@link java.nio.file.Path} to the class file
     * @param preserveClassBuffer - A boolean value of whether or not to hold on to the byte array of class data. If
     *                            true, resulting ClassElement objects will contain the byte array of their
     *                            corresponding class file.
     * @return true if the file is read without errors, otherwise return false.
     */
    public boolean readClassFile(Path path, boolean preserveClassBuffer);

    /**
     * Generate a {@link au.aklein.metajava.ClassElement.ClassElementBuilder} which may be processed further before
     * construction.
     *
     * @return a {@link au.aklein.metajava.ClassElement.ClassElementBuilder} for the class file read.
     */
    public ClassElement.ClassElementBuilder generateClassElementBuilder();

    /**
     * Returns the latest error that occured during reading, if avaliable.
     * @return the most recent ClassFileException, otherwise null.
     */
    public ClassFileException getLastError();

}
