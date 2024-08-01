package ru.vsu.uic.wasp.ng.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class SecurityAnonymousTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<WaspPostgreSQLContainer> postgreSQLContainer = WaspPostgreSQLContainer.getInstance();

    @Autowired
    private MockMvc mvc;

    @Test
    @WithAnonymousUser
    void anonymousUserHasAccessToDefaultStartPage() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasAccessToLoginPage() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToUsersManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().is3xxRedirection())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToRepoManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/repos"))
                .andExpect(status().is3xxRedirection())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToAccountManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/account"))
                .andExpect(status().is3xxRedirection())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserHasNoAccessToCMS() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/cms"))
                .andExpect(status().is3xxRedirection())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserCanAuthenticate() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", "admin@wasp")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/home"))
                .andDo(MockMvcResultHandlers.print());
    }

}
