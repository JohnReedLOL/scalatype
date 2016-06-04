package com.example;

    import java.io.*;
    import java.util.*;
    import info.collaboration_station.utilities.*;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.regex.*;

    public class JavaMain {

        public static void capture() {
            String input = "I have a cat-  -, but I like my dog better.";

            Pattern p = Pattern.compile("(mouse|cat-  -|dog|wolf|bear|human)");
            Matcher m = p.matcher(input);

            List<String> animals = new ArrayList<String>();
            while (m.find()) {
                System.out.println("Found a " + m.group() + ".");
                animals.add(m.group());
            }
        }

        public static void capture2() {
            String input = "User clientId=23421. Some more text clientId=33432. This clientNum=100";

            Pattern p = Pattern.compile("(clientId=)(\\d+)");
            Matcher m = p.matcher(input);

            StringBuffer result = new StringBuffer();
            while (m.find()) {
                System.out.println("Masking: " + m.group(2));
                m.appendReplacement(result, m.group(1) + "***masked***");
            }
            m.appendTail(result);
            System.out.println(result);
        }

        public static void main(String args[]) throws Exception {
            capture();
            capture2();
/*
// This is the command we are going to be running:

sbt "clean" "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")" "compile" "exit"

// This is the regex we are going to be matching for:

" // *.scala"

*/
            List<String> input = new ArrayList<String>();
            input.add("123-45-6789"); // match
            input.add("9876-5-4321");
            input.add("987-65-4321 (attack)");
            input.add("987-65-4321 ");
            input.add("192-83-7465"); // match
            input.add("192--7465");
            input.add("192--");
            input.add("192");
            input.add("7465");
            input.add("987654321"); // match

            // ., \\d, \\D, \\s, \\S, \\w, \\W
            /*
                .  	Dot, any character (may or may not match line terminators, read on)
                \d  	A digit: [0-9]
                \D  	A non-digit: [^0-9]
                \s  	A whitespace character: [ \t\n\x0B\f\r]
                \S  	A non-whitespace character: [^\s]
                \w  	A word character: [a-zA-Z_0-9]
                \W  	A non-word character: [^\w]
             */
            // *, +, ?, {n}, {n,}, {n,m}
            /*
                *      Match 0 or more times
                +      Match 1 or more times
                ?      Match 1 or 0 times
                {n}    Match exactly n times
                {n,}   Match at least n times
                {n,m}  Match at least n but not more than m times
             */
            /*
            \   	Escape the next meta-character (it becomes a normal/literal character)
            ^   	Match the beginning of the line
            .   	Match any character (except newline)
            $   	Match the end of the line (or before newline at the end)
            |   	Alternation (‘or’ statement)
            ()  	Grouping
            []  	Custom character class
             */

            String ssnMatcher = "^" + // Match the beginning of the line
                "(" + // Start grouping
                "\\d{3}" + // Exactly 3 digits [0-9]
                "-?" + // A hyphen match 1 or 0 times
                "\\d{2}" + // Exactly 2 digits [0-9]
                "-?" + // A hyphen match 1 or 0 times
                "\\d{4}" + // Exactly 4 digits [0-9]
                ")" + // end grouping
            "$"; // Match the end of the line

            for (String ssn : input) {
                if (ssn.matches("^(\\d{3}-?\\d{2}-?\\d{4})$")) {
                    System.out.println("Found good SSN: " + ssn);
                }
            }

            String matcher2 = "I lost my " +
                "\\w+"; // 1 or more word characters [a-zA-Z_0-9]

            if ("I lost my 149regVRT56rt".matches(matcher2)) {
                System.out.println("Found good");
            }

            String matcher3 = "I lost my:? (val|var)";

        }
    }