# disqualified-officers-delta-consumer
The `disqualified-officers-delta-consumer` is responsible for transforming officer disqualification data from the 
`disqualified-officers-delta` kafka topic as part of chips and chs data sync.

## Build tools

- [Java 11](https://www.oracle.com/uk/java/technologies/javase/jdk11-archive-downloads.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

## Build

Common commands used for development and running locally can be found in the Makefile.

```text
Target               Description
------               -----------
all                  Calls methods required to build a locally runnable version, typically the build target
build                Pull down any dependencies and compile code into an executable if required
clean                Reset repo to pre-build state (i.e. a clean checkout state)
package              Create a single versioned deployable package (i.e. jar, zip, tar, etc.). May be dependent on the 
build target being run before package
sonar                Run sonar scan
test                 Run all test-* targets (convenience method for developers)
test-unit            Run unit tests

```

## Endpoints
| URL | Description |
| --- | ----------- |
| /disqualified-officers-delta-consumer/healthcheck | Health check URL returns 200 if service is running |

