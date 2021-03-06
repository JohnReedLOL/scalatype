package com.example

import java.io._
import java.nio.file.Path
import java.util._

import scala.trace.{Debug, Pos, SDebug}
import info.collaboration_station.utilities.{FileFinder, GlobFinder, Tester}

import scala.util.matching.Regex


object Hello {

  //note a single Random object is reused here
  val randomGenerator = new Random();

  def generateRandomName(): String = {
    val randomInt = randomGenerator.nextInt();
    "typedFile" + randomInt + ".txt"
  }

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
    val vvvv: Utils.type = Utils

    val proc = Runtime.getRuntime.exec(Array[String]("sbt", "clean", "set scalacOptions in ThisBuild ++= Seq(\"-print\")", "compile", "exit"))
    val in = new BufferedReader(new InputStreamReader(proc.getInputStream))
    var line: String = null

    Thread.sleep(100)

    Debug.trace("Working directory: " + FileFinder.WORKING_DIRECTORY)
    FileFinder.setMySearchDepth(20)

    type FiveStrings = (String, String, String, String, String)
    /** Match the parsed declaration from the source code with the matching declaration in
      * the desugared code (read in with the reader) to get a typed declaration.
      * @param reader Reads in the desugared code to match against
      * @param parsedDeclaration the parsed declaration from source to match against
      * the desugared code.
      * @return The parsedDeclaration with a type inserted or None
      */
    def getTypedDeclaration(reader: BufferedReader, parsedDeclaration: FiveStrings): Option[String] = {
      val (leftSide, filler, valOrVar, varName, rightSide) = parsedDeclaration
      var matchesDeclaration = false
      var toReturn: Option[String] = None
      System.err.println(s"Looking for $valOrVar $varName$rightSide" + Pos())
      while (matchesDeclaration == false && line != null) {
        line = reader.readLine
        // Debug.trace("Starting Loop 3!!!")
        line match {
          case RegEx.TypedDesugaredDeclExtractor(boilerFiller, boilerValOrVar, boilerVarName, boilerRefType) =>
            val shortenedSig = vvvv.shortenTypeSignature(boilerRefType) // remove path.to.Type
            if(boilerValOrVar.equals(valOrVar) && boilerVarName.equals(varName)) {
              System.err.println(s"Found Typed $boilerValOrVar $boilerVarName: $boilerRefType" + Pos())
              // Debug.trace(s"$leftSide$rightSide| matched: $boilerValOrVar $boilerVarName: $boilerRefType = ...;")
              toReturn = Some(s"$leftSide: $shortenedSig$rightSide") // put in type
              // Debug.trace(s"Returning:: $leftSide: $boilerRefType$rightSide")
              matchesDeclaration = true // exit
            } else {
              // Debug.trace(s"$leftSide$rightSide| failed to match: $boilerValOrVar $boilerVarName: $boilerRefType = ...;")
            }
          case RegEx.DesugaredDeclExtractor(boilerFiller, boilerDecl, boilerVarName) =>
            if(boilerDecl.equals(valOrVar) && boilerVarName.equals(varName)) {
              System.err.println(s"Found (no type) $boilerDecl $boilerVarName" + Pos())
              // they matched, but no need to insert a type
              // Debug.trace(s"$leftSide$rightSide| matched: $boilerDecl $boilerVarName = ...;")
              toReturn = None // None can also be used if no type was inserted in desugared code
              matchesDeclaration = true // exit
            } else {
              // Debug.trace(s"$leftSide$rightSide| failed to match: $boilerDecl $boilerVarName = ...;")
            }
          case unmatchedLine  =>
            System.err.println(s"Unmatched: $unmatchedLine")
            toReturn = None // just to be explicit
        }
      }
      toReturn
    }

    /** Iterate through the source file in the SBT project with the given name.
      * Find each of its declarations and type them.
      * @param fileName the file name of the given file in the SBT project
      * @return the source file as one long strings with types put into the declarations.
      */
    def iterateThroughSourceFile(fileName: String): String = {
      val fileReader = new BufferedReader(new FileReader(fileName))
      var outputFile: String = "" // the string that will be output to disk
      try {
        var line: String = ""
        // then search through source code
        while ( {line = fileReader.readLine; line} != null) {
          // Debug.trace("Starting Loop 2!!!")
          line match {
            case RegEx.SourceDeclExtractor(leftSide, filler, valOrVar, varName, rightSide) =>
              // Debug.trace(s"Matched $leftSide$rightSide. In source file (No boilerplate).")
              val toAppend: Option[String] = getTypedDeclaration(
                reader = in,
                parsedDeclaration = (leftSide, filler, valOrVar, varName, rightSide)
              )
              toAppend match {
                case Some(typedDeclaration) =>
                  // Debug.trace("Typed declaration: " + typedDeclaration)
                  outputFile += typedDeclaration + "\n"
                case None => outputFile += line + "\n"
              }
              // Debug.trace(s"$leftSide$rightSide is defined. Out of while loop 3")
            case _ =>
              outputFile += line + "\n" // append without modification
          }
          // if the line is a declaration, insert the type (increment the desugared file until you get a match)
        }
        // write it to disk
        val outputFileName = generateRandomName()
        val fw = new FileWriter(outputFileName)
        fw.write(outputFile)
        fw.close
        Debug.trace("Wrote file " + outputFileName)
      } finally {
        if (fileReader != null) {
          fileReader.close()
          Debug.trace("closed br")
        }
      }
      outputFile
    }

    // first search through desugared code
    while ( {line = in.readLine; line} != null) {
      // Debug.trace("Starting Loop 1!!!")
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
              iterateThroughSourceFile(fileName = currentFile)
            //JavaMain.handleFile(path)
            case None => Tester.killApplication(
              s"This file $fileName was supposed to exist, but could not be found in " + FileFinder.WORKING_DIRECTORY
            )
          }
        // Find wile with name: fullFileName in project.
        case other => // println(other + "--NO_MATCH" + Pos()) //  // Hello.scala
      }
    }
    Debug.trace("Done. Out of while loop 1")
    in.close
  }
}
