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

### Jackson 3 source qualification

The Jackson 3 source-build workflow checks out the exact PR head and builds against
the qualified light-4j candidate and bootstrap sources pinned in
ci/jackson3-source-refs.json. It runs the full reactor (including the real POST
handler binding test), audits resolved source origins, and uploads dependency trees
and Surefire reports. It does not use published NetworkNT snapshots as substitutes
for those sources.

The parent Maven Enforcer rule rejects Jackson 2 runtime dependencies, including
transitive dependencies, during ordinary builds. Jackson annotations remain
allowed because Jackson 3 uses that compatible artifact. The chaos-monkey module
explicitly declares Jackson 3 databind, whose version is inherited from the
light-4j dependency-management BOM rather than independently pinned here, because its POST handler invokes the mapper
returned by Config; the other assault modules do not invoke Jackson directly.

After this change merges, dependent applications must update their source pin to
the actual merge SHA and pass fresh source-build/distribution CI. A local build of
this PR does not qualify an unchanged consumer pin.

The POST regression sends real JSON through BodyHandler before exercising all
four assault conversion branches. It also checks numeric-string coercion, empty
object defaults, malformed JSON, missing/empty bodies, unknown assaults and invalid
integer values, including preservation of prior configs after rejected requests.
It records existing error semantics: ERR10001 resolves to HTTP 401 for missing
bodies/unknown assaults, and invalid numeric conversion propagates as HTTP 500.
Changing those production error codes is outside this dependency cleanup.
