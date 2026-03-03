package com.moneycontas.service;

import com.moneycontas.domain.entity.User;
import com.moneycontas.dto.request.AuthRequest;
import com.moneycontas.dto.request.RegisterRequest;
import com.moneycontas.dto.response.AuthResponse;
import com.moneycontas.repository.UserRepository;
import com.moneycontas.security.JwtService;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneycontas.domain.entity.UserBalance;
import com.moneycontas.repository.UserBalanceRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
            UserBalanceRepository userBalanceRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.userBalanceRepository = userBalanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EntityExistsException("Email already registered: " + request.email());
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        UserBalance balance = new UserBalance(user);
        userBalanceRepository.save(balance);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());

        return new AuthResponse(token, jwtService.getExpirationMs(), user.getName(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());

        return new AuthResponse(token, jwtService.getExpirationMs(), user.getName(), user.getEmail());
    }
}
