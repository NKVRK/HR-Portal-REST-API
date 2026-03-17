package com.hrportal.service;

import com.hrportal.dto.request.DepartmentRequest;
import com.hrportal.dto.response.DepartmentResponse;
import com.hrportal.dto.response.EmployeeResponse;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department already exists with name: " + request.getName());
        }
        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return toDepartmentResponse(departmentRepository.save(department));
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDepartmentResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        return toDepartmentResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (!department.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department already exists with name: " + request.getName());
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return toDepartmentResponse(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        departmentRepository.delete(department);
    }

    @Transactional
    public EmployeeResponse assignEmployeeToDepartment(Long employeeId, Long departmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));

        employee.setDepartment(department);
        Employee saved = employeeRepository.save(employee);

        return employeeService.toEmployeeResponse(saved);
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
