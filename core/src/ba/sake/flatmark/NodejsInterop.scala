package ba.sake.flatmark

import java.time.Instant
import org.graalvm.polyglot.*
// https://kmsquare.in/blog/running-graaljs-and-optimizing-for-performance/
// TODO optimize umjesto contexta reusat engine

object NodejsInterop {
  println("Initializing GraalVM Node.js interop...")
  private lazy val context = Context
    .newBuilder("js", "wasm")
    .allowAllAccess(true)
    .allowPolyglotAccess(PolyglotAccess.ALL)
    .allowExperimentalOptions(true)
    .option("js.webassembly", "true")
    .option("js.esm-eval-returns-exports", "true")
    .build()

// TODO loadanje je sporooooooo
  private lazy val nodejsBundleSource = Source.newBuilder("js", getClass.getClassLoader.getResource("bundle.min.mjs")).build
  //println("GraalVM Node.js loading script.. " + Instant.now())

  private lazy val nodejsModule = context.eval(nodejsBundleSource)

  //println("GraalVM Node.js interop initialized. " + Instant.now())

  def highlightCode(codeStr: String, codeLang: Option[String] = None): String =
    codeLang match
      case Some(lang) =>
        nodejsModule.getMember("highlightCode").execute(codeStr, lang).asString()
      case None =>
        nodejsModule.getMember("highlightCodeAuto").execute(codeStr).asString()

  def highlightMath(mathStr: String): String =
    val highlightMathFun = nodejsModule.getMember("highlightMath")
    highlightMathFun.execute(mathStr).asString()

  def renderGraphviz(dotStr: String, engine: String = "dot"): String =
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
