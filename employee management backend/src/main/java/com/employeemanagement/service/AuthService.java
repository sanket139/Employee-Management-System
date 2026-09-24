package com.employeemanagement.service;

import com.employeemanagement.dto.request.LoginRequest;
import com.employeemanagement.dto.request.RegisterRequest;
import com.employeemanagement.dto.response.JwtResponse;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.enums.Role;
import com.employeemanagement.exception.BadRequestException;
import com.employeemanagement.exception.DuplicateResourceException;
import com.employeemanagement.repository.UserRepository;
import com.employeemanagement.security.JwtUtil;
import com.employeemanagement.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Public self-registration. Ordinary users can only register as HR or EMPLOYEE.
     * ADMIN accounts cannot be created through this public endpoint - see registerAdmin().
     */
    @Transactional
    public void register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered. Contact an existing administrator.");
        }
        createUser(request);
    }

    /**
     * Controlled admin creation - only invocable by an already-authenticated ADMIN
     * (enforced via @PreAuthorize on the controller endpoint).
     */
    @Transactional
    public void registerAdmin(RegisterRequest request) {
        request.setRole(Role.ADMIN);
        createUser(request);
    }

    private void createUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtUtil.generateToken(principal, user.getId(), user.getRole().name());

        return JwtResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
