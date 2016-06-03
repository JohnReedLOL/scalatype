package com.example;

    import java.io.*;
    import java.util.*;

    public class JavaMain {
        public static void main(String args[]) throws Exception {

/*
// This is the command we are going to be running:

sbt "clean" "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")" "compile" "exit"

// This is the regex we are going to be matching for:

" // *.scala"

*/

            System.out.println("Try #1 - using ProcessBuilder");
            {
                ProcessBuilder pb = new ProcessBuilder("javac", "-help");
                Map<String, String> env = pb.environment();
                env.put("VAR1", "myValue");
                env.remove("OTHERVAR");
                env.put("VAR2", env.get("VAR1") + "suffix");
                pb.directory(null); // use the working directory of the current
                File log = new File("log");
                pb.redirectErrorStream(true);
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
                Process p = pb.start();
                assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
                assert pb.redirectOutput().file() == log;
                assert p.getInputStream().read() == -1;
            }
            System.out.println("Try #2 - using Runtime.getRuntime().exec");
            // Process p = Runtime.getRuntime().exec("ls -l"); // this works
            //Process proc = Runtime.getRuntime().exec("javac -help"); //
            // sbt "clean" "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")" "compile" "exit"
            Process proc = Runtime.getRuntime().exec(new String[] {"sbt", "clean",
            "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")",
            "compile", "exit"});
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader inErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            {
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("Done1");
                in.close();
            }
            {
                String line = null;
                while ((line = inErr.readLine()) != null) {
                    // System.out.println(line);
                }
                System.out.println("Done2");
                inErr.close();
            }
        }
    }