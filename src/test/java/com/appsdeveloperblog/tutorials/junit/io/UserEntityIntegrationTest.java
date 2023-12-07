package com.appsdeveloperblog.tutorials.junit.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserEntityIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager; //persists entity to DB

    private UserEntity userEntity;

    @BeforeEach
    void init() {
        userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setFirstName("John");
        userEntity.setLastName("Johnson");
        userEntity.setEmail("john@mail.com");
        userEntity.setEncryptedPassword("123456789");
    }

    @Test
    @DisplayName("Valid user can be persisted")
    void testUserEntity_whenValidUserDetailsProvided_shouldReturnStoredUserDetails() {
        // Arrange & Act
        UserEntity storedUserEntity = testEntityManager.persistAndFlush(userEntity);

        // Assert
        assertTrue(storedUserEntity.getId() > 0);
        assertEquals(userEntity.getUserId(), storedUserEntity.getUserId());
        assertEquals(userEntity.getFirstName(), storedUserEntity.getFirstName());
        assertEquals(userEntity.getLastName(), storedUserEntity.getLastName());
        assertEquals(userEntity.getEmail(), storedUserEntity.getEmail());
        assertEquals(userEntity.getEncryptedPassword(), storedUserEntity.getEncryptedPassword());
    }

    @Test
    @DisplayName("User with too long first name cannot be persisted")
    void testUserEntity_whenFirstNameIsTooLong_shouldThrowException() {
        // Arrange
        userEntity.setFirstName("*".repeat(51));

        // Assert & Act
        assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity);
        }, "Was expecting a PersistenceException to be thrown.");
    }

    @Test
    @DisplayName("User with non-unique UserId cannot be persisted")
    void testUserEntity_whenUserIdIsNotUnique_shouldThrowException() {
        // Arrange
        testEntityManager.persistAndFlush(userEntity);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setUserId(userEntity.getUserId());
        userEntity2.setFirstName("John2");
        userEntity2.setLastName("Johnson2");
        userEntity2.setEmail("john2@mail.com");
        userEntity2.setEncryptedPassword("123456789");

//        // Assert & Act
        assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity2);
        }, "Was expecting a PersistenceException to be thrown.");

    }

}