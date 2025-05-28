











## Making GraalVM native image

```bash
# 1. generate reachability-metadata.json for optimized image
# enable //"-agentlib:native.. and then run:
./mill -i cli.run -i .\site1\ --no-cache
# 2. generate native image
./mill -i cli.nativeImage2
```









