package com.example

import java.io._
import java.nio.file.Path
import java.util._

import scala.trace.{Debug, Pos, SDebug}
import info.collaboration_station.utilities.{FileFinder, GlobFinder, Tester}

import scala.util.matching.Regex


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
    val vvv = new GlobFinder("foo") // this does appear in the parsed code.

    val toPrint = "Hello, world!"
    println(toPrint)

    val proc: Process = Runtime.getRuntime.exec(Array[String]("sbt", "clean", "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")", "compile", "exit"))
    val in: BufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream))
    var line: String = null

    Thread.sleep(100)

    Debug.trace("Working directory: " + FileFinder.WORKING_DIRECTORY)
    FileFinder.setMySearchDepth(20)

    // The desugared code doesn't have a type on it.
    val testInput = """      val vvv = new GlobFinder("foo");"""
    testInput match {
      case RegEx.DeclExtractor(boilerFiller, boilerDecl, boilerVarName, boilerRefType) =>
        Debug.trace(s"$boilerFiller$boilerDecl $boilerVarName: $boilerRefType")
      case unmatchedInput => Debug.trace(unmatchedInput) //       val vvv = new GlobFinder("foo");
    }

    var currentLine = 0 // start at zero and go up
    var currentFile = "" // start with no file.
    var currentLineDesugared = 0
    // first search through desugared code
    while ( {line = in.readLine; line} != null) {
      //System.out.println(line)
      line match {
        //  // Tester.java
        case RegEx.ScalaFileExtractor(fileName) =>
          Debug.trace("file: " + fileName)
          val pathNullable: Path = FileFinder.tryFindAbsolutePathOfFileWhoseNameIs(fileName, FileFinder.WORKING_DIRECTORY)
          val pathOption: Option[Path] = Option(pathNullable) // None if path is null
          pathOption match {
            case Some(path) =>
              Debug.trace("Some path: " + path.toString)
              currentFile = path.toString
              currentLine = 0

              var line: String = null
              var lines: String = ""
              val br: BufferedReader = new BufferedReader(new FileReader(currentFile))
              try {
                // then search through source code
                while ( {line = br.readLine; line} != null) {
                  line match {
                    case RegEx.DeclExtractorNoBoilerplate(leftSide, filler, decl, varName, rightSide) => {
                      Debug.trace(s"Matched $leftSide$rightSide. In source file (No boilerplate).")
                      var matchesDeclaration = false
                      var boilerLine: String = null
                      // then search through desugared code again
                      while ({boilerLine = in.readLine; boilerLine} != null && matchesDeclaration == false) {
                        boilerLine match {
                            // val vvv = new GlobFinder("foo");
                          case RegEx.DeclExtractor(boilerFiller, boilerDecl, boilerVarName, boilerRefType) =>
                            if(boilerDecl.equals(decl) && boilerVarName.equals(varName)) {
                              Debug.trace(s"$leftSide$rightSide| matched: $boilerDecl $boilerVarName: $boilerRefType = ...;")
                              matchesDeclaration = true // exit
                            } else {
                              Debug.trace(s"$leftSide$rightSide| failed to match: $boilerDecl $boilerVarName: $boilerRefType = ...;")
                            }
                          case RegEx.DeclExtractorNoType(boilerFiller, boilerDecl, boilerVarName) =>
                            if(boilerDecl.equals(decl) && boilerVarName.equals(varName)) {
                              // they matched, but no need to insert a type
                              Debug.trace(s"$leftSide$rightSide| matched: $boilerDecl $boilerVarName = ...;")
                              matchesDeclaration = true // exit
                            } else {
                              Debug.trace(s"$leftSide$rightSide| failed to match: $boilerDecl $boilerVarName = ...;")
                            }
                          case unmatchedLine  => {
                            System.err.println(s"Was searching for: |$leftSide$rightSide| in boilerplatey code")
                            System.err.println(unmatchedLine + Pos())
                            /*
Was searching for: |    val vvv = new GlobFinder("foo") // this does appear in the parsed code.| in boilerplatey code
      val vvv = new GlobFinder("foo"); - com.example.Hello.main(Hello.scala:83)
      ^ It's skipping over the match!
                             */
                          } // too much output
                        }
                      }
                      Debug.trace(s"$filler$decl $varName --DEFINED")
                    }
                    case unmatchedLine => {
                      lines += unmatchedLine + File.separator // append without modification
                    }
                  }
                  // if the line is a declaration, insert the type (increment the desugared file until you get a match)
                }
              } finally {
                if (br != null) {
                  br.close()
                }
              }
              Debug.trace("Done!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            //JavaMain.handleFile(path)
            case None => Tester.killApplication(
              s"This file $fileName was supposed to exist, but could not be found in " + FileFinder.WORKING_DIRECTORY
            )
          }
        // Find wile with name: fullFileName in project.
        case other => println(other + "--NO_MATCH" + Pos()) //  // Hello.scala
      }
      currentLineDesugared += 1
    }
    Debug.trace("Done1")
    in.close
  }
}
