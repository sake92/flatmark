











## Making GraalVM native image

```bash
# if dependencies has changed since last reachability-metadata.json generation,
# 1. enable "-agentlib:native" in build.mill
# 2. generate reachability-metadata.json for optimized image
./mill -i cli.run -i .\site1\ --no-cache

# 3. generate native image
./mill -i cli.nativeImage2
```









