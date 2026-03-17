package com.hrportal.service;

import com.hrportal.dto.request.LeaveRequestDto;
import com.hrportal.dto.response.LeaveRequestResponse;
import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveRequest;
import com.hrportal.entity.LeaveStatus;
import com.hrportal.entity.Role;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.exception.UnauthorizedException;
import com.hrportal.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional
    public LeaveRequestResponse applyLeave(LeaveRequestDto dto, Employee employee) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    public List<LeaveRequestResponse> getAllLeaves(Employee employee) {
        if (employee.getRole() == Role.ADMIN) {
            return leaveRequestRepository.findAll().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        return leaveRequestRepository.findByEmployeeOrderByCreatedAtDesc(employee).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LeaveRequestResponse getLeaveById(Long id, Employee employee) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));

        if (employee.getRole() != Role.ADMIN && !leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to view this leave request");
        }

        return toResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse approveLeave(Long id, Employee reviewer) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING leave requests can be approved");
        }
        if (leaveRequest.getEmployee().getId().equals(reviewer.getId())) {
            throw new IllegalArgumentException("Employees cannot approve their own leave requests");
        }

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setReviewedBy(reviewer);
        leaveRequest.setReviewedAt(LocalDateTime.now());

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestResponse rejectLeave(Long id, Employee reviewer) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING leave requests can be rejected");
        }
        if (leaveRequest.getEmployee().getId().equals(reviewer.getId())) {
            throw new IllegalArgumentException("Employees cannot reject their own leave requests");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setReviewedBy(reviewer);
        leaveRequest.setReviewedAt(LocalDateTime.now());

        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    private LeaveRequestResponse toResponse(LeaveRequest lr) {
        String employeeName = lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName();
        String reviewedByName = lr.getReviewedBy() != null
                ? lr.getReviewedBy().getFirstName() + " " + lr.getReviewedBy().getLastName()
                : null;

        return LeaveRequestResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(employeeName)
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .reviewedByName(reviewedByName)
                .reviewedAt(lr.getReviewedAt())
                .createdAt(lr.getCreatedAt())
                .build();
    }
}
