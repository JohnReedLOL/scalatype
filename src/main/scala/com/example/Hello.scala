package com.example

import java.io._
import java.util._
import info.collaboration_station.utilities._

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
    val fileNameExtractorRegEx =
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
    val declarationExtractorRegEx =
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
    /*
    val declarationExtractorRegEx = raw"^(" +
      raw"\s*" +
      raw"" +
      raw"$"
    */
    while ({line = in.readLine; line} != null) {
      System.out.println(line)
    }
    System.out.println("Done1")
    in.close
  }
}
