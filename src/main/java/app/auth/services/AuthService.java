package app.auth.services;
import app.auth.dtos.TokenDto;
import app.auth.dtos.UserDataDto;
import app.auth.dtos.UserLoginDto;
import app.auth.dtos.UserRegisterDto;
import app.auth.entities.User;
import app.auth.exceptions.EmailAlreadyExistsException;
import app.auth.exceptions.InvalidCredentialsException;
import app.auth.repositories.UserRepository;
import app.auth.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,EntityManager entityManager
            ,ModelMapper modelMapper,PasswordEncoder passwordEncoder,JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserDataDto register(UserRegisterDto registerUser){
        String email = registerUser.getEmail();
        if(userRepository.existsByEmail(email)){
            throw new EmailAlreadyExistsException("Email already exist");
        }
        User newUser = modelMapper.map(registerUser,User.class);
        newUser.setPassword(passwordEncoder.encode(registerUser.getPassword()));
        newUser.setStatus("Inactive");
        newUser.setRole("User");

        User savedUser = userRepository.save(newUser);
        entityManager.flush();

        return modelMapper.map(savedUser,UserDataDto.class);
    }

    public TokenDto login(UserLoginDto loginUser){
        User user =  userRepository.findByEmail(loginUser.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        boolean isPasswordMatched = passwordEncoder.matches(loginUser.getPassword(),user.getPassword());

        if(!isPasswordMatched){
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        return new TokenDto(accessToken,refreshTokenCookie);
    }

    public ResponseCookie logout() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

}
