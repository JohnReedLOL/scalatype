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

    val proc: Process = Runtime.getRuntime.exec(Array[String]("sbt", "clean", "set scalacOptions in ThisBuild ++= Seq(\"-Xprint:parser\")", "compile", "exit"))
    val in: BufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream))
    var line: String = null

    Thread.sleep(100)

    Debug.trace("Working directory: " + FileFinder.WORKING_DIRECTORY)
    FileFinder.setMySearchDepth(20)

    type FiveStrings = (String, String, String, String, String)
    def matchInDesugaredCode(reader: BufferedReader, parsedDeclaration: FiveStrings): Unit = {
      val (leftSide, filler, valOrVar, varName, rightSide) = parsedDeclaration
      var matchesDeclaration = false
      while (matchesDeclaration == false && line != null) {
        line = reader.readLine
        Debug.trace("Starting Loop 3!!!")
        line match {
          case RegEx.TypedDesugaredDeclExtractor(boilerFiller, boilerValOrVar, boilerVarName, boilerRefType) =>
            if(boilerValOrVar.equals(valOrVar) && boilerVarName.equals(varName)) {
              Debug.trace(s"$leftSide$rightSide| matched: $boilerValOrVar $boilerVarName: $boilerRefType = ...;")
              matchesDeclaration = true // exit
            } else {
              Debug.trace(s"$leftSide$rightSide| failed to match: $boilerValOrVar $boilerVarName: $boilerRefType = ...;")
            }
          case RegEx.DesugaredDeclExtractor(boilerFiller, boilerDecl, boilerVarName) =>
            if(boilerDecl.equals(valOrVar) && boilerVarName.equals(varName)) {
              // they matched, but no need to insert a type
              Debug.trace(s"$leftSide$rightSide| matched: $boilerDecl $boilerVarName = ...;")
              matchesDeclaration = true // exit
            } else {
              Debug.trace(s"$leftSide$rightSide| failed to match: $boilerDecl $boilerVarName = ...;")
            }
          case unmatchedLine  =>
            System.err.println(s"Was searching for: |$leftSide$rightSide| in boilerplatey code")
            System.err.println(unmatchedLine + Pos())
        }
      }
    }

    def iterateThroughSourceFile(file: String): Unit = {
      var fileLine: String = ""
      var linesInFile: String = ""
      val br: BufferedReader = new BufferedReader(new FileReader(file))
      try {
        // then search through source code
        while ( {fileLine = br.readLine; fileLine} != null) {
          Debug.trace("Starting Loop 2!!!")
          fileLine match {
            case RegEx.SourceDeclExtractor(leftSide, filler, valOrVar, varName, rightSide) =>
              Debug.trace(s"Matched $leftSide$rightSide. In source file (No boilerplate).")
              matchInDesugaredCode(
                reader = in,
                parsedDeclaration = (leftSide, filler, valOrVar, varName, rightSide)
              )
              Debug.trace(s"$leftSide$rightSide is defined. Out of while loop 3")
            case unmatchedLine =>
              linesInFile += unmatchedLine + File.separator // append without modification
          }
          // if the line is a declaration, insert the type (increment the desugared file until you get a match)
        }
      } finally {
        if (br != null) {
          br.close()
          Debug.trace("closed br")
        }
      }
    }

    // first search through desugared code
    while ( {line = in.readLine; line} != null) {
      Debug.trace("Starting Loop 1!!!")
      line match {
        //  // Tester.java
        case RegEx.ScalaFileExtractor(fileName) =>
          Debug.trace("file: " + fileName)
          val pathNullable: Path = FileFinder.tryFindAbsolutePathOfFileWhoseNameIs(fileName, FileFinder.WORKING_DIRECTORY)
          val pathOption: Option[Path] = Option(pathNullable) // None if path is null
          pathOption match {
            case Some(path) =>
              Debug.trace("Some path: " + path.toString)
              val currentFile = path.toString
              iterateThroughSourceFile(file = currentFile)
            //JavaMain.handleFile(path)
            case None => Tester.killApplication(
              s"This file $fileName was supposed to exist, but could not be found in " + FileFinder.WORKING_DIRECTORY
            )
          }
        // Find wile with name: fullFileName in project.
        case other => println(other + "--NO_MATCH" + Pos()) //  // Hello.scala
      }
    }
    Debug.trace("Done. Out of while loop 1")
    in.close
  }
}
