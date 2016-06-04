package info.collaboration_station.utilities;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Use this for run time checks and routine background checks. Helps verify that
 * the application is working correctly.
 *
 * @author johnmichaelreed2
 */
public class Tester {

    // <editor-fold defaultstate="collapsed" desc="Vars, Getters, and Setters">
    /**
     * This thread polls for a variety of background events, where each
     * background events is an materialization of the
     * {@link Utilities.BackgroundEvent_Interface} interface. Its name is
     * "Event_Checker". Its initialization is deferred until its first use.
     */
    private static ScheduledExecutorService myScheduler_ = null;

    private static boolean areRuntimeChecksOn = true;

    private static boolean areBackgroundChecksOn = true;

    public static void setRuntimeChecks(boolean isOn) {
        areRuntimeChecksOn = isOn;
    }

    public static boolean getRuntimeChecksIsOn() {
        return areRuntimeChecksOn;
    }

    public static void setBackgroundChecks(boolean isOn) {
        areBackgroundChecksOn = isOn;
    }

    public static boolean getBackgroundChecksIsOn() {
        return areBackgroundChecksOn;
    }
    /**
     * Determines whether or not the background event check handler has been
     * closed.
     */
    private static boolean wasClosed_ = false;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">
    
    /**
     * Checks to see if an argument is true and throws a
     * {@link java.lang.IllegalArgumentException} if false.
     *
     * @param arg the precondition argument that must be true.
     * @param message the message to be contained with your
     * {@link java.lang.IllegalArgumentException}.
     */
    public static void checkArg(boolean arg, String message) {
        if (!arg) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * Checks to see if an argument is true and throws a
     * {@link java.lang.IllegalArgumentException} if false.
     *
     * @param arg the precondition argument that must be true.
     * @param message the message to be contained with your
     * {@link java.lang.IllegalArgumentException}.
     */
    public static void checkArg(String message, boolean arg) {
        if (!arg) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * Checks that an object is not null. If null, throws a {@link java.lang.IllegalArgumentException}.
     * @param obj the object to not be null.
     */
    public static void checkNotNull(Object obj) {
        if(obj == null) {
            throw new NullPointerException(obj.getClass().getName() + " was null.");
        }
    }
    
    /**
     * Checks that objects are not null. If null, throws a {@link java.lang.IllegalArgumentException}.
     * @param objects the object which ought not be null.
     */
    public static void checkNotNull(Object... objects) {
        for(int i = 0; i < objects.length; ++i) {
            if(objects[i] == null) {
                throw new NullPointerException(objects[i].getClass().getName() + " was null.");
            }
        }
    }

    /**
     * Closes the Tester by killing any background threads it may be running and
     * blocking any new
     * {@link info.collaboration_station.utilities.BackgroundEvent}s from being
     * added to the queue. After closing, background checks should not be
     * submitted to the tester. Does nothing if called repeatedly.
     */
    public static void close() {
        if (!wasClosed_) {
            try {
                if (myScheduler_ != null) {
                    myScheduler_.shutdownNow();
                    Printer.printLineToReadout("\n" + "The background-check scheduler has been shut down", Printer.ReadoutCondition.GOOD, Printer.Significance.VERY_SIGNIFICANT);
                }
            } catch (Exception e) {
                // ignore it.
            }
            wasClosed_ = true;
        }
    }

    /**
     * Kills the entire application and leaves a stack trace.
     *
     * @param message - message to printGood before the application terminates.
     */
    public static void killApplication(String message) {
        Tester.check(false, message, 3);
    }

    /**
     * Kills the application, prints a message, and also printGood the throwable
     * who is responsible for the crash. Prints the entire throwable.
     *
     * @param message message to printGood before the application terminates.
     * @param t throwable whose stack trace is to be included in the
     * termination.
     */
    public static void killApplication(String message, Throwable t) {
        StackTraceElement[] ste = t.getStackTrace();
        String concatenation = message + "\n";
        concatenation += t.toString() + "\n";
        for (int i = 0; i < ste.length; ++i) {
            concatenation += (ste[i].toString() + "\n");
        }
        Tester.killApplicationNoStackTrace(concatenation);
    }

    /**
     * Alternate form of {@link #killApplication(java.lang.String, java.lang.Throwable)
     * }. Prints the entire stack trace.
     */
    public static void killApplication(Throwable t, String messsage) {
        killApplication(messsage, t);
    }

    /**
     * Kills the entire application and prints out the stack trace elements of
     * the throwable responsible
     *
     * @param t the throwable responsible.
     */
    public static void killApplication(Throwable t) {
        killApplication("", t);
    }

    /**
     * Kills the entire application without leaving a stack trace.
     *
     * @param message - message to printGood before the application terminates.
     */
    public static void killApplicationNoStackTrace(String message) {
        Printer.printLineToReadout("\n" + message, Printer.ReadoutCondition.BAD, Printer.Significance.VERY_SIGNIFICANT);
        close();
        System.exit(1);
    }

    /**
     * Checks to see if an assertion is true. Prints stack trace and crashes the
     * program if not true. USE THIS INSTEAD OF REGULAR "assert" STATEMENTS. For
     * information on how to use an assert statement, see:
     * <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/language/assert.html">Programming
     * With Assertions</a>
     *
     * @param assertion - assertion to be checked
     */
    public static void check(boolean assertion) {
        check(assertion, "Empty_Assertion", 3); // nomally it would be 2 but the indirections bumps it up to 3.
    }

    /**
     * Checks to see if an assertion is true and prints an error message if
     * false. Also prints a stack trace and crashes the program if false. For
     * information on how to use an assert statement, see:
     * <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/language/assert.html">Programming
     * With Assertions</a>
     *
     * @param assertion - assertion to be checked
     * @param message - error message to printGood
     */
    public static void check(final boolean assertion, final String message) {
        check(assertion, message, 3); // nomally it would be 2 but the indirections bumps it up to 3.
    }

    /**
     * Only checks for assertions when {@link #areRuntimeChecksOn} is true.
     */
    private static void check(final boolean assertion, final String message, int firstRowOfStackTrace) {
        if (!assertion && (areRuntimeChecksOn == true)) {

            String toBePrinted = "\n" + "Assertion failed in Thread: \""
                    + Thread.currentThread().getName() + "\"" + "\n" + message;
            // length of stack trace is 5 (thread called by check called by check called by handle called by main)
            //printlnToReadout(toBePrinted, ReadoutCondition.BAD, Significance.VERY_SIGNIFICANT); // printGood rows 3 & 4
            final StackTraceElement[] stackTraceArray = Thread.currentThread().getStackTrace();
            // This should printGood the stack trace from firstRowOfStackTrace down.
            Printer.Package_Private_Methods.printStackTraceInternally(toBePrinted, stackTraceArray, firstRowOfStackTrace);
            close();
            System.exit(1);
        }
    }

    /**
     * Same as {@link info.collaboration_station.utilities.Tester#tryPollForBackgroundEventEveryXmsStartingInYms(info.collaboration_station.utilities.BackgroundEvent, long, long)
     * }
     * , but the first poll occurs after a time delay equal to the delay between
     * subsequent polls.
     *
     * @return true on successful polling event submission, false if event
     * cannot be polled for.
     */
    public static boolean tryPollForBackgroundEventEveryXms(BackgroundEvent event, long milliseconds) {
        try {
            return tryPollForBackgroundEventEveryXmsStartingInYms(event, milliseconds, milliseconds);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }
    }

    /**
     * Polls for and responds to a background event at regular intervals
     *
     * @param event the event to poll for and respond to
     * @param millisecondInterval the number of milliseconds between the end of
     * one poll/response and the start of the next.
     * @param millisecondsToPollingStart the number of milliseconds until the
     * first poll for background event occurs.
     * @return true if the event can be polled for and false if an exception
     * occurs preventing the polling events from happening.
     */
    public static boolean tryPollForBackgroundEventEveryXmsStartingInYms(final BackgroundEvent event, final long millisecondInterval, final long millisecondsToPollingStart) {
        if (event == null) {
            throw new IllegalArgumentException("Event canot be null");
        } else if (millisecondInterval <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        } else if (millisecondsToPollingStart <= 0) {
            throw new IllegalArgumentException("Delay must be positive");
        }
        if (areBackgroundChecksOn == false || wasClosed_ == true) {
            return false;
        }

        if (myScheduler_ == null) {
            /* deferred instantiation. Make the thread a daemon thread so that checks can continue until application
             termination without preventing the JVM from halting.*/
            myScheduler_ = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                }
            });
            // deferred name assignment.
            myScheduler_.execute(new Runnable() {
                // Set the name of myEventCheckerThread_ to "Event_Checker".
                @Override
                public void run() {
                    Thread.currentThread().setName("Event_Checker");
                }
            });
        }
        final Runnable handleEvent = new Runnable() {

            @Override
            public void run() {
                if (!Thread.currentThread().isInterrupted()) {
                    final boolean didEventOccur;
                    // Check for events if background checking is on.
                    if (Tester.getBackgroundChecksIsOn()) {
                        didEventOccur = event.checkForEventOccurance();
                    } else {
                        didEventOccur = false;
                    }
                    if (didEventOccur) {
                        event.respondToEventOccurance();
                    }
                } else {
                    return; // terminates on interrupt.
                }
            }
        };
        try {
            myScheduler_.scheduleWithFixedDelay(handleEvent, millisecondsToPollingStart, millisecondInterval, TimeUnit.MILLISECONDS);
            return true;
        } catch (RejectedExecutionException ree) {
            Printer.printException("myScheduler_ is already shut down", ree);
            return false;
        } catch (Exception someOtherException) {
            return false;
        }
    }
    // </editor-fold>
}
