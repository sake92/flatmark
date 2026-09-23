package ba.sake.flatmark.generation

import ba.sake.flatmark.{FlatmarkException, SiteConfig, YamlInstances}
import org.slf4j.LoggerFactory
import org.virtuslab.yaml.*

import scala.jdk.CollectionConverters.*

private[generation] object SiteConfigLoader {
  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val DefaultSiteConfig = "name: My Site"

  def load(siteRootFolder: os.Path): SiteConfig = {
    val configFile = siteRootFolder / "_config.yaml"
    val yaml =
      if os.exists(configFile) then
        os.read(configFile).trim match {
          case ""      => DefaultSiteConfig
          case content => content
        }
      else DefaultSiteConfig

    import YamlInstances.given
    val decoded = yaml
      .as[SiteConfig]
      .fold(
        error =>
          throw FlatmarkException(s"Invalid site config in file: ${configFile}. Expected SiteConfig format.", error),
        identity
      )
    val baseUrl = decoded.base_url
      .orElse(System.getenv().asScala.get("FLATMARK_BASE_URL").filterNot(_.isBlank))
      .map(_.stripSuffix("/"))
    val config = decoded.copy(base_url = baseUrl)
    logger.debug(s"Site configuration: ${config}")
    config
  }
}
