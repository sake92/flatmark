package ba.sake.flatmark

@main def app() =
  println(
    NodejsInterop.highlightCode("val x = 1", Some("scala"))
  )
  println(
    NodejsInterop.highlightMath("x = 5")
  )
  println(
    NodejsInterop.renderGraphviz("digraph G {Hello->World}")
  )
