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

import au.aklein.metajava.exception.ClassPathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Obtain a list of paths to .class files in directories and JAR files
 */
public final class PathProvider {
    private final String includeFilter;
    private List<Path> filePaths;

    private PathProvider(String packageName) {
        this.includeFilter = packageName;
        this.filePaths = new ArrayList<>();
    }

    /**
     * Return a list of paths of the .class files to be searched
     * @return a List of file paths
     */
    public List<Path> getPathList()  {
        return filePaths;
    }

    public static PathProvider newPathProvider(String packageName) throws IOException {
        PathProvider newProvider = new PathProvider(packageName);
        newProvider.filePaths.addAll(newProvider.generatePaths(newProvider.getClassPaths()));
        return newProvider;
    }

    private List<Path> handleJAR(Path path) throws IOException {
        List<Path> rootPaths = new ArrayList<>();
        try(FileSystem fs = FileSystems.newFileSystem(path, Thread.currentThread().getContextClassLoader())) {


            fs.getRootDirectories().forEach(rootPaths::add);
            return generatePaths(rootPaths);
        } catch (IOException e) {
            throw new ClassPathException("Unable to access JAR at: "+path);
        }
    }

    private boolean matchPath(Path path) {
        return path.toString().contains(includeFilter.replace(".", "/")) ||
               path.toString().contains(includeFilter.replace(".", "\\"));
    }

    private List<Path> generatePaths(List<Path> rootPaths) throws IOException {
        LinkedList<Path> directories = new LinkedList<>();
        directories.addAll(rootPaths);
        List<Path> paths = new ArrayList<>();

        DirectoryStream.Filter<Path> filter = entry -> entry.toString().contains(includeFilter.replace(".", "/")) ||
                entry.toString().contains(includeFilter.replace(".", "\\")) ||
                Files.isDirectory(entry);

        while(!directories.isEmpty()) {
            Path path = directories.pop();
            if(Files.isDirectory(path)) {
                try (DirectoryStream<Path> directory = Files.newDirectoryStream(path, filter)) {
                    directory.forEach(directories::add );
                } catch (IOException e) {
                    throw new ClassPathException("Unable to enumerate directory: "+path);
                }

            } else if(path.toString().endsWith(".jar")) {
                paths.addAll(handleJAR(path));
            } else {
                if(matchPath(path)) {
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    /**
     * Return a list of Classpaths
     * @return A list containing each path in the systems CLASSPATH
     */
    private List<Path> getClassPaths() {
        List<Path> classPathList = new ArrayList<Path>();
        String classPath = System.getProperty("java.class.path");
        for(String pathToken : classPath.split(File.pathSeparator)) {
            if(!pathToken.contains("jdk1")) classPathList.add(Paths.get(pathToken));
        }

        return classPathList;

    }


}
