package api.socialmedia.service;

import api.socialmedia.dto.request.AuthRequest;
import api.socialmedia.dto.request.RegRequest;
import api.socialmedia.dto.responce.AuthResponse;
import api.socialmedia.entity.Role;
import api.socialmedia.entity.User;
import api.socialmedia.exception.InputDataException;
import api.socialmedia.model.EnumRoles;
import api.socialmedia.repository.AuthenticationRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationRepository authenticationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public ResponseEntity<?> register(RegRequest request) {
        Optional<User> userFromBD = userRepository.findByUsername(request.getLogin());
        if (userFromBD.isPresent()) {
            throw new InputDataException("User with the same username already exists");
        }

        User newUser = User.builder()
                .username(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(Collections.singleton(new Role(EnumRoles.ROLE_USER)))
                .build();
        userRepository.save(newUser);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    public AuthResponse login(AuthRequest request) {
        final String username = request.getLogin();
        final String password = request.getPassword();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final UserDetails userDetails = userService.loadUserByUsername(username);
        String token = jwtTokenUtil.generateToken(userDetails);
        authenticationRepository.putTokenAndUsername(token, username);
        log.info("User {} is authorized", username);
        return AuthResponse.builder()
                .authToken(token)
                .build();
    }
}
