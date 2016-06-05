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

    val DummyRegEx: Regex = """(.*)""".r

    val Process = """([a-cA-C])([^\s]+)""".r // define first, rest is non-space
    for (p <- Process findAllIn "baha bah Cah zzzzzzAahr") p match {
      case Process("b", rest) => println("first: 'b', some rest " + rest) // b aha, b ha
      case Process(first, rest) => println("some " + first + ", rest: " + rest) // C ah, some A, rest: ahr
      // etc.
    }


    val MY_RE = "(foo|bar).*".r
    val result = "foo123" match {
      case MY_RE(m) => m;
      case _ => "No match"
    }
    val MY_RE2 = "(foo|bar)".r
    val result2 = "foo123" match {
      case MY_RE2(m) => m;
      case _ => "No match"
    }
    Debug.trace(result)
    Debug.trace(result2)

    // optional leading characters [non-whitespace]
    // leading whitespace [1 or more]
    // double forward slash
    // one whitespace character
    // file name (one or more characters terminated by .java or .scala)
    val ScalaFileRegEx =
      """.*
         \s+
         //
         \s{1}
         ([\w.]+ \.scala)
      """.replaceAll("(\\s)", "").r
    val BookExtractorRE: Regex =
      """([^\.j]+) \.j (val|var) \s+
         ([^,]+) \s{1} author= (.+)""".replaceAll("(\\s)", "").r // <1>
    val MagazineExtractorRE: Regex =
      """([^,]+),\s+issue=(.+)""".r

    val catalog = Seq(
      "title=Programming Scala Second Edition.jval  booboo author=Dean Wampler",
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
    /*
    val declarationExtractorRegEx = raw"^(" +
      raw"\s*" +
      raw"" +
      raw"$"
    */

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
      case ScalaFileRegEx(fileName) => println("file: " + fileName)
      case other => println(other + "--NO_MATCH")
    }
    //System.exit(-1)

    println("Working directory: " + FileFinder.WORKING_DIRECTORY)
    FileFinder.setMySearchDepth(20)

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

    var currentLine = 0 // start at zero and go up
    var currentFile = "" // start with no file.
    var currentLineDesugared = 0
    while ( {
      line = in.readLine; line
    } != null) {
      //System.out.println(line)
      line match {
        //  // Tester.java
        case ScalaFileRegEx(fileName) => {
          println("file: " + fileName + "\n\n")
          val pathNullable: Path = FileFinder.tryFindAbsolutePathOfFileWhoseNameIs(fileName, FileFinder.WORKING_DIRECTORY)
          val pathOption: Option[Path] = Option(pathNullable) // None if path is null
          pathOption match {
            case Some(path) => {
              println("Some path: " + path.toString)
              currentFile = path.toString
              currentLine = 0

              var line: String = null
              val br: BufferedReader = new BufferedReader(new FileReader(currentFile))
              try {
                while ( {
                  line = br.readLine;
                  line
                } != null) {}
              } finally {
                if(br != null) {br.close()}
              }
              println("Done!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
              //JavaMain.handleFile(path)
            }
            case None => println("No path")
          }
          // Find wile with name: fullFileName in project.
        }
        case other => println(other + "--NO_MATCH") //  // Hello.scala
      }
      currentLineDesugared += 1
    }
    System.out.println("Done1")
    in.close
  }
}
