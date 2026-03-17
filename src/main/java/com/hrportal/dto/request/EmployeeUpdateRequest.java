package com.hrportal.dto.request;

import com.hrportal.entity.Role;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {

    private String firstName;
    private String lastName;
    private String email;
    private Long departmentId;
    private Role role;
}
