package com.example

import java.io.{BufferedReader, File, FileReader, FileWriter}
import java.nio.file.Path

/**
  * Created by johnreed on 6/5/16.
  */
object JunkCode {
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
}
