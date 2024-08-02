package ru.vsu.uic.wasp.ng.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class SecurityAuthenticatedUserTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<WaspPostgreSQLContainer> postgreSQLContainer = WaspPostgreSQLContainer.getInstance();

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(username = "anyName", password = "anyPassword", roles = {"USER_MANAGER"})
    void authenticatedUserWithUserManagerRoleHasAccessToUserManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(username = "anyName", password = "anyPassword", roles = {"REPO_MANAGER"})
    void authenticatedUserWithReposManagerRoleHasAccessToReposManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/repos"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(username = "anyName", password = "anyPassword", roles = {"CONTENT_MANAGER"})
    void authenticatedUserWithContentManagerRoleHasAccessToContentManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/cms"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(username = "anyName", password = "anyPassword", roles = {"AUTHENTICATED_USER"})
    void authenticatedUserWithAuthenticatedUserRoleHasAccessToHome() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/home"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(username = "anyName", password = "anyPassword", roles = {"AUTHENTICATED_USER"})
    void authenticatedUserWithAuthenticatedUserRoleHasAccessToAccountManagement() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/account"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

}
