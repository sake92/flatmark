package ba.sake.flatmark.templates

import ba.sake.flatmark.FrontMatterUtils

import java.io.{File, Reader, StringReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.boundary
import io.pebbletemplates.pebble.error.LoaderException
import io.pebbletemplates.pebble.loader.FileLoader

class YamlSkippingFileLoader(rootFolder: Path) extends FileLoader {

  override def getReader(templateName: String): Reader = {
    val file = getFile(templateName)
    if !file.exists || !file.isFile then {
      throw LoaderException(null, s"Could not find template '${templateName}'")
    }

    val rawContent = Files.readString(file.toPath, StandardCharsets.UTF_8)
    val (_, rawContentNoYaml) = FrontMatterUtils.extract(rawContent)
    new StringReader(rawContentNoYaml)
  }

  private def getFile(originalTemplateName: String) = {
    val normalizedPrefix = Option(getPrefix)
      .getOrElse("")
      .dropWhile(_ == File.separatorChar)
      .reverse
      .dropWhile(_ == File.separatorChar)
      .reverse
    val normalizedTemplateName =
      originalTemplateName.dropWhile(_ == File.separatorChar).reverse.dropWhile(_ == File.separatorChar).reverse
    val normalizedSuffix = Option(getSuffix).getOrElse("")
    val templateName = (if normalizedPrefix.isEmpty then "" else s"${normalizedPrefix}/") +
      normalizedTemplateName + (if normalizedSuffix.isEmpty then "" else normalizedSuffix)
    rootFolder.resolve(templateName).toFile
  }
}
