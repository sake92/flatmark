







## Generating GraalVM native image metadata

This is needed when there are changes in the dependencies.

```bash
./mill -i cli.assembly

graalvm/java -agentlib:native-image-agent=config-output-dir=cli/resources/META-INF/native-image --sun-misc-unsafe-memory-access=allow -jar out/cli/assembly.dest/out.jar serve -i examples/multilang --no-cache
# now play with the app, change files, trigger some actions, etc.
# then kill the process and it will generate the metadata

./mill -i cli.nativeImage2
```









