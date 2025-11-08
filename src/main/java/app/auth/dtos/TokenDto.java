package app.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@AllArgsConstructor
public class TokenDto {
    private String access_token;
    private ResponseCookie refresh_token;
}
