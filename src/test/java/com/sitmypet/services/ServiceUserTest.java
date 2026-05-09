package com.sitmypet.services;

import com.sitmypet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServiceUserTest {

    private ServiceUser serviceUser;

    @BeforeEach
    void setUp() {
        serviceUser = new ServiceUser();
    }

    @Test
    void testAfficherListNotNull() {
        // Test basique pour s'assurer que la méthode afficher ne lève pas d'exception
        // et retourne bien une instance de List (même vide).
        List<User> list = serviceUser.afficher();
        assertNotNull(list, "La liste retournée ne doit pas être null");
    }
}
