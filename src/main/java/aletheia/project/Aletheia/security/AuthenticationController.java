package aletheia.project.Aletheia.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(
            PasswordEncoder encoder,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    // 🔐 LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        String token = jwtUtil.generateToken(loginRequest.getEmail());

        return ResponseEntity.ok(
                new AuthResponse(loginRequest.getEmail(), token)
        );
    }

    // 📝 REGISTER
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        UserEntity user = new UserEntity();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRole("USER");
        user.setActive(true);


        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(user.getEmail(), token));
    }
}
