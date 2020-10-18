package com.baloombaz.userservice.controllers;

import com.baloombaz.userservice.controllers.dto.UserAccountDTO;
import com.baloombaz.userservice.controllers.dto.UserRegistrationDTO;
import com.baloombaz.userservice.models.ErrorResponseWrapper;
import com.baloombaz.userservice.models.User;
import com.baloombaz.userservice.models.Error;
import com.baloombaz.userservice.services.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/find")
    public ResponseEntity find(@RequestParam String username) {
        UserAccountDTO userAccount = userService.findAccountByUsername(username);
        if (userAccount != null)
            return ResponseEntity.ok(userAccount);
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/details")
    public ResponseEntity getUser(@PathVariable("id") Long userId) {
        Optional<User> user = userService.get(userId);
        if (!user.isPresent()) {
            Error error = new Error("id", "User with requested id doesn't exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseWrapper(error));
        }
        return ResponseEntity.ok(user.get().getUserDetails());
    }

    @PostMapping("/register")
    public ResponseEntity registerUser(@Valid @RequestBody UserRegistrationDTO user, BindingResult bindingResult) {
        if (userService.getUsernameAndEmailCount(user.getUsername(), user.getEmail()) > 0) {
            Error error = new Error("", "Username or email is already in use");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper(error));
        }
        if (bindingResult.hasErrors()) {
            Error error = new Error();
            error.setField(bindingResult.getFieldError().getField());
            error.setMessage(bindingResult.getFieldError().getDefaultMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper(error));
        }
        User registeredUser = userService.register(user);
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info(registeredUser.toString());
        rabbitTemplate.convertAndSend("users-exchange","users.created.#", registeredUser.jsonUserAccountDTOAuthService());
        return ResponseEntity.ok(registeredUser);
    }

}
