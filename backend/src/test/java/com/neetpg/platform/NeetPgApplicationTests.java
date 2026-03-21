package com.neetpg.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neetpg.platform.dto.AuthDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NeetPgApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;

    @Test
    @Order(1)
    void contextLoads() {
    }

    @Test
    @Order(2)
    void testRegister() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @Order(3)
    void testLogin() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @Order(4)
    void testDuplicateRegistration() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setName("Test User 2");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void testInvalidLogin() throws Exception {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void testGetSubjectsPublic() throws Exception {
        mockMvc.perform(get("/api/subjects"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void testProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    void testProtectedEndpointWithAuth() throws Exception {
        if (authToken == null) {
            testRegister();
        }
        mockMvc.perform(get("/api/quiz/sessions")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void testQuizStartSubmitAndResult() throws Exception {
        if (authToken == null) {
            testRegister();
        }

        // Start a quiz
        MvcResult startResult = mockMvc.perform(post("/api/quiz/start")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quizType\":\"RANDOM\",\"questionCount\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.questions").isArray())
                .andReturn();

        String startJson = startResult.getResponse().getContentAsString();
        var root = objectMapper.readTree(startJson);
        long sessionId = root.get("sessionId").asLong();
        var questions = root.get("questions");
        long q1Id = questions.get(0).get("id").asLong();
        long q2Id = questions.get(1).get("id").asLong();

        // Submit the quiz
        String submitBody = String.format(
                "{\"answers\":[{\"questionId\":%d,\"selectedAnswer\":\"A\",\"timeTaken\":10},{\"questionId\":%d,\"selectedAnswer\":\"B\",\"timeTaken\":15}]}",
                q1Id, q2Id);

        mockMvc.perform(post("/api/quiz/" + sessionId + "/submit")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.totalQuestions").value(2));

        // Get quiz result (this is the endpoint that was returning 500)
        mockMvc.perform(get("/api/quiz/" + sessionId + "/result")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.questionDetails").isArray());
    }

    @Test
    @Order(9)
    void testValidationErrors() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setName("");
        request.setEmail("invalid");
        request.setPassword("12");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
