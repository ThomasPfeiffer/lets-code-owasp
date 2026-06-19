# Lab-Backend: laeuft im Container via `gradle bootRun` mit Spring DevTools.
# Der docker-compose-Mount von ./backend/src ueber /workspace/src sorgt zusammen mit
# DevTools fuer automatischen Restart bei Code-Aenderungen (User Story 27).
FROM gradle:8.14-jdk21

WORKDIR /workspace

# Build-Definition zuerst kopieren, damit der Dependency-Layer cachebar ist.
COPY settings.gradle.kts build.gradle.kts ./

# Dependencies vorab aufloesen (best effort; faellt nicht, falls offline).
RUN gradle --no-daemon dependencies > /dev/null 2>&1 || true

# Quellcode kopieren (wird zur Laufzeit per Bind-Mount ueberlagert).
COPY src ./src

EXPOSE 3011

# Auto-Restart bei Backend-Code-Aenderung (User Story 27 / Issue 04).
#
# DevTools loest einen Neustart NUR aus, wenn sich der Classpath (kompilierte Klassen)
# aendert. Eine reine Source-Aenderung unter /workspace/src reicht NICHT — sie muss zuerst
# nach build/classes kompiliert werden. Deshalb laeuft bootRun im Gradle-Continuous-Modus
# (-t): Gradle ueberwacht src, kompiliert geaenderte Dateien neu in build/classes, und
# DevTools erkennt die geaenderte Klasse und startet den ApplicationContext neu.
#
# Kette: Host-Edit -> Bind-Mount /workspace/src -> `gradle -t` recompile -> build/classes
#        -> DevTools Classpath-Watcher -> Restart.
CMD ["gradle", "--no-daemon", "-t", "bootRun"]
