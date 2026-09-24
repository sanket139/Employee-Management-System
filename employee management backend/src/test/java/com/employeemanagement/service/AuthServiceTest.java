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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setUsername("johndoe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.EMPLOYEE);
    }

    @Test
    void register_success_savesEncodedPasswordUser() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("ENCODED");

        authService.register(registerRequest);

        verify(userRepository).save(argThat(u ->
                u.getUsername().equals("johndoe") &&
                u.getPassword().equals("ENCODED") &&
                u.getRole() == Role.EMPLOYEE));
    }

    @Test
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void register_adminRoleViaPublicEndpoint_throwsBadRequest() {
        registerRequest.setRole(Role.ADMIN);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Admin accounts cannot be self-registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsJwtResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        User user = User.builder()
                .id(1L).username("johndoe").email("john@example.com")
                .password("ENCODED").role(Role.EMPLOYEE).enabled(true).build();

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), eq(1L), eq("EMPLOYEE"))).thenReturn("mock.jwt.token");

        JwtResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUsername()).isEqualTo("johndoe");
        assertThat(response.getRole()).isEqualTo("EMPLOYEE");
        verify(authenticationManager).authenticate(any());
    }
}
