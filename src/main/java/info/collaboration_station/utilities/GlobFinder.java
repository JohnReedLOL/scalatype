package info.collaboration_station.utilities;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

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
public class GlobFinder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher_;
    public static Path last_file_path_found = null;

    public GlobFinder(final String pattern) {
        this.matcher_ = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
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
