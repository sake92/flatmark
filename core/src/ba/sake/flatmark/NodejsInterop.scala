package ba.sake.flatmark

import java.security.MessageDigest
import java.util.Base64
import org.graalvm.polyglot.*

class NodejsInterop(cacheFolder: os.Path) {

  private lazy val context = Context
    .newBuilder("js", "wasm")
    .allowAllAccess(true)
    .allowPolyglotAccess(PolyglotAccess.ALL)
    .allowExperimentalOptions(true)
    .option("js.webassembly", "true")
    .option("js.esm-eval-returns-exports", "true")
    .build()

  private lazy val nodejsBundleSource =
    Source.newBuilder("js", getClass.getClassLoader.getResource("bundle.min.mjs")).build
  private lazy val nodejsModule = context.eval(nodejsBundleSource)

  def highlightCode(codeStr: String, codeLang: Option[String] = None): String =
    withFileCache(s"highlightCode-${codeLang.getOrElse("unknown")}-${getMd5B64(codeStr)}") {
      codeLang match
        case Some(lang) =>
          nodejsModule.getMember("highlightCode").execute(codeStr, lang).asString()
        case None =>
          nodejsModule.getMember("highlightCodeAuto").execute(codeStr).asString()
    }

  def highlightMath(mathStr: String): String =
    withFileCache(s"highlightMath-${getMd5B64(mathStr)}") {
      val highlightMathFun = nodejsModule.getMember("highlightMath")
      highlightMathFun.execute(mathStr).asString()
    }

  def renderGraphviz(dotStr: String, engine: String = "dot"): String =
    withFileCache(s"renderGraphviz-${engine}-${getMd5B64(dotStr)}") {
      val renderGraphvizFun = nodejsModule.getMember("renderGraphviz")
      renderGraphvizFun.execute(dotStr, engine).asString()
    }

  private def withFileCache(cacheKey: String)(code: => String) = {
    val cachedResultFileName = cacheFolder / "cached-results" / s"${cacheKey}.txt"
    if os.exists(cachedResultFileName) then {
      os.read(cachedResultFileName)
    } else {
      val finalResult = code
      os.write.over(cachedResultFileName, finalResult, createFolders = true)
      finalResult
    }
  }

  private def getMd5B64(str: String): String = {
    val bytesOfMessage = str.getBytes("UTF-8")
    val md = MessageDigest.getInstance("MD5")
    val theMD5digest = md.digest(bytesOfMessage)
    val b64 = Base64.getEncoder.encode(theMD5digest)
    new String(b64, "UTF-8").replace('/', '-').replace('=', '_').replace('+', '$')
  }
}

// AKO NEKAD ZATREBA PROMISE/ASYNC
/*
import java.util.concurrent.CompletionStage
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer
val queue = new LinkedBlockingQueue[AnyRef]()
    val thenn: Consumer[Object] = (v: Object) => {
      queue.add(v)
    }
    promiseValue.invokeMember("then", thenn)
    val result = queue.take()
 */
