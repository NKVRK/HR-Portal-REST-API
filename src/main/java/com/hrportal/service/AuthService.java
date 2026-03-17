package com.hrportal.service;

import com.hrportal.dto.request.LoginRequest;
import com.hrportal.dto.request.RegisterRequest;
import com.hrportal.dto.response.AuthResponse;
import com.hrportal.dto.response.EmployeeResponse;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.entity.Role;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.repository.EmployeeRepository;
import com.hrportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmployeeService employeeService;

    public EmployeeResponse register(RegisterRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .department(department)
                .build();

        Employee saved = employeeRepository.save(employee);
        return employeeService.toEmployeeResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + request.getEmail()));

        String token = jwtTokenProvider.generateToken(employee);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(employee.getEmail())
                .role(employee.getRole())
                .build();
    }
}
