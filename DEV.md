

## Making GraalVM native image

```bash
# generate reachability-metadata.json for optimized image
./mill -i cli.run -i .\site1\ --no-cache

./mill -i cli.nativeImage2
```
