package com.example.ecompoc.user.controller;

import com.example.ecompoc.user.dto.CreateUserRequest;
import com.example.ecompoc.user.dto.LoginRequest;
import com.example.ecompoc.user.dto.LoginResponse;
import com.example.ecompoc.user.dto.UserResponse;
import com.example.ecompoc.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for user management endpoints
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Users", description = "User management API endpoints")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * POST /api/users - Create a new user
     */
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account with the provided user details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "User creation request", required = true)
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * GET /api/users/{id} - Get user profile by ID
     */
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user profile details for the specified user ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "User ID", required = true, example = "user123")
            @PathVariable String id) {
        UserResponse user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * POST /api/login - Authenticate user
     */
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password, returns login token on success"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
