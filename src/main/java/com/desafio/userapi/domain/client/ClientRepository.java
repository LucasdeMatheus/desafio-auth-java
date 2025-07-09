package com.desafio.userapi.domain.client;

import com.desafio.userapi.domain.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);

    boolean existsByEmail(String email);

    Client findByEmail(String email);

    void deleteByEmail(String email);
}

