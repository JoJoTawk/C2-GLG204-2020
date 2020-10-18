package com.baloombaz.userservice.controllers;

import com.baloombaz.userservice.controllers.dto.UserAccountDTO;
import com.baloombaz.userservice.controllers.dto.UserPasswordResetDTO;
import com.baloombaz.userservice.controllers.dto.UserRegistrationDTO;
import com.baloombaz.userservice.controllers.dto.UserUpdateDTO;
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

    @DeleteMapping("{id}")
    public ResponseEntity deleteAccount(@PathVariable("id") Long id) {
        try {
            userService.delete(id);
            rabbitTemplate.convertAndSend("users-exchange", "users.deleted", id);
        } catch (EmptyResultDataAccessException e) {
//            java.lang.Error error = new java.lang.Error("id", "User with requested id cannot be found");
            Error error = new Error("", "User with requested id cannot be found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseWrapper(error));
        } catch (Exception e) {
//            java.lang.Error error = new java.lang.Error("", "Deleting user failed");
            Error error = new Error("", "Deleting user failed\"found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper(error));
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDTO user) throws ServletException {
        User updatedUser = userService.getLazyInit(id);
        if (userService.getEmailCount(user.getEmail()) > 0) {
//            java.lang.Error error = new java.lang.Error("email", "Requested email is already in use");
            Error error = new Error("", "Requested email is already in use");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper(error));
        }
        updatedUser.setEmail(user.getEmail());
        User us = userService.save(updatedUser);
        // for now it's not needed to inform other services about user changes
        // rabbitTemplate.convertAndSend("users-exchange", "users.updated.#", us.getId()+";"+us.getEmail()+";update");
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}/reset-password")
    public ResponseEntity resetPassword(@PathVariable("id") Long id, @Valid @RequestBody UserPasswordResetDTO user) {
        try {
            //Difference between getOne method - loads whole entity immediately
            Optional<User> userPasswordReset = userService.get(id);
            if (!BCrypt.checkpw(user.getOldPassword(), userPasswordReset.get().getPasswordHash())) {
//                java.lang.Error error = new java.lang.Error("oldPassword", "Old password is not correct");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper());
            }
            if (!user.getNewPassword().equals(user.getConfirmNewPassword())) {
//                java.lang.Error error = new java.lang.Error("confirmNewPassword", "Confirmation of password is not correct");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper());
            }
            userService.resetPassword(user, userPasswordReset);
        } catch (EntityNotFoundException e) {
//            java.lang.Error error = new java.lang.Error("id", "User with requested id cannot be found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseWrapper());
        } catch (Exception e) {
//            java.lang.Error error = new java.lang.Error("", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseWrapper());
        }
        return ResponseEntity.ok().build();
    }
}
