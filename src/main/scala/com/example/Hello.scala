package com.example

import java.io._
import java.util._
import scala.trace.{Debug, SDebug, Pos}
import scala.util.matching.Regex
// import info.collaboration_station.utilities._

object Hello {
  def main(args: Array[String]): Unit = {
    val toPrint = "Hello, world!"
    println(toPrint)

    val proc: Process = Runtime.getRuntime.exec(Array[String]("sbt", "clean", "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")", "compile", "exit"))
    val in: BufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream))
    var line: String = null

    // start line
    // optional leading characters
    // leading whitespace [1 or more]
    // double forward slash
    // one whitespace character
    // file name (one or more characters)
    // .java
    // end of line
    val FileNameExtractorRegEx =
      """
         |^(
         |.*
         |\s+
         |//
         |\s
         |fileName=(.+)
         |.java
         |)$
       """.stripMargin

    // extracts declarations such as " val cat_!: Cat = makeCat()" or "Foo.bar();var     <><: :Int = 7"
    // or "/*comment*/ var cat: CAT_<>< = makeCat()"
    val DeclarationExtractorRegEx =
      // start line
      // whitespace or comments than semicolon then whitespace or comments then whitespace
      // var or val
      // optional whitespace
      // variable name with arbitrary characters (not necessarily lowercase)
      // optional whitespace
      // colon
      // optional whitespace
      // An upper case letter indicating a type
      // subsequent optional letters or symbols following it (non-whitespace)
      // optional whitespace
      // and equal sign
      // arbitrary subsequent characters (including but not limited to whitespace)
      // end of line
      """
        |^(
        |(\s*|.*;\s*|.*\s+)
        |(val |var )
        |\s*
        |varName=(.+)
        |\s*
        |:
        |\s*
        |typeName=([A-Z]\S+)
        |\s*
        |=
        |.*
        |)$
      """.stripMargin

    val DummyRegEx: Regex  = """(.*)""".r

    val Process = """([a-cA-C])([^\s]+)""".r // define first, rest is non-space
    for (p <- Process findAllIn "baha bah Cah zzzzzzAahr") p match {
      case Process("b", rest) => println("first: 'b', some rest " + rest) // b aha, b ha
      case Process(first, rest) => println("some " + first + ", rest: " + rest) // C ah, some A, rest: ahr
      // etc.
    }


    val MY_RE = "(foo|bar).*".r
    val result = "foo123" match { case MY_RE(m) => m; case _ => "No match" }
    val MY_RE2 = "(foo|bar)".r
    val result2 = "foo123" match { case MY_RE2(m) => m; case _ => "No match" }
    Debug.trace(result)
    Debug.trace(result2)

    // (.+)
    // triple quote includes whitespace and newline. Remove it with .replaceAll("(\\s|\n)"
    val BookExtractorRE: Regex =
      """([^,]+) , (val|var) \s+
         ([^,]+) \s{1} author= (.+)""".replaceAll("(\\s|\n)", "").r     // <1>
    val MagazineExtractorRE: Regex = """([^,]+),\s+issue=(.+)""".r

    val catalog = Seq(
      "title=Programming Scala Second Edition,val  booboo author=Dean Wampler",
      "title=The New Yorker, issue=January 2014",
      "Unknown: text=Who put this here??"
    )

    for (item <- catalog) {
      item match {
        case BookExtractorRE(title, variable, separator, author) => // <2>
          println(s"""Book "$title", written by $author. variable = $variable, sep = $separator""")
        case MagazineExtractorRE(title, issue) =>
          println(s"""Magazine "$title", issue $issue""")
        case entry => println(s"Unrecognized entryyy: $entry")
      }
    }
    System.exit(-1)
      /*
      val declarationExtractorRegEx = raw"^(" +
        raw"\s*" +
        raw"" +
        raw"$"
      */
    while ({line = in.readLine; line} != null) {
      System.out.println(line)
      line match {
        case DummyRegEx(title) => println(s""" dummy: $title """)
        case noMatch => println("noMatch: " + noMatch)
      }
    }
    System.out.println("Done1")
    in.close
  }
}
