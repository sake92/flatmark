package ba.sake.flatmark.generation

import ba.sake.flatmark.{FlatmarkException, PageConfig, YamlInstances, parseConfig}
import org.slf4j.LoggerFactory
import org.virtuslab.yaml.*

private[generation] final case class SiteSources(
    contentFolder: os.Path,
    files: Seq[os.Path],
    pageConfigs: Map[os.Path, PageConfig],
    dataYamls: Map[String, Node]
)

private[generation] object SiteSourceLoader {
  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val SupportedSortOrders = Set("publish_date", "-publish_date", "title", "-title")

  def load(siteRootFolder: os.Path): SiteSources = {
    val contentFolder = siteRootFolder / "content"
    val files = loadContentFiles(contentFolder)
    val pageConfigs = files.map { file =>
      val config = parseConfig(file, os.read(file))
      validatePageConfig(file, config)
      file -> config
    }.toMap
    SiteSources(contentFolder, files, pageConfigs, loadData(siteRootFolder / "_data"))
  }

  private def loadContentFiles(contentFolder: os.Path): Seq[os.Path] = {
    if !os.exists(contentFolder) then {
      logger.warn("The 'content/' folder does not exist, skipping content processing.")
      Seq.empty
    } else {
      if !os.isDir(contentFolder) then
        throw FlatmarkException(s"The 'content/' folder is not a folder: ${contentFolder}")
      if os.list(contentFolder).isEmpty then logger.warn("The 'content/' folder is empty, no content to process.")
      os.walk(contentFolder, skip = _.segments.exists(segment => segment.startsWith(".") || segment.startsWith("_")))
        .filter(file => os.isFile(file) && (file.ext == "md" || file.ext.endsWith("html")))
        .sorted
    }
  }

  private def loadData(dataFolder: os.Path): Map[String, Node] =
    if !os.exists(dataFolder) then Map.empty
    else {
      import YamlInstances.given
      os.list(dataFolder)
        .filter(file => file.ext == "yaml" && os.isFile(file))
        .sorted
        .map { file =>
          val content = os.read(file)
          val node =
            if content.isBlank then Node.MappingNode(Map.empty)
            else
              content.as[Node].getOrElse {
                throw FlatmarkException(s"Invalid YAML format in file: ${file}. Expected YAML data.")
              }
          file.baseName -> node
        }
        .toMap
    }

  private def validatePageConfig(file: os.Path, config: PageConfig): Unit = {
    if config.pagination.per_page <= 0 then
      throw FlatmarkException(s"Invalid pagination.per_page in '${file}': expected a positive number.")
    if !SupportedSortOrders(config.pagination.sort_by) then
      throw FlatmarkException(
        s"Unsupported sort order '${config.pagination.sort_by}' in '${file}'. Supported: ${SupportedSortOrders.toSeq.sorted
            .mkString(", ")}."
      )
    config.ext.foreach { extension =>
      if extension.isBlank || extension == "." || extension == ".." || extension.exists(ch => ch == '/' || ch == '\\')
      then throw FlatmarkException(s"Invalid output extension '${extension}' in '${file}': expected one path segment.")
    }
  }
}
