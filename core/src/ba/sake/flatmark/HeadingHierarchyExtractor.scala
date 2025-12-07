package ba.sake.flatmark

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.boundary
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object HeadingHierarchyExtractor {

  case class Heading(
      level: Int,
      text: String,
      id: String,
      children: ListBuffer[Heading] = ListBuffer()
  ) {
    override def toString: String =
      ("  " * level) + s"<h${level}>${text}\n" + children.mkString
  }

  def extract(doc: Document): List[Heading] = {
    val topLevelHeadings = ListBuffer[Heading]()

    // This will hold the current "parent" for each heading level.
    // Index 0 for H1, index 1 for H2, etc. Max level is 6, so size 6.
    // Using Array[Heading | Null] for better performance
    val currentParents: Array[Heading | Null] = new Array[Heading | Null](6)

    // Select all heading tags in document order
    val allHeadings = doc.select("h1, h2, h3, h4, h5, h6").asScala // Convert Java Elements to Scala Seq

    for (headingElement <- allHeadings) {
      val tagName = headingElement.tagName() // e.g., "h1", "h2"
      val level = tagName.charAt(1) - '0' // Extract level (1-6) more efficiently

      val newHeading = Heading(level, headingElement.text(), headingElement.attr("id").trim)

      if (level == 1) {
        // H1 is always a top-level heading
        topLevelHeadings += newHeading
        currentParents(0) = newHeading // Set H1 as the current parent for H1s
        // Reset parents for lower levels
        var i = 1
        while (i < 6) {
          currentParents(i) = null
          i += 1
        }
      } else {
        // Find the appropriate parent for this heading
        // Iterate upwards from the previous level
        var parentFound: Heading | Null = null
        var i = level - 2
        while (i >= 0 && parentFound == null) {
          if (currentParents(i) != null) {
            parentFound = currentParents(i)
          }
          i -= 1
        }

        if (parentFound != null) {
          parentFound.children += newHeading
        } else {
          // If no higher-level parent is found, it's a top-level heading
          // (e.g., an H2 without a preceding H1)
          topLevelHeadings += newHeading
        }

        // Set this heading as the current parent for its own level
        currentParents(level - 1) = newHeading
        // Reset parents for lower levels (any subsequent Hx that are children of this one)
        var j = level
        while (j < 6) {
          currentParents(j) = null
          j += 1
        }
      }
    }
    topLevelHeadings.toList // Convert ListBuffer to immutable List
  }

  // TODO extract to test
  def main(args: Array[String]): Unit = {
    val html = """<html><body>
                 |<h1>Main Title</h1>
                 |  <h2>Section 1</h2>
                 |    <h3>Subsection 1.1</h3>
                 |      <h4>Sub-subsection 1.1.1</h4>
                 |    <h3>Subsection 1.2</h3>
                 |  <h2>Section 2</h2>
                 |    <h3>Subsection 2.1</h3>
                 |
                 |<h1>Another Main Title</h1>
                 |  <h2>Section 3</h2>
                 |    <h3>Subsection 3.1</h3>
                 |  <h2>Section 4</h2>
                 |</body></html>""".stripMargin

    val doc: Document = Jsoup.parse(html)
    val hierarchy = extract(doc)

    println("Extracted Heading Hierarchy:")
    println(hierarchy.mkString("\n\n\n"))
  }
}
