package ba.sake.flatmark

import scala.jdk.CollectionConverters.*
import io.undertow.util.QueryParameterUtils
import org.slf4j.LoggerFactory
import ba.sake.querson.*

object ThemeResolver {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  /** @return the folder that contains theme */
  def resolve(
      themeSource: String,
      localThemesFolder: os.Path,
      themesCacheFolder: os.Path,
      updateTheme: Boolean
  ): os.Path = {
    val parsedUri = java.net.URI.create(themeSource)
    parsedUri.getScheme match {
      case null =>
        val folder = localThemesFolder / os.SubPath(themeSource)
        logger.debug(s"Using local theme folder: ${folder}")
        if !os.exists(folder) then
          throw FlatmarkException(s"Local theme folder does not exist. Please create it or use a valid theme URL.")
        folder
      case "http" | "https" =>
        logger.debug(s"Using remote theme from URL: ${themeSource}")
        val qp = QueryParameterUtils
          .parseQueryString(parsedUri.getQuery, "utf-8")
          .asScala
          .map((k, v) => k -> v.asScala.toSeq)
          .toMap
          .parseQueryStringMap[ThemeUrlQP]
        val themeHash =
          s"${parsedUri.getScheme}-${parsedUri.getHost}${parsedUri.getPath}-${HashUtils.generate(themeSource)}"
            .replace("/", "-")
        val themeRepoFolder = themesCacheFolder / themeHash
        if os.exists(themeRepoFolder) then {
          if updateTheme then
            logger.info(s"Updating theme from ${themeSource}...")
            os.call(("git", "pull"), cwd = themeRepoFolder)
            logger.info(s"Pulled latest theme")
          else logger.debug(s"Theme is already downloaded. Skipping update.")
        } else {
          val httpCloneUrl = s"${parsedUri.getScheme}://${parsedUri.getHost}${parsedUri.getPath}.git"
          logger.info(s"Downloading theme...")
          // TODO fallback to ssh and api
          os.makeDir.all(themesCacheFolder)
          os.call(
            ("git", "clone", "--depth", "1", "--branch", qp.branch, httpCloneUrl, themeHash),
            cwd = themesCacheFolder
          )
          logger.info(s"Cloned theme")
        }
        themeRepoFolder / os.RelPath(qp.folder)
      case other =>
        throw FlatmarkException(
          s"Unsupported theme URL scheme: ${other}. Only 'http', 'https' or no scheme (folder in _themes) are supported."
        )
    }
  }

  case class ThemeUrlQP(
      branch: String = "main",
      folder: String = "."
  ) derives QueryStringRW
}
