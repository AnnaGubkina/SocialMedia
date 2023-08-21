package api.socialmedia.controller;


import api.socialmedia.dto.request.AuthRequest;
import api.socialmedia.dto.request.RegRequest;
import api.socialmedia.dto.responce.AuthResponse;
import api.socialmedia.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @Operation(summary  = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
    })
    @PostMapping("/register")
    public ResponseEntity<?>register(@RequestBody RegRequest request) {
        return authenticationService.register(request);
    }


    @Operation(summary  = "Log in to the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode  = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        log.info("Authentication is successfully");
        return authenticationService.login(request);

    }
}
