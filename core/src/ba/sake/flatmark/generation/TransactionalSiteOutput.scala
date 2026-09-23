package ba.sake.flatmark.generation

import java.util.UUID
import scala.util.control.NonFatal

private[generation] object TransactionalSiteOutput {
  def publish(siteRootFolder: os.Path)(build: os.Path => Unit): Unit = {
    val outputFolder = siteRootFolder / "_site"
    val buildsFolder = siteRootFolder / ".flatmark-cache" / "builds"
    val id = UUID.randomUUID().toString
    val stagingFolder = buildsFolder / s"site-${id}"
    val backupFolder = buildsFolder / "previous-site"
    os.makeDir.all(buildsFolder)
    recoverInterruptedPublish(outputFolder, backupFolder)
    os.makeDir.all(stagingFolder)

    var previousMoved = false
    var published = false
    try {
      build(stagingFolder)
      if os.exists(outputFolder) then {
        os.move(outputFolder, backupFolder, createFolders = true)
        previousMoved = true
      }
      try {
        os.move(stagingFolder, outputFolder, createFolders = true)
        published = true
      } catch {
        case NonFatal(error) =>
          if previousMoved && !os.exists(outputFolder) && os.exists(backupFolder) then
            os.move(backupFolder, outputFolder, createFolders = true)
          throw error
      }
      if previousMoved then os.remove.all(backupFolder)
    } finally {
      if os.exists(stagingFolder) then os.remove.all(stagingFolder)
      if published && os.exists(backupFolder) then os.remove.all(backupFolder)
    }
  }

  private def recoverInterruptedPublish(outputFolder: os.Path, backupFolder: os.Path): Unit =
    if os.exists(backupFolder) then
      if os.exists(outputFolder) then os.remove.all(backupFolder)
      else os.move(backupFolder, outputFolder, createFolders = true)
}
