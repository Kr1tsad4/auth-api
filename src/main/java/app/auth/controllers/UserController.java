package app.auth.controllers;

import app.auth.dtos.UserDataDto;
import app.auth.exceptions.ForbiddenException;
import app.auth.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDataDto> getUserById(@PathVariable int id) {
        UserDataDto principalUser = (UserDataDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int authenticatedUserId = principalUser.getId();
        UserDataDto user = userService.findById(id);

        if (!user.getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not allowed to access this resource");
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
