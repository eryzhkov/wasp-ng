package ru.vsu.uic.wasp.ng.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@Testcontainers
@AutoConfigureMockMvc
public class SecurityAnonymousTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<WaspPostgreSQLContainer> postgreSQLContainer = WaspPostgreSQLContainer.getInstance();

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithAnonymousUser
    void anonymousUserHasAccessToDefaultStartPage() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Try to login")))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasAccessToLoginPage() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Login")))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToUsersManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get(contextPath + "/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToRepoManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/repos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToAccountManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToCMS() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/cms"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserCanAuthenticate() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/auth")
                .param("username", "admin@wasp")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/home"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserProvidesEmptyPrincipal() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .param("username", "")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login-failed"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserProvidesEmptyCredentials() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .param("username", "admin@wasp")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login-failed"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserProvidesEmptyPrincipalAndCredentials() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login-failed"))
                .andDo(MockMvcResultHandlers.print());
    }

}
