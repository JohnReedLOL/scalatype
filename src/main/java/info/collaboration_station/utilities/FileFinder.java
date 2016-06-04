package info.collaboration_station.utilities;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import static java.nio.file.FileVisitOption.*;
import java.util.*;

/**
 * This class helps find files in the file system on both Linux and Windows. It
 * allows applications to specify the file they want to search for and
 * approximately where in the file system they expect to find it without having
 * to know the exact location. Use it to find files somewhere in a particular
 * folder or within a certain sub-folder.
 *
 * @author johnmichaelreed2
 */
public class FileFinder {

    private final static char WINDOWS_CLASSPATH_SEPARATOR = ';';
    private final static char WINDOWS_DIRECTORY_SEPARATOR = '\\';
    private final static char LINUX_CLASSPATH_SEPARATOR = ':';
    private final static char LINUX_DIRECTORY_SEPARATOR = '/';

    public final static String CLASSPATH_SEPARATOR = System.getProperty("path.separator");
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");
    public final static String HOME_DIRECTORY = System.getProperty("user.home");
    public final static String WORKING_DIRECTORY = System.getProperty("user.dir");

    /**
     * How many levels (subdirectories deep) a file search should go before
     * giving up.
     */
    private static int mySearchDepth_ = 6;

    static {
        Tester.check(CLASSPATH_SEPARATOR != null, "Null path separator");
        Tester.check(HOME_DIRECTORY != null, "Null home directory");
        Tester.check(WORKING_DIRECTORY != null, "Null current user directory");
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // It's Windows. Make sure the file seperator is correct.
            Tester.check(FILE_SEPARATOR.equals("" + WINDOWS_DIRECTORY_SEPARATOR));
            Tester.check(CLASSPATH_SEPARATOR.equals("" + WINDOWS_CLASSPATH_SEPARATOR));
        }
        Tester.check(FILE_SEPARATOR.equals(File.separator));
        Tester.check(CLASSPATH_SEPARATOR.equals(File.pathSeparator));
    }

    public static String getParentDirPath(String fileOrDirPath) {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar,
                endsWithSlash ? fileOrDirPath.length() - 2 : fileOrDirPath.length() - 1));
    }

    public static int getMySearchDepth() {
        return mySearchDepth_;
    }

    public static void setMySearchDepth(int aMySearchDepth_) {
        Tester.check(aMySearchDepth_ > 0, "The depth of a file search must be positive.");
        mySearchDepth_ = aMySearchDepth_;
    }

    /**
     * Sample code that finds files that match the specified glob pattern. For
     * more information on what constitutes a glob pattern, see
     * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
     *
     * The file or directories that match the pattern are printed to standard
     * out. The number of matches is also printed.
     *
     * When executing this application, you must put the glob pattern in quotes,
     * so the shell will not expand any wild cards: java Find . -name "*.java"
     */
    private static class Finder
            extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher_;
        public static Path last_file_path_found = null;

        Finder(final String pattern) {
            matcher_ = FileSystems.getDefault()
                    .getPathMatcher("glob:" + pattern);
        }

        // Compares the glob pattern against
        // the file or directory name.
        /**
         *
         * @param file
         * @return true is found, false if not found.
         */
        boolean find(final Path file) {
            final Path name = file.getFileName();
            //Application.printEx(name.toString());
            if (name != null && matcher_.matches(name)) {
                last_file_path_found = file;
                //Application.printEx("match");
                return true;
            }
            //Application.printEx("no match");
            return false;
        }

        /**
         * Sets the last file path found to null and returns its value from
         * before it was set to null.
         *
         * @return The file path obtained via the search.
         */
        Path done() {
            if (last_file_path_found == null) {
                //Application.printEx("File not found");
            } else {
                //Application.printEx("File found");
            }
            final Path to_return = last_file_path_found;
            last_file_path_found = null;
            return to_return;
        }

        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs) {
            if (find(file)) { // if we found the file, terminate
                //Application.printEx("Terminating file search");
                return FileVisitResult.TERMINATE;
            } else { // else continue
                //Application.printEx("Continueing file search");
                return CONTINUE;
            }
        }

        // Invoke the pattern matching
        // method on each directory.
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) {
            if (find(dir)) { // if we found the file, terminate
                //Application.printEx("Terminating directory search");
                return FileVisitResult.TERMINATE;
            } else { // else continue
                //Application.printEx("Continueing directory search");
                return CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                IOException exc) {
            //System.err.println(exc);
            return CONTINUE;
        }
    }

    /**
     * @return null on failure non-null on success.
     */
    public static String tryFindFileWhoseNameBeginsWith(final String file_name, final String searchDirectory) {
        final Path startingDir = Paths.get(searchDirectory); //Paths.get(WORKING_DIRECTORY);
        Finder finder = new Finder(file_name + "*");
        try {
            Files.walkFileTree(startingDir, EnumSet.of(FOLLOW_LINKS), mySearchDepth_, finder);
        } catch (IOException e) {
            // If you can't visit a file, ignore it.
        }
        final Path foundPath = finder.done();
        if (foundPath == null) {
            // failure condition.
            return null;
        } // else success condition
        final Path absolute_path = foundPath.toAbsolutePath();
        Path real_path = null;
        try {
            //Application.printEx(absolute_path.toString() + "  size of link options: " + LinkOption.values().length);
            real_path = absolute_path.toRealPath();
        } catch (IOException e) {
            Tester.killApplication("This isn't supposed to happen because the file was found", e);
        }
        Tester.check(absolute_path != null);
        return real_path != null ? real_path.toString() : absolute_path.toString();
    }

    /**
     * Same as {@link #tryFindFileWhoseNameBeginsWith(java.lang.String, java.lang.String)
     * }
     * but with the base directory of the file search set to the users working
     * directory.
     */
    public static String tryFindFileWhoseNameBeginsWith(final String file_name) {
        return tryFindFileWhoseNameBeginsWith(file_name, WORKING_DIRECTORY);
    }

    /**
     * Same as {@link #tryFindPathToFileWhoseNameIs(java.lang.String, java.lang.String)
     * }
     * but with the base directory of the file search set to the users working
     * directory.
     */
    public static String tryFindPathToFileWhoseNameIs(final String file_name) {
        return tryFindPathToFileWhoseNameIs(file_name, WORKING_DIRECTORY);
    }

    /**
     * Tries to find a file by searching recursively from base_directory up to
     * its subdirectories.
     *
     * @param file_name the name of the file we are searching for
     * @param base_directory the directory where the recursive file search
     * starts.
     * @return null on failure, non-null on success.
     */
    public static Path tryFindAbsolutePathOfFileWhoseNameIs(final String file_name, final String base_directory) {
        Tester.check(file_name != null, "No null inputs");
        Tester.check(base_directory != null, "No null inputs");

        final Path startingDir = Paths.get(base_directory);
        Tester.check(startingDir != null);
        final Finder finder = new Finder(file_name);
        Tester.check(finder != null);
        try {
            Files.walkFileTree(startingDir, EnumSet.of(FOLLOW_LINKS), mySearchDepth_, finder);
        } catch (IOException e) {
            // usually occurs if you are not allowed to traverse a certain file/folder.
            // ignore it.
        }
        final Path foundPath = finder.done();
        if (foundPath == null) {
            // failure condition.
            return null;
        } // success condition.
        final Path absolute_path = foundPath.toAbsolutePath();
        Tester.check(absolute_path != null);
        return absolute_path;
    }

    /**
     * @return null on failure, non-null on success.
     */
    public static String tryFindPathToFileWhoseNameIs(final String file_name, final String base_directory) {
        Tester.check(file_name != null, "No null inputs");
        Tester.check(base_directory != null, "No null inputs");

        Path real_path = null;
        final Path absolute_path = tryFindAbsolutePathOfFileWhoseNameIs(file_name, base_directory);
        if (absolute_path == null) {
            // File could not be found.
            return null;
        } // else file was found.
        try {
            real_path = absolute_path.toRealPath();
        } catch (IOException e) {
            Tester.killApplication("The absolute path was found, so canonical path should exist", e);
            return null;
        }
        if (real_path != null) {
            return real_path.toString();
        } else {
            return absolute_path.toString();
        }
    }
}
