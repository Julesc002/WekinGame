package com.wekinGame.Controllers;

import java.util.Map;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wekinGame.Repository.UserRepository;
import com.wekinGame.ressources.HTTPCodes;
import com.wekinGame.ressources.Hasher;
import com.wekinGame.ressources.JavaMail;

@RestController
public class UserController {

    @PostMapping("/user/new")
    public ResponseEntity<HTTPCodes> createUser(
            final @RequestParam(value = "username") String username,
            final @RequestParam(value = "password") String password,
            final @RequestParam(value = "email") String email,
            final @RequestParam(value = "bday") String bday) {
        if (UserRepository.usernameOrEmailTaken(username, email)) {
            return new ResponseEntity<HTTPCodes>(HTTPCodes.CONFLICT, HttpStatus.CONFLICT);
        }
        Document newUser = new Document("pseudo", username)
                .append("_id", UserRepository.newUserId())
                .append("mail", email)
                .append("mdp", Hasher.hashPassword(password))
                .append("date_naissance", bday);
        UserRepository.push(newUser);
        JavaMail.sendBienvenueEmail(email, username);
        return new ResponseEntity<HTTPCodes>(HTTPCodes.OK, HttpStatus.OK);
    }

    @GetMapping("/user/{id}/delete")
    public ResponseEntity<HTTPCodes> deleteUser(final @PathVariable int id) {
        if (UserRepository.exist(id)) {
            UserRepository.delete(id);
            return new ResponseEntity<HTTPCodes>(HTTPCodes.OK, HttpStatus.OK);
        }
        return new ResponseEntity<HTTPCodes>(HTTPCodes.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/{id}/info")
    public Document getAccountInfo(final @PathVariable int id) {
        Document accountInfo = UserRepository.getUserInfoById(id);
        try {
            if (accountInfo != null) {
                return accountInfo;
            } else {
                return new Document("Response",
                        new ResponseEntity<HTTPCodes>(HTTPCodes.NOT_FOUND, HttpStatus.NOT_FOUND));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Document("Response",
                    new ResponseEntity<HTTPCodes>(HTTPCodes.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/user/connect")
    public Document connectAccount(final @RequestBody Map<String, String> param) {
        Document result = UserRepository.getFromPseudoAndPassword(param.get("pseudo"), param.get("password"));
        if (result == null) {
            return new Document("_id", -1);
        }
        return result;
    }

}