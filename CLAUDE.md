# CLAUDE.md

This file provides guidance to AI assistants working with this codebase.

## Project Overview

A minimal **Jakarta EE 11** demonstration application that exposes a single JAX-RS REST endpoint, packaged as a **WildFly bootable JAR**.

- **Language**: Java 21
- **Framework**: Jakarta EE 11 (JAX-RS, CDI)
- **Application Server**: WildFly 39.0.1.Final (embedded bootable JAR)
- **Build Tool**: Maven
- **Packaging**: WAR → Bootable JAR

## Repository Structure

```
jeeuberjardemo/
├── pom.xml                                          # Maven build configuration
├── readme.md                                        # Quick-start instructions
└── src/main/
    ├── java/com/example/jeedemo/
    │   ├── JeedemoRestApplication.java              # JAX-RS Application, root path /data
    │   └── HelloController.java                     # REST resource, GET /data/hello
    ├── resources/
    │   └── META-INF/
    │       └── microprofile-config.properties       # MicroProfile config (currently empty)
    └── webapp/
        ├── index.html                               # Landing page linking to the REST endpoint
        └── WEB-INF/
            └── beans.xml                            # CDI config (bean-discovery-mode="all")
```

## Build & Run

```bash
# Build the bootable JAR (runs the "liberty" Maven profile by default)
mvn package

# Run the application (available at http://localhost:8080)
java -jar target/jeedemo-bootable.jar
```

The bootable JAR bundles WildFly with only the `jaxrs` layer to keep the artifact minimal.

## Endpoints

| Method | URL | Response |
|--------|-----|----------|
| GET | `http://localhost:8080/data/hello` | `Hello World` |

The landing page at `http://localhost:8080/index.html` links to the JAX-RS endpoint.

## Key Source Files

### `JeedemoRestApplication.java`
Registers the JAX-RS application with the root path `/data`. All REST resources are reachable under this prefix.

### `HelloController.java`
Single JAX-RS resource annotated with `@Path("/hello")` and `@Singleton`. The `sayHello()` method handles `GET` requests and returns a plain-text `"Hello World"` response.

## Maven Configuration

The `pom.xml` defines a single Maven profile named `liberty` (active by default) that:

1. **`maven-war-plugin:3.3.2`** — packages the application as a WAR without requiring `web.xml`.
2. **`wildfly-jar-maven-plugin:12.0.0.Final`** — produces `target/jeedemo-bootable.jar` with the `jaxrs` Galleon layer from WildFly 39.0.1.Final.
3. **`maven-compiler-plugin`** — compiles with Java source/target 21.
4. **`git-commit-id-maven-plugin:5.0.0`** — embeds git metadata (`git.properties`) into the build artifact.

Additional Maven repositories configured:
- JBoss Public Repository (`repository.jboss.org`)
- Red Hat GA (`maven.repository.redhat.com`)

## Conventions

- **Package**: `com.example.jeedemo` — add all new classes here.
- **REST resources**: annotate with `@Path`, place alongside `HelloController.java`.
- **CDI beans**: `beans.xml` uses `bean-discovery-mode="all"`, so all classes in the deployment are eligible for CDI injection without additional annotations.
- **No `web.xml`**: the application uses annotation-based configuration; `failOnMissingWebXml` is set to `false`.
- **Java 21 required**: Jakarta EE 11 mandates Java SE 21 as the minimum runtime.
- **No test framework** is currently configured. Add tests under `src/test/java` using a Jakarta EE-compatible testing approach (e.g., Arquillian) if needed.
- **No environment variables** or external configuration are required to run the application.

## Pre-Merge Verification (Required)

Before merging any branch, verify the application builds and runs correctly using the following procedure. This is required every time.

### Environment Note

In this sandbox environment, `repository.jboss.org` and `maven.repository.redhat.com` are blocked (HTTP 407). `mvn package` cannot complete due to WildFly Galleon provisioning requiring these repos. Use the manual procedure below instead.

### Prerequisites

Download once and reuse across sessions:

```bash
# Download WildFly 39.0.1.Final (only needed once)
curl -L -o /tmp/wildfly-39.0.1.Final.zip \
  https://github.com/wildfly/wildfly/releases/download/39.0.1.Final/wildfly-39.0.1.Final.zip
unzip /tmp/wildfly-39.0.1.Final.zip -d /tmp/

# Download Jakarta EE 11 compile-time API JARs (only needed once)
curl -L -o /tmp/jaxrs4.jar \
  https://repo.maven.apache.org/maven2/jakarta/ws/rs/jakarta.ws.rs-api/4.0.0/jakarta.ws.rs-api-4.0.0.jar
curl -L -o /tmp/inject2.jar \
  https://repo.maven.apache.org/maven2/jakarta/inject/jakarta.inject-api/2.0.1/jakarta.inject-api-2.0.1.jar
```

### Verification Steps

```bash
# 1. Compile with Java 21 + Jakarta EE 11 APIs
rm -rf /tmp/classes11 && mkdir /tmp/classes11
javac --release 21 \
  -cp "/tmp/jaxrs4.jar:/tmp/inject2.jar" \
  src/main/java/com/example/jeedemo/*.java \
  -d /tmp/classes11

# 2. Package as WAR
rm -rf /tmp/wardir && mkdir -p /tmp/wardir/WEB-INF/classes
cp -r /tmp/classes11/com /tmp/wardir/WEB-INF/classes/
cp src/main/webapp/WEB-INF/beans.xml /tmp/wardir/WEB-INF/
cp src/main/webapp/index.html /tmp/wardir/
cd /tmp/wardir && jar cf /tmp/jeedemo.war . && cd -

# 3. Stop any running WildFly, deploy WAR, and start WildFly
pkill -9 -f "jboss|wildfly|standalone" 2>/dev/null; sleep 2
cp /tmp/jeedemo.war /tmp/wildfly-39.0.1.Final/standalone/deployments/
nohup /tmp/wildfly-39.0.1.Final/bin/standalone.sh \
  -Djboss.http.port=8080 > /tmp/wf.log 2>&1 &

# 4. Wait for startup (look for "started in" in log)
sleep 20 && grep -E "started in|Deployed|ERROR|FATAL" /tmp/wf.log

# 5. Verify endpoints
curl -s http://localhost:8080/jeedemo/data/hello
# Expected: Hello World

curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:8080/jeedemo/index.html
# Expected: HTTP 200
```

All steps must succeed before merging.

## Development Notes

- There is no linter or formatter enforced. Follow standard Java conventions.
- The `microprofile-config.properties` file is present but empty; use it to add MicroProfile Config properties when needed.
- The `git-commit-id-maven-plugin` generates `git.properties` in the WAR; do not manually create or edit that file.
- Avoid committing build artifacts (the `.gitignore` already excludes `target/`).
