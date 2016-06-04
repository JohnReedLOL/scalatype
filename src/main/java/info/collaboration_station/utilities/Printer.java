package info.collaboration_station.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Use this to print to the terminal. Also backs up output to a log file.
 *
 * @author johnmichaelreed2
 */
public class Printer {

    // <editor-fold defaultstate="collapsed" desc="Enums">
    /**
     * Used to rank terminal readouts based on relative importance. The default
     * significance level is "SIGNIFICANT". "VERY_SIGNIFICANT" is for things which
     * should not be ignored and "INSIGNIFICANT" is for things that might not
     * concern you.
     */
    public static enum Significance {
        /**
         * For things which you might not want to see in the terminal.
         */
        INSIGNIFICANT(0), 
        /**
         * The default.
         */
        SIGNIFICANT(1), 
        /**
         * For things best not ignored.
         */
        VERY_SIGNIFICANT(2);
        /**
         * My significance starts at zero and increases with increasing
         * significance.
         */
        private final int mySignificanceLevel_;

        private Significance(int significance) {
            mySignificanceLevel_ = significance;
        }

        /**
         * @return the significance level of this enum used for comparison
         * purposes. Starts at zero and increases with increasing significance.
         */
        public int getSignificance() {
            return mySignificanceLevel_;
        }
    }

    /**
     * Used to specify whether something is being printed under an error
     * situation (bad) or a non-error situation (good).
     */
    public static enum ReadoutCondition {

        BAD, GOOD
    }

    /**
     * Used to specify whether everything should be sent to standard output,
     * everything should be sent to standard error, or only bad things/errors
     * should be sent to standard error.
     */
    public static enum DefaultPrintStream {

        ONLY_STANDARD_OUT, ONLY_STANDARD_ERROR, BOTH
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Vars, Getters, and Setters">
    /**
     * The system independent line separator, shortened to two letters for
     * convenience.
     */
    public static final String ls = System.getProperty("line.separator");

    static {
        Tester.check(ls != null);
    }

    /**
     * The number of rows in a stack trace to be displayed in the terminal (in
     * case the stack trace is very long). Note that the entire stack trace is
     * always backed up to the log file just in case.
     */
    private static int myNumRowsPerStackTrace_ = 6;

    /**
     * @return the number of stack trace rows to be displayed in the terminal.
     */
    public static int getNumberOfRowsInStackTraces() {
        return myNumRowsPerStackTrace_;
    }

    /**
     * @param numRows the number of stack trace rows that you would like to see
     * in the terminal when you print out a stack trace. Although not all rows
     * will appear in the terminal, by default they be sent to the log file.
     */
    public static void setNumberOfRowsInStackTraces(int numRows) {
        if (numRows < 0) {
            throw new IllegalArgumentException("You can't display a negative number of rows in a stack trace.");
        }
        myNumRowsPerStackTrace_ = numRows;
    }

    /**
     * When this variable is true, the log file will be printed to. If it is
     * false, the log file will not be used even if it can be written to.
     */
    private static boolean printToLogFile_ = true;

    /**
     * When this variable is true, the terminal will be printed to. If it is
     * false, the terminal will not be printed to.
     */
    private static boolean printToTerminal_ = true;

    /**
     * All messages that are at this significance level or higher are printed.
     * By default set to
     * {@link info.collaboration_station.utilities.Printer.Significance#SIGNIFICANT}
     */
    private static Significance mySignificanceLevel_ = Significance.SIGNIFICANT;

    /**
     * Specified where terminal output should go. Since the color black is
     * easier on the eyes than the color red, and since mixing standard out and
     * standard error causes out-of-order print statements, the default setting
     * is only standard out, even for exceptions.
     */
    private static DefaultPrintStream myTargetPrintStream_ = DefaultPrintStream.ONLY_STANDARD_OUT;

    /**
     * Expresses whether or not the printer is closed.
     */
    private static boolean wasClosed_ = false;

    /**
     * @return true is this printer was closed, false otherwise.
     */
    public static boolean isClosed() {
        return wasClosed_;
    }

    /**
     * Gets the variable {@link #printToLogFile_}.
     *
     * @return true if log file printing is on, false otherwise.
     */
    public static boolean getPrintToLogFile() {
        return printToLogFile_;
    }

    /**
     * Determines whether or not the log file should be printed to.
     *
     * @param useLogFile true for printing to the log file and false for no
     * printing to the log file.
     */
    public static void setPrintToLogFile(boolean useLogFile) {
        printToLogFile_ = useLogFile;
    }

