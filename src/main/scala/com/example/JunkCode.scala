package com.example

import java.io.{BufferedReader, File, FileReader, FileWriter}
import java.nio.file.Path

import scala.trace.Debug

/**
  * Created by johnreed on 6/5/16.
  */
object JunkCode {

  def succ(i: Int, j: Int): Int = i + j + 1

  case class Foo() {
    def succ = 8
    def succ(i: Int) = i + 1
  }
  val f = new Foo()
  val v = f.succ
  val x = 7;

  /*
  def handleFile(path: Path) {
    val log: File = new File(path.toString)
    val search: String = "textFiles/a.txt"
    val replace: String = "replaceText/b.txt"
    try {
      val fr: FileReader = new FileReader(log)
      var s: String = null
      var totalStr: String = ""
      try {
        val br: BufferedReader = new BufferedReader(fr)
        try {
          while ({s = br.readLine; s} != null) {
            {
              totalStr += s
            }
          }
          // this is going to have to be subsituted for a gradual replace strategy.
          totalStr = totalStr.replaceAll(search, replace)
          val fw: FileWriter = new FileWriter(log)
          fw.write(totalStr)
          fw.close
        } finally {
          if (br != null) br.close()
        }
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }


  val string1 = " // Tester.java"
  val string2 = " // Hello.scala"
  // [[syntax trees at end of                    parser]] // JavaMain.java--NO_MATCH
  /*
  string1 match {
    case ScalaFileRegEx((fileName1 @ _), (suffix @ _)) => println("file: " + fileName1 + " suffix: " + suffix)
    case other => println(other + "--NO_MATCH")
  }*/
  string2 match {
    case RegEx.ScalaFileExtractor(fileName) => Debug.trace("file: " + fileName)
    case other => Debug.trace(other + "--NO_MATCH")
  }
  type CAT_<>< = Int
  def makeCat() = 8
  /*comment*/ var <><:><> : CAT_<>< = makeCat() // if it contains a colon the colon must be after a whitespace.
  val string3 = " /*comment*/   var cat:CAT_<><  = makeCat() ;"
  string3 match {
    case RegEx.DeclExtractor(filler, decl, varName, refType) => Debug.trace(s"$decl $varName $refType")
    case _ => Debug.trace("Fail")
  }
  val string4 = " var   cat  = makeCat();"
  string4 match {
    case RegEx.DeclExtractorNoBoilerplate(leftSide, filler, decl, varName, rightSide) =>
      Debug.trace(s"|$leftSide\\|$filler\\|$decl\\|$rightSide\\|$varName\\|")
    case _ => Debug.trace("Fail2")
  }
  // System.exit(-1)

  // The desugared code doesn't have a type on it.
  val testInput = """      val vvv = new GlobFinder("foo");"""
  testInput match {
    case RegEx.DeclExtractor(boilerFiller, boilerDecl, boilerVarName, boilerRefType) =>
      Debug.trace(s"$boilerFiller$boilerDecl $boilerVarName: $boilerRefType")
    case unmatchedInput => Debug.trace(unmatchedInput) //       val vvv = new GlobFinder("foo");
  }

  */
}
