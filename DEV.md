

## Making GraalVM native image

```bash
# 1. if dependencies has changed since last reachability-metadata.json generation:
export FORK_ARGS="-agentlib:native-image-agent=config-output-dir=cli/resources/META-INF/native-image"

# 2. generate reachability-metadata.json for optimized image:
./mill -i cli.run -i examples/multilang --no-cache

# 3. generate native image:
./mill -i cli.nativeImage2
```









