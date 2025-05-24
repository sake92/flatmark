package ba.sake.flatmark

import java.nio.file.Paths
import org.graalvm.polyglot.*
import org.graalvm.polyglot.proxy.*

object NodejsInterop {
  private val context = Context
    .newBuilder("js", "wasm")
    .allowAllAccess(true)
    .allowPolyglotAccess(PolyglotAccess.ALL)
    .allowExperimentalOptions(true)
    .option("js.webassembly", "true")
    .option("js.esm-eval-returns-exports", "true")
    .build()

  private val nodejsBundleSource = Source.newBuilder("js", getClass.getClassLoader.getResource("bundle.min.mjs")).build
  private val nodejsModule = context.eval(nodejsBundleSource)

  def highlightCode(codeStr: String, codeLang: Option[String] = None) =
    codeLang match
      case Some(lang) =>
        nodejsModule.getMember("highlightCode").execute(codeStr, lang).asString()
      case None =>
        nodejsModule.getMember("highlightCodeAuto").execute(codeStr).asString()

  def highlightMath(mathStr: String) =
    val highlightMathFun = nodejsModule.getMember("highlightMath")
    highlightMathFun.execute(mathStr).asString()

  def renderGraphviz(dotStr: String, engine: String = "dot") =
    val renderGraphvizFun = nodejsModule.getMember("renderGraphviz")
    renderGraphvizFun.execute(dotStr, engine).asString()

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
