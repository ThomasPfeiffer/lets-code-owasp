package de.lise.lab.todo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.lise.lab.support.TestSecurityConfig;
import de.lise.lab.support.TestTokens;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.web.FilterChainProxy;

/**
 * Integrationstests an der HTTP-Grenze (MockMvc + spring-security-test).
 *
 * <p>Etabliert das Test-Pattern fuer alle folgenden Slices:
 * <ul>
 *   <li>Verhalten/Autorisierung mit simuliertem {@code jwt()}-RequestPostProcessor
 *       (inkl. {@code realm_access.roles}), ohne Keycloak/Netzwerk.</li>
 *   <li>Token-Validierung (Signatur/Ablauf/Issuer) mit echten signierten Tokens gegen den
 *       vollstaendigen Security-Filter-Chain.</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TodoApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private TodoRepository todos;

    private MockMvc mvc;

    private Long aliceTodoId;
    private Long bobTodoId;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        todos.deleteAll();
        aliceTodoId = todos.save(new Todo("alice", "Alice eins", false)).getId();
        todos.save(new Todo("alice", "Alice zwei", true));
        bobTodoId = todos.save(new Todo("bob", "Bob geheim", false)).getId();
    }

    // --- Verhalten / Autorisierung (simuliertes jwt()) ---

    @Test
    void anonymousRequestIsUnauthorized() throws Exception {
        mvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userSeesOnlyOwnTodos() throws Exception {
        mvc.perform(get("/api/todos").with(aliceJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.owner == 'bob')]").isEmpty())
                .andExpect(jsonPath("$[?(@.owner == 'alice')]").exists());
    }

    @Test
    void bobDoesNotSeeAliceTodos() throws Exception {
        mvc.perform(get("/api/todos").with(bobJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].owner").value("bob"));
    }

    @Test
    void realmRolesAreTranslatedToAuthorities() throws Exception {
        // Echte signierte Tokens durchlaufen den KeycloakRealmRoleConverter.
        // user-Token darf /api/todos (authenticated), aber nicht /api/admin/** (ROLE_admin).
        mvc.perform(get("/api/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.validToken("alice", List.of("user")))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.validToken("alice", List.of("user")))))
                .andExpect(status().isForbidden());
    }

    // --- Schreiboperationen + Ownership (Slice #2) ---

    @Test
    void postCreatesTodoWithOwnerFromToken() throws Exception {
        mvc.perform(post("/api/todos").with(aliceJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Neu von Alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.owner").value("alice"))
                .andExpect(jsonPath("$.title").value("Neu von Alice"))
                .andExpect(jsonPath("$.done").value(false));

        // Persistiert und Alice zugeordnet.
        mvc.perform(get("/api/todos").with(aliceJwt()))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void postIgnoresOwnerFromBody() throws Exception {
        // Alice schmuggelt owner=bob in den Body -> muss ignoriert werden.
        mvc.perform(post("/api/todos").with(aliceJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Untergeschoben\",\"owner\":\"bob\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value("alice"));

        // Bob darf den Eintrag nicht sehen.
        mvc.perform(get("/api/todos").with(bobJwt()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[?(@.title == 'Untergeschoben')]").isEmpty());
    }

    @Test
    void getOwnTodoByIdSucceeds() throws Exception {
        mvc.perform(get("/api/todos/" + aliceTodoId).with(aliceJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aliceTodoId))
                .andExpect(jsonPath("$.owner").value("alice"))
                .andExpect(jsonPath("$.title").value("Alice eins"));
    }

    @Test
    void getForeignTodoByIdIsRejected() throws Exception {
        // Alice greift auf Bobs Todo zu -> 404 (Ownership-Schutz, IDOR-Ansatzpunkt).
        mvc.perform(get("/api/todos/" + bobTodoId).with(aliceJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void putUpdatesOwnTodoTitleAndStatus() throws Exception {
        mvc.perform(put("/api/todos/" + aliceTodoId).with(aliceJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Alice eins (geaendert)\",\"done\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aliceTodoId))
                .andExpect(jsonPath("$.owner").value("alice"))
                .andExpect(jsonPath("$.title").value("Alice eins (geaendert)"))
                .andExpect(jsonPath("$.done").value(true));
    }

    @Test
    void putForeignTodoIsRejected() throws Exception {
        mvc.perform(put("/api/todos/" + bobTodoId).with(aliceJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Gekapert\",\"done\":true}"))
                .andExpect(status().isNotFound());

        // Bobs Todo bleibt unveraendert.
        mvc.perform(get("/api/todos/" + bobTodoId).with(bobJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bob geheim"))
                .andExpect(jsonPath("$.done").value(false));
    }

    @Test
    void deleteOwnTodoSucceeds() throws Exception {
        mvc.perform(delete("/api/todos/" + aliceTodoId).with(aliceJwt()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/todos/" + aliceTodoId).with(aliceJwt()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/todos").with(aliceJwt()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteForeignTodoIsRejected() throws Exception {
        mvc.perform(delete("/api/todos/" + bobTodoId).with(aliceJwt()))
                .andExpect(status().isNotFound());

        // Bobs Todo existiert weiterhin.
        mvc.perform(get("/api/todos/" + bobTodoId).with(bobJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void writeOperationsRequireAuthentication() throws Exception {
        mvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"anon\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/todos/" + aliceTodoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"anon\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/todos/" + aliceTodoId))
                .andExpect(status().isUnauthorized());
    }

    // --- Admin-Ansicht + Function-Level Authorization (Slice #3) ---

    @Test
    void adminSeesAllTodosOfAllUsers() throws Exception {
        mvc.perform(get("/api/admin/todos").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.owner == 'alice')]").exists())
                .andExpect(jsonPath("$[?(@.owner == 'bob')]").exists());
    }

    @Test
    void userIsForbiddenFromAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/todos").with(aliceJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointWithRealSignedAdminTokenReturnsAllTodos() throws Exception {
        // Echtes signiertes admin-Token durchlaeuft den vollen Filter-Chain + KeycloakRealmRoleConverter.
        mvc.perform(get("/api/admin/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.validToken("admin", List.of("admin")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void adminEndpointWithRealSignedUserTokenIsForbidden() throws Exception {
        // Echtes signiertes user-Token -> 403 an /api/admin/todos.
        mvc.perform(get("/api/admin/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.validToken("alice", List.of("user")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/admin/todos"))
                .andExpect(status().isUnauthorized());
    }

    // --- Token-Validierung (echte signierte Tokens, voller Filter-Chain) ---

    @Test
    void validSignedTokenIsAccepted() throws Exception {
        mvc.perform(get("/api/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.validToken("alice", List.of("user")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void expiredTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.expiredToken("alice", List.of("user")))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongIssuerTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.wrongIssuerToken("alice", List.of("user")))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongSignatureTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/todos").header(HttpHeaders.AUTHORIZATION,
                        bearer(TestTokens.wrongSignatureToken("alice", List.of("user")))))
                .andExpect(status().isUnauthorized());
    }

    // --- Helfer ---

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor aliceJwt() {
        return jwt()
                .jwt(builder -> builder.claim("preferred_username", "alice"))
                .authorities(authoritiesFromRealmRoles("user"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor bobJwt() {
        return jwt()
                .jwt(builder -> builder.claim("preferred_username", "bob"))
                .authorities(authoritiesFromRealmRoles("user"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor adminJwt() {
        return jwt()
                .jwt(builder -> builder.claim("preferred_username", "admin"))
                .authorities(authoritiesFromRealmRoles("admin"));
    }

    private static java.util.Collection<org.springframework.security.core.GrantedAuthority>
            authoritiesFromRealmRoles(String... roles) {
        // Spiegelt KeycloakRealmRoleConverter: realm-Rolle -> ROLE_<rolle>.
        return java.util.Arrays.stream(roles)
                .map(r -> (org.springframework.security.core.GrantedAuthority)
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }
}
