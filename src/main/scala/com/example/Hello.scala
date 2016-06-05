package com.example

import java.io._
import java.nio.file.Path
import java.util._

import scala.trace.{Debug, Pos, SDebug}
import scala.util.matching.Regex
import info.collaboration_station.utilities.{FileFinder, GlobFinder}

object Hello {
  def main(args: Array[String]): Unit = {

    /*
    Commenting out the below line causes:
    Disconnected from the target VM, address: '127.0.0.1:43748', transport: 'socket'
    Exception in thread "main" java.lang.NoClassDefFoundError: info/collaboration_station/utilities/GlobFinder
      at info.collaboration_station.utilities.FileFinder.tryFindAbsolutePathOfFileWhoseNameIs(FileFinder.java:128)
      at com.example.Hello$.main(Hello.scala:137)
      at com.example.Hello.main(Hello.scala)
    Caused by: java.lang.ClassNotFoundException: info.collaboration_station.utilities.GlobFinder
      at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
      at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
      at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:331)
      at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
      ... 3 more
     */
    val vvv = new GlobFinder("foo")

    val toPrint = "Hello, world!"
    println(toPrint)

    val proc: Process = Runtime.getRuntime.exec(Array[String]("sbt", "clean", "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")", "compile", "exit"))
    val in: BufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream))
    var line: String = null

    Thread.sleep(100)

    val string1 = " // Tester.java"
    val string2 = " // Hello.scala"
    // [[syntax trees at end of                    parser]] // JavaMain.java--NO_MATCH
    /*
    string1 match {
      case ScalaFileRegEx((fileName1 @ _), (suffix @ _)) => println("file: " + fileName1 + " suffix: " + suffix)
      case other => println(other + "--NO_MATCH")
    }*/
    string2 match {
      case RegEx.ScalaFileExtractor(fileName) => println("file: " + fileName)
      case other => println(other + "--NO_MATCH")
    }
    type CAT_<>< = Int
    def makeCat() = 8
    /*comment*/ var cat: CAT_<>< = makeCat()
    val string3 = " /*comment*/   var cat    :  CAT_<><  = makeCat() ;"
    string3 match {
      case RegEx.DeclExtractor(filler, decl, varName, refType) => println(s"$decl $varName $refType")
      case _ => println("Fail")
    }
    val string4 = " var   cat  = makeCat();"
    string4 match {
      case RegEx.DeclExtractorNoBoilerplate(filler, decl, varName) => println(s"$decl $varName")
      case _ => println("Fail2")
    }
    // System.exit(-1)

    println("Working directory: " + FileFinder.WORKING_DIRECTORY)
    FileFinder.setMySearchDepth(20)

    var currentLine = 0 // start at zero and go up
    var currentFile = "" // start with no file.
    var currentLineDesugared = 0
    while ( {line = in.readLine; line} != null) {
      //System.out.println(line)
      line match {
        //  // Tester.java
        case RegEx.ScalaFileExtractor(fileName) =>
          println("file: " + fileName + "\n\n")
          val pathNullable: Path = FileFinder.tryFindAbsolutePathOfFileWhoseNameIs(fileName, FileFinder.WORKING_DIRECTORY)
          val pathOption: Option[Path] = Option(pathNullable) // None if path is null
          pathOption match {
            case Some(path) =>
              println("Some path: " + path.toString)
              currentFile = path.toString
              currentLine = 0

              var line: String = null
              var lines: String = ""
              val br: BufferedReader = new BufferedReader(new FileReader(currentFile))
              try {
                while ( {line = br.readLine; line} != null) {
                  line match {
                    case RegEx.DeclExtractorNoBoilerplate(filler, decl, varName) => {println(s"$filler$decl $varName --DEFINED")}
                    case _ => println("No declaration")
                  }
                  // if the line is a declaration, insert the type (increment the desugared file until you get a match)
                }
              } finally {
                if (br != null) {
                  br.close()
                }
              }
              println("Done!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            //JavaMain.handleFile(path)
            case None => println("No path")
          }
        // Find wile with name: fullFileName in project.
        case other => println(other + "--NO_MATCH") //  // Hello.scala
      }
      currentLineDesugared += 1
    }
    System.out.println("Done1")
    in.close
  }
}
