package com.group2.admin_service.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.group2.admin_service.dto.ClaimDTO;
import com.group2.admin_service.dto.ClaimStatusDTO;
import com.group2.admin_service.dto.PolicyDTO;
import com.group2.admin_service.dto.PolicyRequestDTO;
import com.group2.admin_service.dto.ReportResponse;
import com.group2.admin_service.dto.ReviewRequest;
import com.group2.admin_service.dto.UserDTO;
import com.group2.admin_service.service.AdminService;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private AdminService adminService;

    public AdminController(AdminService adminService) {
        super();
        this.adminService = adminService;
    }

    // Claim Review API (Approve / Reject - PRD E.8)
    @PutMapping("/claims/{id}/review")
    public ResponseEntity<String> reviewClaim(
            @PathVariable("id") Long id,
            @RequestBody ReviewRequest request,
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        adminService.reviewClaim(id, request);
        return ResponseEntity.ok("Claim reviewed successfully");
    }

    // Get all claims with pagination (PRD C)
    @GetMapping("/claims")
    public ResponseEntity<Object> getAllClaims(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        try {
            return ResponseEntity.ok(adminService.getAllClaims(page, size));
        } catch (Exception e) {
            logger.error("Error in AdminController.getAllClaims: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Get all registered customers (PRD C)
    @GetMapping("/customers")
    public ResponseEntity<List<UserDTO>> getAllCustomers(
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        return ResponseEntity.ok(adminService.getAllCustomers());
    }

    // Operational Reports & Business Analytics (PRD E.10)
    @GetMapping("/reports")
    public ResponseEntity<ReportResponse> getReports(
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        return ResponseEntity.ok(adminService.getReports());
    }
}