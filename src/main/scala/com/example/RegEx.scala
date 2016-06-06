package com.example

import scala.util.matching.Regex

/**
  * Created by johnreed on 6/5/16.
  * A place for regular expressions
  */
object RegEx {
  val ScalaFileExtractor =
    """.*
         \s+
         //
         \s{1}
         ([\w.]+ \.scala)
    """.replaceAll("(\\s)", "").r

  // extracts declarations such as " val cat_!: Cat = makeCat()" or "Foo.bar();var     <><: :Int = 7"
  // or "/*comment*/ var cat: CAT_<>< = makeCat()"
  val DeclExtractor =
    """(\s* | .* ; \s* | .* \s+)
         (val|var)
         \s+
         ([^:;\s]+)
         \s*
         :
         \s*
         ([A-Z]\S*)
         \s*
         =
         .*
         ;
    """.replaceAll("(\\s)", "").r

  /**
    * Same as DeclExtractor, but without the type.
    * Used when the parser does not include a type
    */
  val DeclExtractorNoType =
    """(\s* | .* ; \s* | .* \s+)
         (val|var)
         \s+
         ([^:;\s]+)
         \s*
         =
         .*
         ;
    """.replaceAll("(\\s)", "").r

  // Same, but without the : Type. Semi-colon optional. Extra breaks for right of val/var and left (insert types in breaks)
  // warning, will break for val lala_:_int:Int = 5
  val DeclExtractorNoBoilerplate =
    """((\s* | .* ; \s* | .* \s+)
         (val|var)
         \s+
         ([^:;\s]+))(
         \s*
         =
         .*)
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
}
