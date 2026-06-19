package de.lise.lab.devexperience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.lise.lab.support.TestSecurityConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Dev-Experience-Slice (Issue 04): Hot-Reload (Frontend) + Auto-Restart (Backend).
 *
 * <p>Diese Slice ist primaer infrastrukturell (docker compose, Dockerfile, Vite/Gradle).
 * Da Hot-Reload und Auto-Restart nicht innerhalb eines JVM-Unit-Tests ausgeloest werden
 * koennen (sie brauchen den laufenden Container, einen Dateiwatcher und Recompile), pruefen
 * die Tests die <em>Voraussetzungen</em>, die diese Mechanismen ueberhaupt funktionieren
 * lassen, sowie dass die Dev-Experience-Konfiguration den laufenden Server nicht regressiert:
 *
 * <ul>
 *   <li>Backend-Auto-Restart braucht (a) die Spring-DevTools-Abhaengigkeit und (b) einen
 *       Recompile-Schritt, der geaenderte Quellen in den Classpath bringt — im Container per
 *       Gradle-Continuous-Build ({@code gradle -t bootRun}). DevTools startet nur bei einer
 *       Classpath-Aenderung neu, nie bei einer reinen Source-Aenderung.</li>
 *   <li>Frontend-Hot-Reload braucht den Vite-Dev-Server mit {@code --host} und einen
 *       Bind-Mount des Quellverzeichnisses.</li>
 *   <li>Beides muss mit dem unveraenderten {@code docker compose up} laufen.</li>
 * </ul>
 *
 * <p>Der Boot-Smoke-Test stellt sicher, dass der ApplicationContext mit den
 * Dev-Experience-Properties startet und die API weiterhin antwortet.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class DevExperienceConfigTest {

    /** Projekt-Root relativ zum Backend-Modul (Test-CWD ist .../backend). */
    private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath().getParent();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    // --- AC: "Beides funktioniert mit der unveraenderten docker compose up-Umgebung." ---
    // Boot-Smoke: der Server startet mit aktiver Profilkonfiguration und antwortet an der
    // HTTP-Grenze. Beweist, dass die Dev-Experience-Aenderungen die App nicht regressiert haben.

    @Test
    void applicationBootsAndServesApiAtHttpBoundary() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        // Anonym -> 401 (Resource Server lebt), authentifiziert -> 200 (API lebt).
        mvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/todos").with(jwt()
                        .jwt(b -> b.claim("preferred_username", "alice"))
                        .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
    }

    // --- AC: "Eine Aenderung an einer Backend-Quelldatei loest einen automatischen Restart
    //          des Backends aus (Spring DevTools)." ---
    // Voraussetzung 1: DevTools ist als Abhaengigkeit deklariert.

    @Test
    void springDevToolsDependencyIsDeclared() throws Exception {
        String buildFile = Files.readString(PROJECT_ROOT.resolve("backend/build.gradle.kts"));
        assertThat(buildFile)
                .as("Auto-Restart braucht die Spring-DevTools-Abhaengigkeit (developmentOnly)")
                .contains("org.springframework.boot:spring-boot-devtools");
    }

    // Voraussetzung 2: Der Container muss geaenderte Quellen recompilieren, sonst aendert sich
    // der Classpath nie und DevTools startet nicht neu. Das leistet der Gradle-Continuous-Build.

    @Test
    void backendContainerRunsBootRunWithContinuousRecompile() throws Exception {
        String dockerfile = Files.readString(PROJECT_ROOT.resolve("backend/Dockerfile"));
        assertThat(dockerfile)
                .as("bootRun muss laufen, damit der Server im Container startet")
                .contains("bootRun");
        assertThat(dockerfile)
                .as("Continuous-Build (-t) recompiliert geaenderte Quellen -> Classpath-Aenderung "
                        + "-> DevTools-Restart. Ohne -t loest eine Source-Aenderung keinen Restart aus.")
                .contains("\"-t\"");
    }

    @Test
    void composeMountsBackendSourceAndEnablesDevToolsRestart() throws Exception {
        String compose = Files.readString(PROJECT_ROOT.resolve("docker-compose.yml"));
        assertThat(compose)
                .as("Backend-Quellen muessen in den Container gemountet sein, damit Host-Edits "
                        + "im Container sichtbar werden")
                .contains("./backend/src:/workspace/src");
        assertThat(compose)
                .as("DevTools-Restart muss aktiviert sein")
                .contains("SPRING_DEVTOOLS_RESTART_ENABLED: \"true\"");
    }

    // --- AC: "Eine Aenderung an einer Frontend-Quelldatei wird ohne manuellen Rebuild live
    //          im Browser sichtbar." ---
    // Voraussetzungen: Vite-Dev-Server mit --host (extern erreichbar) + Bind-Mount der Quellen.

    @Test
    void frontendContainerRunsViteDevServerWithHost() throws Exception {
        String dockerfile = Files.readString(PROJECT_ROOT.resolve("frontend/Dockerfile"));
        assertThat(dockerfile)
                .as("Hot-Reload braucht den Vite-Dev-Server (npm run dev), nicht einen Prod-Build")
                .contains("run");
        assertThat(dockerfile)
                .as("--host macht den HMR-faehigen Dev-Server im Container von aussen erreichbar")
                .contains("--host");
    }

    @Test
    void composeBindMountsFrontendSourceForHmr() throws Exception {
        String compose = Files.readString(PROJECT_ROOT.resolve("docker-compose.yml"));
        assertThat(compose)
                .as("Frontend-Quellen als Bind-Mount -> Host-Edits erscheinen live im Container -> HMR")
                .contains("./frontend/src:/app/src");
    }
}
