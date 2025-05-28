

Current design of the project:
- graalvm for native image
- serving syntax highlighting, diagram rendering trough http and getting it via Selenium
  - works even for mermaidjs, diagrams are plain SVGs
  - but we do need chrome installed!
- file cache for diagrams, code highlighting, etc.



## GraalVM native image

Best for CLIs, snappiest startup time etc.
Need to be careful with reflection, dynamic class loading, etc.


## jpackage

Makes installers for all platforms.
Would be veery nice, easier to distribute to winget, apt, brew, etc.

