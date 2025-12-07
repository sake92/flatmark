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
    
    // Single pass through the lines
    val lines = templateRaw.split('\n')
    var i = 0
    boundary {
      while (i < lines.length) {
        val line = lines(i).trim
        if (line.nonEmpty) {
          if (line == "---") {
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
    
    if (hasYamlFrontMatter) {
      val yaml = lines.slice(firstTripleDashIndex + 1, secondTripleDashIndex).mkString("\n")
      val content = lines.drop(secondTripleDashIndex + 1).mkString("\n").trim
      (yaml, content)
    } else ("", templateRaw)
  }
}
