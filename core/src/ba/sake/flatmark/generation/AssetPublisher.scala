package ba.sake.flatmark.generation

import ba.sake.flatmark.FlatmarkException
import org.slf4j.LoggerFactory

import scala.util.Properties

private[generation] final class AssetPublisher(
    siteRootFolder: os.Path,
    outputFolder: os.Path,
    themeFolder: Option[os.Path]
) {
  private val logger = LoggerFactory.getLogger(getClass.getName)

  def publishThemeStatic(): Unit =
    themeFolder.foreach(folder => copyFolder(folder / "static"))

  def publishSiteStatic(): Unit =
    copyFolder(siteRootFolder / "static")

  def compileSass(): Unit = {
    val sassFolder = siteRootFolder / "_sass"
    if os.exists(sassFolder) then {
      logger.debug("Compiling Sass files to _site/styles...")
      val executable = if Properties.isWin then "sass.bat" else "sass"
      val stderr = new StringBuilder
      val result = os.call(
        (executable, s"${sassFolder}:${outputFolder}/styles"),
        cwd = siteRootFolder,
        stdout = os.ProcessOutput.Readlines(_ => ()),
        stderr = os.ProcessOutput.Readlines(line => stderr.append(line).append('\n')),
        timeout = 120000,
        check = false
      )
      if result.exitCode != 0 then
        throw FlatmarkException(s"Sass compilation failed with exit code ${result.exitCode}:\n${stderr.result()}")
    }
  }

  private def copyFolder(folder: os.Path): Unit =
    if os.exists(folder) then
      os.walk(folder).foreach { file =>
        if os.isFile(file) then
          os.copy(
            file,
            outputFolder / file.relativeTo(folder),
            replaceExisting = true,
            createFolders = true,
            mergeFolders = true,
            followLinks = false
          )
      }
}
