package com.wekinGames.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.wekinGame.Controllers.UserController;
import com.wekinGame.Repository.UserRepository;
import com.wekinGame.ressources.HTTPCodes;
import com.wekinGame.ressources.Hasher;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private static MockedStatic<UserRepository> userMock = mockStatic(UserRepository.class);

    @InjectMocks
    private UserController userController;

    @AfterEach
    public void clearInvocations() {
        userMock.clearInvocations();
    }

    @AfterAll
    public static void close() {
        userMock.close();
    }

    @Test
    public void testCreateUser() {
        // GIVEN
        String givenNom = "test";
        String givenPassword = "test";
        String givenEmail = "test@gmail.com";
        String givenBirthday = "28/12/2004";
        Integer expectedId = 2;
        Document expectedUser = new Document("pseudo", givenNom)
                .append("_id", expectedId)
                .append("mail", givenEmail)
                .append("mdp", Hasher.hashPassword(givenPassword))
                .append("date_naissance", givenBirthday);
        userMock.when(() -> UserRepository.newUserId()).thenReturn(expectedId);

        // WHEN
        userController.createUser(givenNom, givenPassword, givenEmail, givenBirthday);

        // THEN
        userMock.verify(() -> UserRepository.push(expectedUser), Mockito.times(1));
    }

    @Test
    public void testCreateUserCaseEmailOrUsernameAlreadyTaken() {
        // GIVEN
        String givenNom = "test";
        String givenPassword = "test";
        String givenEmail = "testgmailcom";
        String givenBirthday = "28/12/2004";
        Boolean expectedBoolean = true;
        ResponseEntity<HTTPCodes> expectedDocument = new ResponseEntity<HTTPCodes>(HTTPCodes.CONFLICT,
                HttpStatus.CONFLICT);
        userMock.when(() -> UserRepository.usernameOrEmailTaken(givenNom, givenEmail)).thenReturn(expectedBoolean);

        // WHEN
        ResponseEntity<HTTPCodes> obtainedDocument = userController.createUser(givenNom, givenPassword, givenEmail,
                givenBirthday);

        // THEN
        assertEquals(expectedDocument, obtainedDocument);
    }

    @Test
    public void testDeleteUser() {
        // GIVEN
        int idUser = 1;
        Boolean expectedBoolean = true;
        userMock.when(() -> UserRepository.exist(idUser)).thenReturn(expectedBoolean);

        // WHEN
        userController.deleteUser(idUser);

        // THEN
        userMock.verify(() -> UserRepository.delete(idUser), Mockito.times(1));
    }

    @Test
    public void testDeleteUserCaseIdNotExist() {
        // GIVEN
        int idUser = 1;
        Boolean expectedBoolean = false;
        userMock.when(() -> UserRepository.exist(idUser)).thenReturn(expectedBoolean);

        // WHEN
        userController.deleteUser(idUser);

        // THEN
        userMock.verify(() -> UserRepository.delete(idUser), Mockito.never());
    }

    @Test
    public void testGetAccountInfo() {
        // GIVEN
        int idUser = 1;
        Document expectedUserInfo = new Document("username", "test")
                .append("date_naissance", "18/01/2000");
        userMock.when(() -> UserRepository.getUserInfoById(idUser)).thenReturn(expectedUserInfo);

        // WHEN
        Document obtainedUserInfo = userController.getAccountInfo(idUser);

        // THEN
        assertEquals(expectedUserInfo, obtainedUserInfo);
    }

    @Test
    public void testGetAccountInfoCaseNoneFound() {
        // GIVEN
        int idUser = 1;
        Document expectedUserInfo = new Document("msg", "Erreur: non trouvé");
        userMock.when(() -> UserRepository.getUserInfoById(idUser)).thenReturn(expectedUserInfo);

        // WHEN
        Document obtainedUserInfo = userController.getAccountInfo(idUser);

        // THEN
        assertEquals(expectedUserInfo, obtainedUserInfo);
    }

    @Test
    public void testConnectAccount() {
        // GIVEN
        Map<String, String> givenUser = new HashMap<String, String>();
        givenUser.put("pseudo", "test");
        givenUser.put("password", "test");

        // WHEN
        userController.connectAccount(givenUser);

        // THEN
        userMock.verify(() -> UserRepository.getFromPseudoAndPassword(givenUser.get("pseudo"),
                givenUser.get("password")), Mockito.times(1));
    }

    @Test
    public void testConnectAccountCaseNoneFound() {
        // GIVEN
        Map<String, String> givenUser = new HashMap<String, String>();
        givenUser.put("pseudo", "test");
        givenUser.put("password", "test");
        userMock.when(() -> UserRepository.getFromPseudoAndPassword(givenUser.get("pseudo"),
                givenUser.get("password"))).thenReturn(new Document("_id", -1));

        // WHEN
        userController.connectAccount(givenUser);

        // THEN
        userMock.verify(() -> UserRepository.getFromPseudoAndPassword(givenUser.get("pseudo"),
                givenUser.get("password")), Mockito.times(1));
    }
}
