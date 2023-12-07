package com.appsdeveloperblog.tutorials.junit.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UsersRepository usersRepository;

    private UserEntity user;

    @BeforeEach
    void init() {
        user = new UserEntity();
        user.setUserId(UUID.randomUUID().toString());
        user.setEmail("test@tes.com");
        user.setFirstName("test");
        user.setLastName("test");
        user.setEncryptedPassword("123456789");
        testEntityManager.persistAndFlush(user);
    }

    @Test
    void testFindByEmail_whenGivenCorrectEmail_returnsUserEntity() {
        // Arrange & Act
        UserEntity storedUser = usersRepository.findByEmail(user.getEmail());

        // Assert
        assertNotNull(storedUser);
        assertEquals(
                user.getEmail(),
                storedUser.getEmail(),
                "Returned email does not match the expected value"
        );
    }

    @Test
    void testFindById_whenGivenCorrectUserId_returnsUserEntity() {
        // Arrange
        testEntityManager.persistAndFlush(user);

        // Act
        UserEntity storedUser = usersRepository.findByUserId(user.getUserId());

        // Assert
        assertNotNull(storedUser);
        assertEquals(
                user.getUserId(),
                storedUser.getUserId(),
                "Returned user uuid does not match the expected value"
        );
    }



}