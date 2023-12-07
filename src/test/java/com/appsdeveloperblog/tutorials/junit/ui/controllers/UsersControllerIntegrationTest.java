package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.security.SecurityConstants;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * WITH DEFINED PORT
 */
//// it tells spring boot to look for main application
//// class(annotated with @SpringBootApplication and has main method) and start spring app context
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
////        properties = {"server.port=8088", "hostname=192.168.0.2"}
//)
////@TestPropertySource(locations = "/application-test.properties", properties = "server.port=8088")
//@TestPropertySource(locations = "/application-test.properties")


/*
 * With Random port
 */
// it tells spring boot to look for main application
// class(annotated with @SpringBootApplication and has main method) and start spring app context
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate; // HttpClient

    @Value("${server.port}")
    private int serverPort;

    @LocalServerPort
    private int randomPort;

    private String authorizationToken;

    @Test
    void contextLoads() {
        System.out.println(serverPort);
        System.out.println(randomPort);
    }

    @Test
    @DisplayName("User can be created")
    @Order(1)
    void testCreateUser_whenValidDetailsProvided_returnsUserDetails() throws JSONException {
        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "John");
        userDetailsRequestJson.put("lastName", "Johnson");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password", "123456789");
        userDetailsRequestJson.put("repeatPassword", "123456789");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), headers);

        // Act
        ResponseEntity<UserRest> createdUserDetailsEntity = testRestTemplate.postForEntity(
                "/users", request,
                UserRest.class);

        UserRest createdUserDetails = createdUserDetailsEntity.getBody();

        // Assert
        assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        assertEquals(
                userDetailsRequestJson.getString("firstName"),
                createdUserDetails.getFirstName(),
                "Returned user's first name seems to be incorrect"
        );
        assertEquals(
                userDetailsRequestJson.getString("lastName"),
                createdUserDetails.getLastName(),
                "Returned user's last name seems to be incorrect"
        );
        assertEquals(
                userDetailsRequestJson.getString("email"),
                createdUserDetails.getEmail(),
                "Returned user's email seems to be incorrect"
        );
        assertFalse(
                createdUserDetails.getUserId().trim().isEmpty(),
                "User id should not be empty"
        );
    }

    @Test
    @DisplayName("GET /users requires JWT")
    @Order(2)
    void testGetUsers_whenMissingJWT_returns403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity requestEntity = new HttpEntity(null, headers);

        // Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange(
                "/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
        });

        // Assert
        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode(),
                "Http status code 403 Forbidden should have been returned"
        );
    }

    @Test
    @DisplayName("/login works")
    @Order(3)
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        // Arrange
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email", "test@test.com");
        loginCredentials.put("password", "123456789");
        HttpEntity<String> request = new HttpEntity<>(loginCredentials.toString());

        // Act
        ResponseEntity response = testRestTemplate.postForEntity(
                "/users/login",
                request,
                null);

        authorizationToken = response
                .getHeaders()
                .getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Http Status code should be 200");
        assertNotNull(
                response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0),
                "Response should contain Authorization header with JWT"
        );
        assertNotNull(
                response.getHeaders().getValuesAsList("UserID").get(0),
                "Response should contain UserID in a response header."
        );
    }

    @Test
    @Order(4)
    @DisplayName("GET /users works")
    void testGetUsers_whenValidJWTProvided_returnsUsers() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authorizationToken);

        HttpEntity requestEntity = new HttpEntity(headers);

        // Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange(
                "/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
                });

        // Assert
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode(),
                "Http status code should have be 200"
        );

        assertTrue(
                response.getBody().size() == 1,
                "There should be exactly 1 user in the list"
        );

    }
}