    /**
     * Gets the variable {@link #printToTerminal_}
     *
     * @return true if terminal printing is on, false otherwise.
     */
    public static boolean getPrintToTerminal() {
        return printToTerminal_;
    }

    /**
     * Determines whether of not the terminal should be printed to.
     *
     * @param toPrint true for printing to the terminal or false for no printing
     * to the terminal.
     */
    public static void setPrintToTerminal(boolean toPrint) {
        printToTerminal_ = toPrint;
    }

    /**
     * Gets the variable {@link #mySignificanceLevel_}
     *
     * @return The significance level below which no terminal output will be
     * displayed.
     */
    public static Significance getMyOutputSignificanceLevel() {
        return mySignificanceLevel_;
    }

    /**
     * Sets the variable {@link #mySignificanceLevel_}
     *
     * @param level all messages that are at this significance level or higher are
     * printed. Messages that are below (less important than) this significance level
     * are not printed.
     */
    public static void setMyOutputSignificanceLevel(Significance level) {
        mySignificanceLevel_ = level;
    }

    /**
     * Gets the variable {@link #myTargetPrintStream_}
     */
    public static DefaultPrintStream getMyDefaultPrintStream() {
        return myTargetPrintStream_;
    }

    /**
     * All terminal error messages will go through this DefaultPrintStream.
     */
    public static void setMyDefaultPrintStream(DefaultPrintStream streamType) {
        myTargetPrintStream_ = streamType;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Delayed Initialization Vars">
    /**
     * The name of the folder that holds all the log files, or null if there are
     * no log files.
     */
    private static final String myLogFolderNameOrNull_;

    /**
     * Initialize the above final variable in this static block.
     */
    static {
        final String intendedFolderName = "Log_Files";
        final File logFilesFolder = new File(FileFinder.WORKING_DIRECTORY
                + FileFinder.FILE_SEPARATOR + intendedFolderName);
        if (!logFilesFolder.exists()) {
            // No folder exists, so the folder needs to be created.
            boolean madeFolder = false;
            try {
                madeFolder = logFilesFolder.mkdir();
            } catch (SecurityException se) {
                // madeFolder remains false.
            }

            if (madeFolder) {
                myLogFolderNameOrNull_ = intendedFolderName;
            } else {
                myLogFolderNameOrNull_ = null;
            }
        } else {
            // The folder already exists.
            myLogFolderNameOrNull_ = intendedFolderName;
        }
    }

    /**
     * The name of the log file if log file creation was successful, or null if
     * no log file was created. Perform null checks before use.
     */
    private static final String myLogFileNameOrNull_;

    /**
     * @return Either the name of the log file or empty string if the log file
     * was not successfully created.
     */
    public static String getLogFileNameOrEmptyString() {
        if (myLogFileNameOrNull_ != null) {
            return myLogFileNameOrNull_;
        } else {
            return "";
        }
    }
    /**
     * If initialization was successful, this variable will be non-null,
     * otherwise it will remain null. Perform null checks before use.
     */
    private static OutputStreamWriter myLogOutputStreamWriterOrNull_ = null;
    /**
     * If initialization was successful, this variable will be non-null,
     * otherwise it will by null. Perform null checks before use.
     */
    private static final BufferedWriter myLogBufferedWriterOrNull_; // this does the actual writing

    /**
     * Initialize ALL the above final variables in this static block.
     */
    static {
        if (!(myLogFolderNameOrNull_ == null)) {
            final DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd___HH:mm:ss");
            final Calendar cal = Calendar.getInstance();
            final String dateTimeForLogFile = dateFormat.format(cal.getTime()); /* 2014_08_06___16:00:22 */

            final String expectedFileName = dateTimeForLogFile + ".txt";
            final String fullPath = FileFinder.WORKING_DIRECTORY
                    + FileFinder.FILE_SEPARATOR + myLogFolderNameOrNull_
                    + FileFinder.FILE_SEPARATOR + expectedFileName;

            final File logFile = new File(fullPath);

            boolean wasFileCreated = false;
            try {
                wasFileCreated = logFile.createNewFile();
            } catch (IOException | SecurityException ioe) {
                // wasFileCreated remains false.
            }
            // wasFileCreated remains false.
            boolean success = false;
            if (wasFileCreated == true) {
                final String wasFileFound = FileFinder.tryFindPathToFileWhoseNameIs(expectedFileName);
                Tester.check(wasFileFound != null, "I made the file so I should be able to find it.");
                try {
                    myLogOutputStreamWriterOrNull_ = new OutputStreamWriter(
                            new FileOutputStream(logFile, true), "utf-8");
                } catch (FileNotFoundException fnfe) {
                    Tester.killApplication("It's impossible for a newly created file to not be found.", fnfe);
                } catch (UnsupportedEncodingException uee) {
                    Tester.killApplication("It's impossible for UTF-8 to be an unsupported text format.", uee);
                } catch (Exception e) {
                    //just ignore it, the things we are trying to initialize will be null.
                    myLogOutputStreamWriterOrNull_ = null;
                }

                if (myLogOutputStreamWriterOrNull_ != null) {
                    myLogBufferedWriterOrNull_ = new BufferedWriter(myLogOutputStreamWriterOrNull_);
                    try {
                        myLogBufferedWriterOrNull_.write("Starting log file" + ls);
                        success = true;
                    } catch (IOException ioe) {
                        // Just ignore it - don't kill the thread.
                    }
                } else {
                    myLogBufferedWriterOrNull_ = null;
                }
            } else {
                myLogOutputStreamWriterOrNull_ = null;
                myLogBufferedWriterOrNull_ = null;
            }

            if (success == true) {
                myLogFileNameOrNull_ = expectedFileName;
            } else {
                myLogFileNameOrNull_ = null;
            }
        } else {
            // No log folder, so definetely no log files.
            Printer.printBad("Could not create log file because log folder does not exist.");
            myLogFileNameOrNull_ = null;
            myLogBufferedWriterOrNull_ = null;
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    /**
     * Closes the Printer (which you should do just before application
     * termination). After closing the printer, it should not be used. Does
     * nothing if called repeatedly.
     */
    public static void close() {
        if (!wasClosed_) {
            try {
                if (myLogBufferedWriterOrNull_ != null) {
                    printLineToReadout("\n" + "The log file is being shut down.", ReadoutCondition.GOOD, Significance.SIGNIFICANT);
                    myLogBufferedWriterOrNull_.close();
                }
            } catch (Exception ioe) {
                // ignore the error. myLogBufferedWriterOrNull_ is already closed.
            }
        }
    }

    /**
     * Tries to write text to log file. Replaces all "\n" newline characters
     * that may have been sent to the terminal with OS specific end of line
     * characters before printing to the text file.
     *
     * @param text the text to be written to the log file.
     * @return false if no text is written or true if text is successfully
     * written.
     */
    private static boolean tryWritingSomethingToLogFileNoNewline(String text) {
        if (myLogBufferedWriterOrNull_ == null) {
            return false;
        } else {
            try {
                String updatedText = text.replaceAll("\n", ls);
                myLogBufferedWriterOrNull_.write(updatedText);
                myLogBufferedWriterOrNull_.flush();
                return true;
            } catch (IOException ioe) {
                // means the writer was closed
                return false;
            }
        }
    }

    /**
     * This print statement does not include a stack trace, but it does append a
     * newline.
     *
     * @see #printToReadout(java.lang.String,
     * info.collaboration_station.utilities.Printer.ReadoutCondition,
     * info.collaboration_station.utilities.Printer.Significance)
     */
    public static void printLineToReadout(final String message, ReadoutCondition condition, Significance severity) {
        printToReadout((message + "\n"), condition, severity);
    }

    /**
     * Meant to be used by other print statements internally, this print
     * statement does not include a stack trace.
     *
     * @param message the message to be printed
     * @param condition whether the message is an error or non-error message
     */
    public static synchronized void printToReadout(final String message, ReadoutCondition condition, Significance severity) {

        // Logging happens regardless of severity.
        if (printToLogFile_) {
            // log stuff
            if (!(Printer.myLogFileNameOrNull_ == null)) {
                final String logFilesPathString = FileFinder.tryFindPathToFileWhoseNameIs(myLogFileNameOrNull_);
                Tester.check(logFilesPathString != null, "The log file name is non-null, so the log file must exist.");
                boolean success = Printer.
                        tryWritingSomethingToLogFileNoNewline(message.replaceAll("\n", ls));
                // this success is being silently ignored if it doesn't write to log file,
                // I'm not doing anything about it.
            } else {
                // Don't bother, it won't work.
            }
        }
        if (!printToTerminal_) {
            // Do not printNonError anything.
            return;
        } else if (mySignificanceLevel_.getSignificance() > severity.getSignificance()) {
            // This message is not important enough to be printed. 
            return; // return without printing to terminal.
        } else {
            // This message is important enough to be printed to terminal.
            if (myTargetPrintStream_ == DefaultPrintStream.ONLY_STANDARD_OUT) {
                System.out.print(message);
            } else if (myTargetPrintStream_ == DefaultPrintStream.ONLY_STANDARD_ERROR) {
                System.err.print(message);
            } else {
                // myTargetPrintStream_ == BOTH
                if (condition == ReadoutCondition.BAD) {
                    System.err.print(message);
                } else if (condition == ReadoutCondition.GOOD) {
                    System.out.print(message);
                } else {
                    Tester.killApplication("This condition is logically impossible");
                }
            }
        }
    }

    /**
     * Prints non-error messages with one line stack trace. When target print
     * stream is both std out and err, this prints to standard out.
     *
     * @param message The error message
     * @param severityLevel How important the message is
     * @param stackTraceStart The position of the current line on the stack
     * trace
     */
    private static void printNonError(String message, Significance severityLevel, int stackTraceStart) {
        final String thread_name = Thread.currentThread().getName();
        final String location_of_print_statement = Thread.currentThread().getStackTrace()[stackTraceStart].toString();
        printLineToReadout("\n" + "Thread \"" + thread_name + "\": "
                + location_of_print_statement + "\n" + message, ReadoutCondition.GOOD, severityLevel);
    }

    /**
     * Prints the name of the current thread and where the printNonError
     * statement comes from to standard output or to the stream specified by
     * {@link #myTargetPrintStream_}. The {@link #myTargetPrintStream_} variable
     * acts as a global override for the preferred output stream of the
     * application.
     *
     * @param message - message to be printed
     */
    private static void printGood(String message, Significance severityLevel) {
        printNonError(message, severityLevel, 3);
    }

    /**
     * Informs about something that is not very significant or important, but
     * that isn't bad.
     */
    public static void printSlightlyGood(String message) {
        printNonError(message, Significance.INSIGNIFICANT, 3);
    }

    /**
     * Prints something that is good or that was supposed to happen.
     *
     * @param message the message to be printed as if it were a regular
     * non-error message.
     */
    public static void printGood(String message) {
        printNonError(message, Significance.SIGNIFICANT, 3);
    }

    /**
     * Informs about an usual or special occurrence or event with significant
     * effect on code flow. Not for errors.
     */
    public static void printVeryGood(String message) {
        printNonError(message, Significance.VERY_SIGNIFICANT, 3);
    }

    /**
     * Prints error messages with one line stack trace. When target print stream
     * is both std out and error, this prints to standard error.
     *
     * @param message The error message
     * @param severityLevel How important the message is
     * @param stackTraceStart The position of the current line on the stack
     * trace
     */
    private static void printError(String message, Significance severityLevel, int stackTraceStart) {
        final String thread_name = Thread.currentThread().getName();
        final String location_of_print_statement = Thread.currentThread().getStackTrace()[stackTraceStart].toString();
        printLineToReadout("\n" + "Thread \"" + thread_name + "\": "
                + location_of_print_statement + "\n" + message, ReadoutCondition.BAD, severityLevel);
    }

    /**
     * Internal message for printing errors/warnings and notifying about
     * unforseen events with a negative effect on program execution. Includes
     * the thread name and stack trace.
     *
     * @param message message to be printed
     * @param severityLevel How significant the output is
     */
    private static void printBad(String message, Significance severityLevel) {
        printError(message, severityLevel, 3);
    }

    /**
     * Notifies about inconveniences or non-positive mundane occurrences. Can
     * also be used for things that we don't care to read, like the last 20
     * lines of a 200 line stack trace.
     *
     * @param message the message to be printed.
     */
    public static void printSlightlyBad(String message) {
        printError(message, Significance.INSIGNIFICANT, 3);
    }

    /**
     * Notified about potential threats or negative occurrences. Not meant for
     * fatal errors.
     *
     * @param message the message to be printed.
     */
    public static void printBad(String message) {
        printError(message, Significance.SIGNIFICANT, 3);
    }

    /**
     * Notifies about potentially fatal or highly unusual negative events.
     *
     * @param message the message to be printed.
     */
    public static void printVeryBad(String message) {
        printError(message, Significance.VERY_SIGNIFICANT, 3);
    }

    /**
     * Prints out an exception formatted appropriately (the first
     * {@link #myNumRowsPerStackTrace_} lines of the stack trace
     * with the point at which the exception is printed.).
     *
     * @param t Exception or error to be printed as an error.
     */
    public static void printException(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException("You can't print a null exception");
        }
        String toPrint = "\n" + t.toString();
        // prints the first n lines of the stack trace, starting from line zero.
        Package_Private_Methods.printStackTraceInternally(toPrint, t.getStackTrace(), 0);
        // prints the line where print stack trace was called in case differes from location where the exception was caught/thrown.
        printLineToReadout(Thread.currentThread().getStackTrace()[3].toString() + "\n", ReadoutCondition.BAD, Significance.VERY_SIGNIFICANT);

    }

    /**
     * Prints out a message and a stack trace caused by a throwable as an error.
     * Message cannot be null.
     *
     * @param message message to be printed (cannot be null)
     * @param t throwable that caused the message to be printed
     */
    public static void printException(String message, Throwable t) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
        if (t == null) {
            throw new IllegalArgumentException("You can't print a null exception");
        }
        printThrowableInternal(message, t);
    }

    private static void printThrowableInternal(String nonNullMessage, Throwable t) {
        Tester.check(nonNullMessage != null, "Non-null message cannot be null.");
        Tester.check(t != null, "You can't print a null exception");

        String toPrint = "\n" + nonNullMessage + "\n" + t.toString();
        // prints the first n lines of the stack trace, starting from line zero.
        Package_Private_Methods.printStackTraceInternally(toPrint, t.getStackTrace(), 0);
        // prints the line where print stack trace was called in case differes from location where the exception was caught/thrown.
        printLineToReadout("Printed at: " + Thread.currentThread().getStackTrace()[3].toString() + "\n", ReadoutCondition.BAD, Significance.VERY_SIGNIFICANT);
    }

    /**
     * Container for package private methods.
     */
    protected static class Package_Private_Methods {

        /**
         * ONLY MEANT BE BE USED IN THE info.collaboration-station.utilities
         * PACKAGE.
         *
         * Prints a stack trace (starting from firstRow) which is preceded by a
         * leading message ("\n" added to end of leading message) and followed
         * by a newline at the end.
         *
         * @param message the message which appears just before the stack trace.
         * @param stackTrace The stack trace to be printed.
         * @param firstRow The first row of the stack trace, between zero and
         * length.
         */
        public static void printStackTraceInternally(final String message, StackTraceElement[] stackTrace, int firstRow) {
            /**
             * This will be the important (front) part of the stack trace.
             */
            final int numImportantRows = myNumRowsPerStackTrace_;
            String importantConcatenation = "";
            importantConcatenation += (message + "\n"); // add leading message and newline
            final int length = stackTrace.length;
            Tester.check(firstRow <= length, "The first row of the stack trace is outside of the bounds of the stack trace array.");
            final int totalNumRowsToPrint = length - firstRow;
            if (totalNumRowsToPrint <= numImportantRows) {
                // This loops totalNumRowsToPrint times.
                for (int i = firstRow; i < firstRow + totalNumRowsToPrint; ++i) {
                    importantConcatenation += stackTrace[i] + "\n";
                }
                printToReadout(importantConcatenation, ReadoutCondition.BAD, Significance.VERY_SIGNIFICANT); // This println statement adds an extra trailing newline
                return;
            } else {
                // Both of these for loops together loop totalNumRowsToPrint times.
                // First for loop iterates numImportantRows times.
                for (int i = firstRow; i < firstRow + numImportantRows; ++i) {
                    importantConcatenation += stackTrace[i] + "\n";
                }
                printToReadout(importantConcatenation, ReadoutCondition.BAD, Significance.VERY_SIGNIFICANT); // This println statement adds an extra trailing newline
                /**
                 * This will be the less important (back) part of the stack
                 * trace.
                 */
                String unimportantConcatenation = "";
                for (int i = firstRow + numImportantRows; i < firstRow + totalNumRowsToPrint; ++i) {
                    unimportantConcatenation += stackTrace[i] + "\n";
                }
                printToReadout(unimportantConcatenation, ReadoutCondition.BAD, Significance.INSIGNIFICANT);
                return;
            }
        }
    }
    // </editor-fold>
}
