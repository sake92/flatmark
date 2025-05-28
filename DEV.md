

## Making GraalVM native image

```bash
# prepare the reachability-metadata.json for optimized image
./mill -i cli.run --no-cache

./mill -i cli.nativeImage2
```
