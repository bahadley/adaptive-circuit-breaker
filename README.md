### Demo of an adaptive circuit breaker

### How do I run this?

Use two terminals on a single host.

In one terminal run:

```
$ sbt -DPORT=2551 -DHOST=127.0.0.1 -Dconfig.resource=/da-node.conf run
```

In the second terminal run:

```
$ sbt -DPORT=2552 -DHOST=127.0.0.1 -Dconfig.resource=/h2-node.conf run
