# light-chaos-monkey
Chaos Monkey handlers for light ecosystem. Inspired by Chaos Engineering at Netflix and Chaos Monkey for Spring Boot.


### The Goal of Chaos Monkey

While helping my customers to build large scale distributed systems in light-4j, I constantly receive the following questions:

* Will the fallback work?
* How would the application behave with network latency?
* What will happen if one of the service instances is down?
* How does the service discovery help during service deployment?

As you can see, there are a lot of questions to be answered to make our customers comfortable to rollout distributed applications.

These questions led me to dive into [Chaos Engineering](https://principlesofchaos.org/) and started this project to share my thoughts and experience.

### How does it work?

As we know, the light-4j framework is using middleware handlers to address cross-cutting concerns in the request/response chain in the same service instance or at the network level with [light-router](https://github.com/networknt/light-router), [light-proxy](https://github.com/networknt/light-proxy) or [light-mesh/http-sidecar](https://github.com/networknt/light-mesh).

To simulate some of the Chaos Monkey behaviours, we built several middleware handlers that can be injected into the request/response chain. These middleware handlers simulate network latency, out of memory, runtime exception and application shutdown etc.

For more information, please visit the [document](https://doc.networknt.com/style/light-chaos-monkey/) and [tutorial](https://doc.networknt.com/tutorial/chaos-monkey/).

### Dynamic Configuration at runtime

Each middleware handler has its configuration file with a switch to enable or disable. So you can even deploy it to the production without any negative impact.

A configuration endpoint /chaosmonkey enables these handlers at runtime with different parameters. It allows controlled Chaos Engineering from the pipeline so that it can be rerun for every release. Also, this gives the team more opportunities to simulate random production behaviours.

### Assault Handlers

Assault handlers are the heart of Chaos Monkey and the service makes use of them based on your configuration. Following assault handlers are actual provided.

* Latency Assault

If Latency Assault is enabled, latency is added to a request. You control the number of requests where this should occur via the level.

* Exception Assault

You can determine at runtime whether an exception should occur when the method is used.

* KillApp Assault

When the configured methods are called in the application, the Chaos Monkey will shut down the application.

* Memory Assault

Memory Assaults attack the memory of the Java Virtual Machine.

### YAML parser alignment

This repository loads configuration through light-4j's `config` module; it has no
direct SnakeYAML API calls or merged-YAML writer. The parent manages SnakeYAML
Engine 3.0.1 for the transitive parser supplied by the
[light-4j Engine migration](https://github.com/networknt/light-4j/pull/2813).
Classic SnakeYAML 2.7 remains managed for compatibility with other transitive
consumers and existing classic API exports. No unused direct parser dependency
is added to the assault modules.

Dependency management selects versions; it does not migrate an older `config`
artifact or add Engine when that artifact does not depend on it. Builds must
consume the Engine-migrated light-4j artifacts for the parser migration to take
effect. Project and light-4j versions remain `2.4.2-SNAPSHOT` for the owner's
coordinated release update.

With the migrated `config`, YAML follows its YAML 1.2 Core settings. Review deployed
configuration before rollout: `on`/`yes` become strings, leading-zero integers are
decimal, legacy binary/underscore/sexagesimal forms become strings, and timestamps
are strings. Quote text and use `true`/`false` for booleans. Duplicate-key policy,
resource limits, supported tags, and decryption are controlled by light-4j; this
repository does not override them. Parser alignment is not validation of deployed
configuration files or a live chaos-assault qualification.
