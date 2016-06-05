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
         (\S+)
         \s*
         :
         \s*
         ([A-Z]\S*)
         \s*
         =
         .*
         ;
    """.replaceAll("(\\s)", "").r

  // Same, but without the : Type. Semi-colon optional.
  val DeclExtractorNoBoilerplate =
    """(\s* | .* ; \s* | .* \s+)
         (val|var)
         \s+
         (\S+)
         \s*
         =
         .*
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
