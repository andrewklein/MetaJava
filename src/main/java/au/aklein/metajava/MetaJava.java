
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

package au.aklein.metajava;

import au.aklein.metajava.exception.ClassPathException;
import au.aklein.metajava.internal.ClassFileReader;
import au.aklein.metajava.internal.PathProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class MetaJava {
    private String packageFilter;
    private boolean ignoreErrors;
    private List<ClassElement> classes;

    private MetaJava(String packageFilter, boolean ignoreErrors) {
        this.packageFilter = packageFilter;
        this.ignoreErrors = ignoreErrors;
    }

    private MetaJava scan() {
        try {
            PathProvider pathProvider = PathProvider.newPathProvider(packageFilter);
            this.classes = generateClassElements(pathProvider.getPathList(), !ignoreErrors, false);
        } catch (IOException e) {
            throw new ClassPathException("Unable to enumerate class path");
        }
        return this;
    }

    /**
     * Scan the specified package, ignoring any errors.
     *
     * @param packageFilter - the package to scan
     * @return a new MetaJava instance
     *
     */
    public static MetaJava scanClassPath(String packageFilter) {
        return scanClassPath(packageFilter, true);
    }

    /**
     * Main entry point for the MetaJava library. Scans a specified package based on the current system CLASSPATH and
     * returns a new MetaJava instance.
     * @param packageFilter - the package to scan
     * @param ignoreErrors - if set to false, any errors encountered while parsing class files will throw a {@link
     * au.aklein.metajava.exception.ClassFileException}
     * @return a new MetaJava instance
     */
    public static MetaJava scanClassPath(String packageFilter, boolean ignoreErrors) {
        return new MetaJava(packageFilter, ignoreErrors).scan();
    }

    /**
     * Specifies what sort of elements will be tested when scanning class files.
     * @param elementType - The {@link au.aklein.metajava.ElementType}
     * @return - A new {@link au.aklein.metajava.MetaJava.MetaQuery} instance.
     */
    public MetaQuery where(ElementType elementType) {
        return new MetaQuery(classes, elementType);
    }

    private static List<ClassElement> generateClassElements(List<Path> filePaths,
                                                            boolean showError,
                                                            boolean preserveClassBuffer) {
        List<ClassElement> classes = new ArrayList<>();
        List<ClassElement> innerClasses = new ArrayList<>();
        List<ClassElement.ClassElementBuilder> builders = new ArrayList<>();

        for(Path classPath : filePaths) {
            ClassFileReader classFile = ClassFileReader.newClassFileReader();
            if(classFile.readClassFile(classPath, preserveClassBuffer)) {
                ClassElement.ClassElementBuilder resultElement = classFile.generateClassElementBuilder();
                if(resultElement.isInner()) innerClasses.add(resultElement.construct());
                else builders.add(resultElement);
            } else if(showError) {
                throw classFile.getLastError();
            }
        }

        for(ClassElement.ClassElementBuilder outer: builders) {
            innerClasses.stream().filter(outer::hasInner).forEach(outer::addInner);
        }
        builders.forEach(builder -> { classes.add(builder.construct()); });

        return classes;
    }

    /**
     * Helper class for constructing search queries.
     */
    public static final class MetaQuery {
        private final ElementType targetType;
        private final List<ClassElement> classes;
        private MetaQuery(List<ClassElement> classes, ElementType type) {
            this.targetType = type;
            this.classes = classes;
        }

        /**
         * Tests if particular Elements are contained within chosen target method.
         *
         * @param searchElements - The elements that will be
         * @param <MatchedElement> - The type of {@link au.aklein.metajava.Element} the target must contain.
         * @return A list of Elements
         */
        @SuppressWarnings("unchecked")
        public <MatchedElement extends Element> MetaResult has(Element... searchElements) {

            List<MatchedElement> found = new ArrayList<>();
            for(ClassElement classElement : classes) {
                switch(targetType) {
                    case TYPE:
                        if(testElement(classElement, searchElements)) found.add((MatchedElement) classElement);
                        break;
                    case METHOD:
                        classElement.getMethods().forEach(
                                methodElement -> {
                                    if(testElement(methodElement, searchElements)) found.add((MatchedElement) methodElement);
                                }
                        );
                        break;
                    default: break;
                }
            }

            return new MetaResult(this, (List<Element>) found);
        }

        private boolean testElement(Element testElement, Element... searchElements) {
            for(Element searchElement : searchElements) {
                if(testElement.has(searchElement)) return true;
            }
            return false;
        }


    }


    /**
     * Helper class for manipulating results.
     */
    public static final class MetaResult {
        private final List<Element> searchElements;

        private MetaResult(MetaQuery query, List<Element> elements) {

            this.searchElements = elements;
        }

        /**
         * Returns a List of {@link au.aklein.metajava.Element} objects of a particular type,
         * based on their relationship to Elements found.
         *
         * @param returnType - The type of element being returned
         * @param <R> - The type of {@link au.aklein.metajava.Element} being returned.
         * @return A list of Elements
         */
        @SuppressWarnings("unchecked")
        public <R extends Element> List<R> get(ElementType returnType) {
            Set<R> results = new HashSet<>();
            for(Element element : searchElements) {
                results.addAll(element.getAssociated(returnType));
            }
            return new ArrayList<R>(){{addAll(results);}};
        }

    }

}
