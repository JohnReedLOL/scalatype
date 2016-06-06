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
  // Note: desugared type is not necessarily lowercase. Ex. lala.Type
  val TypedDesugaredDeclExtractor =
    """(\s* | .* \s+)
         (val|var)
         \s+
         ([^:;\s]+)
         \s*
         :
         \s*
         (\S*)
         \s*
         =
         .*
         ;{1}
    """.replaceAll("(\\s)", "").r

  /**
    * Same as DeclExtractor, but without the type.
    * Used when the parser does not include a type
    * // getting rid of ".* ; \s* | " for complexity reasons
    */
  val DesugaredDeclExtractor =
    """(\s* | .* \s+)
         (val|var)
         \s+
         ([^:;\s]+)
         \s*
         =
         .*
         ;{1}
    """.replaceAll("(\\s)", "").r

  // Same, but without the : Type. Semi-colon optional. Extra breaks for right of val/var and left (insert types in breaks)
  // warning, will break for val lala_:_int:Int = 5
  // get rid of " .* ; \s* |" for simplicity
  // No colons in source decl (because then it would be a typed decl)
  val SourceDeclExtractor =
    """((\s* | .* \s+)
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
