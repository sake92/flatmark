package ba.sake.flatmark

import scala.util.boundary

// TODO test
object FrontMatterUtils {

  /** Extracts the YAML front matter from the given template (markdown or whatever). Returns a tuple of (raw YAML front
    * matter, content without front matter).
    */
  def extract(templateRaw: String): (String, String) = {
    var hasYamlFrontMatter = false
    var firstTripleDashIndex = -1
    var secondTripleDashIndex = -1
    boundary {
      val iter = templateRaw.linesIterator
      var i = 0
      while iter.hasNext do {
        val line = iter.next().trim
        if line.nonEmpty then {
          if line == "---" then {
            if (firstTripleDashIndex == -1) firstTripleDashIndex = i
            else if (secondTripleDashIndex == -1) {
              secondTripleDashIndex = i
              hasYamlFrontMatter = true
              boundary.break()
            }
          } else if (firstTripleDashIndex == -1) {
            boundary.break() // first non-empty line is not triple dash -> no YAML front matter
          }
        }
        i += 1
      }
    }
    if hasYamlFrontMatter then {
      val yaml = templateRaw.linesIterator
        .slice(firstTripleDashIndex + 1, firstTripleDashIndex + 1 + secondTripleDashIndex - firstTripleDashIndex - 1)
        .mkString("\n")
      val content = templateRaw.linesIterator
        .drop(secondTripleDashIndex + 1)
        .mkString("\n")
        .trim
      (yaml, content)
    } else ("", templateRaw)
  }
}
