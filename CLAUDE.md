# CLAUDE.md

This file provides guidance to AI assistants working with this codebase.

## Project Overview

A minimal **Jakarta EE 10** demonstration application that exposes a single JAX-RS REST endpoint, packaged as a **WildFly bootable JAR**.

- **Language**: Java 11
- **Framework**: Jakarta EE 10 (JAX-RS, CDI)
- **Application Server**: WildFly 27.0.1.Final (embedded bootable JAR)
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
2. **`wildfly-jar-maven-plugin:8.1.0.Final`** — produces `target/jeedemo-bootable.jar` with the `jaxrs` Galleon layer from WildFly 27.0.1.Final.
3. **`maven-compiler-plugin`** — compiles with Java source/target 11.
4. **`git-commit-id-maven-plugin:5.0.0`** — embeds git metadata (`git.properties`) into the build artifact.

Additional Maven repositories configured:
- JBoss Public Repository (`repository.jboss.org`)
- Red Hat GA (`maven.repository.redhat.com`)

## Conventions

- **Package**: `com.example.jeedemo` — add all new classes here.
- **REST resources**: annotate with `@Path`, place alongside `HelloController.java`.
- **CDI beans**: `beans.xml` uses `bean-discovery-mode="all"`, so all classes in the deployment are eligible for CDI injection without additional annotations.
- **No `web.xml`**: the application uses annotation-based configuration; `failOnMissingWebXml` is set to `false`.
- **No test framework** is currently configured. Add tests under `src/test/java` using a Jakarta EE-compatible testing approach (e.g., Arquillian) if needed.
- **No environment variables** or external configuration are required to run the application.

## Development Notes

- There is no linter or formatter enforced. Follow standard Java conventions.
- The `microprofile-config.properties` file is present but empty; use it to add MicroProfile Config properties when needed.
- The `git-commit-id-maven-plugin` generates `git.properties` in the WAR; do not manually create or edit that file.
- Avoid committing build artifacts (the `.gitignore` already excludes `target/`).
