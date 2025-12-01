package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.service.AuthService;
import com.amazin.svelteamazin.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@TestPropertySource(properties = "app.owner-password=testpw")
public class AuthControllerTest {

    private MockMvc mvc;

    private AuthService auth;

    private UserService users;

    @Test
    @BeforeEach
    public void setup() {
        auth = org.mockito.Mockito.mock(AuthService.class);
        users = org.mockito.Mockito.mock(UserService.class);

        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "auth", auth);
        ReflectionTestUtils.setField(controller, "users", users);
        // set configured owner password to the test property value
        ReflectionTestUtils.setField(controller, "configuredOwnerPassword", "testpw");

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    public void userLogin_success_setsCookiesAndReturnsUser() throws Exception {
        User u = new User(1L, "alice", "secret", "USER");
        when(auth.authenticateUser("alice", "secret")).thenReturn(u);

        mvc.perform(post("/api/auth/user-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(cookie().exists("role"))
                .andExpect(cookie().exists("username"));
    }

    @Test
    public void userLogin_invalidCredentials_returns401() throws Exception {
        when(auth.authenticateUser(anyString(), anyString())).thenReturn(null);

        mvc.perform(post("/api/auth/user-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"bad\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void ownerLogin_success_withAuthService() throws Exception {
        when(auth.authenticateOwner("ownerpw")).thenReturn(true);

        mvc.perform(post("/api/auth/owner-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"ownerpw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(cookie().exists("role"))
                .andExpect(cookie().exists("username"));
    }

    @Test
    public void ownerLogin_success_withConfiguredPassword() throws Exception {
        // AuthService returns false but the configured property (testpw) should allow login
        when(auth.authenticateOwner("testpw")).thenReturn(false);

        mvc.perform(post("/api/auth/owner-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"testpw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    public void ownerLogin_badPassword_returns401() throws Exception {
        when(auth.authenticateOwner("nope")).thenReturn(false);

        mvc.perform(post("/api/auth/owner-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void me_withUserCookie_returnsUser() throws Exception {
        User u = new User(2L, "bob", "pw", "USER");
        when(users.findByUsername("bob")).thenReturn(u);

        mvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("username", "bob"), new jakarta.servlet.http.Cookie("role", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    public void me_withOwnerRoleAndNoUsername_returnsOwnerFallback() throws Exception {
        mvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("role", "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("owner"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    public void me_noRoleCookie_returns401() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
