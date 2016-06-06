package com.example

/**
  * Created by johnreed on 6/6/16.
  */
object Utils {
  def shortenTypeSignature(sig: String): String = {
    val splitString = sig.split(".")
    // Warning: Assumes that all packages are lowercase and that the actual type is uppercase.
    val uppercaseStrings = splitString.filter(str => str.length > 0 && str.charAt(0).isUpper)
    uppercaseStrings.reduceLeft( (left, next) => left + "." + next ) // put dots back in
  }
}
