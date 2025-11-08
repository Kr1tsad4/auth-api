package app.auth.dtos;

import lombok.Data;

@Data
public class UserDataDto {
    private Integer id;
    private String email;
    private String fullName;
    private String dob;
    private String phoneNumber;
    private String status;
    private String role;
}