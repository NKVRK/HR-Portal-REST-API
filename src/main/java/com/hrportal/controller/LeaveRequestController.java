package com.hrportal.controller;

import com.hrportal.dto.request.LeaveRequestDto;
import com.hrportal.dto.response.LeaveRequestResponse;
import com.hrportal.entity.Employee;
import com.hrportal.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> applyLeave(
            @Valid @RequestBody LeaveRequestDto request,
            @AuthenticationPrincipal Employee currentEmployee) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveRequestService.applyLeave(request, currentEmployee));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> getAllLeaves(
            @AuthenticationPrincipal Employee currentEmployee) {
        return ResponseEntity.ok(leaveRequestService.getAllLeaves(currentEmployee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestResponse> getLeaveById(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee currentEmployee) {
        return ResponseEntity.ok(leaveRequestService.getLeaveById(id, currentEmployee));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> approveLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee currentEmployee) {
        return ResponseEntity.ok(leaveRequestService.approveLeave(id, currentEmployee));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> rejectLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee currentEmployee) {
        return ResponseEntity.ok(leaveRequestService.rejectLeave(id, currentEmployee));
    }
}
