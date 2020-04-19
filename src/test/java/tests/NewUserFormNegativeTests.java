package tests;

import data.User;
import data.UserGenerator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NewUserFormNegativeTests extends BaseTest {
    @BeforeClass
    public void login() {
        app.loginAsRoot();
    }

    //expected error messages on user creation form
    private final String MISSING_PASSWORD_MSG = "Password is required!";
    private final String MISSING_LOGIN_MSG = "Login is required!";
    private final String DIFF_PASSWORDS_MSG = "Password doesn't match!";
    private final String DUPLICATE_USERLOGIN_MSG = "Value should be unique: login";
    private final String SPACE_IN_LOGIN_MSG = "Restricted character ' ' in the name";
    private final String RESTRICTED_LOGINCHARS_MSG = "login shouldn't contain characters \"<\", \"/\", \">\": login";

    @DataProvider
    public Object[] provideUserWithMandatoryFields() {
        UserGenerator userGenerator = new UserGenerator();
        User user = userGenerator.generateUserWithMandatoryFields();
        return new Object[]{user};
    }

    @DataProvider
    //the only restricted field on special symbols is login field
    //the only not allowed symbols in login name: <>/ and space
    public Iterator<Object[]> provideUserWithNotAllowedLoginChars() {
        List<User> users = new ArrayList<>();
        UserGenerator userGenerator = new UserGenerator();
        //user with "/"
        User userWithSlash = userGenerator.generateUserWithMandatoryFields();
        userWithSlash.setLogin("with/");
        users.add(userWithSlash);
        //user with "<"
        User userWithLeftAngleBracket = userGenerator.generateUserWithMandatoryFields();
        userWithLeftAngleBracket.setLogin("with<");
        users.add(userWithLeftAngleBracket);
        //user with ">"
        User userWithRightAngleBracket = userGenerator.generateUserWithMandatoryFields();
        userWithRightAngleBracket.setLogin("with>");
        users.add(userWithRightAngleBracket);
        return users.stream().map((g) -> new Object[]{g}).collect(Collectors.toList()).iterator();
    }

    //tests for not filled in mandatory fields

    //login name is empty
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void createNewUserWithoutLogin(User user) {
        user.setLogin(null);
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.newUserForm.getErrorMessageOnMandatoryFields();
        Assert.assertEquals(actualErrorMessage, MISSING_LOGIN_MSG, "error message doesn't match!");
    }

    //password is empty
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void createNewUserWithoutPassword(User user) {
        user.setPassword(null);
        user.setRepeatPassword(null);
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.newUserForm.getErrorMessageOnMandatoryFields();
        Assert.assertEquals(actualErrorMessage, MISSING_PASSWORD_MSG, "error message doesn't match!");
    }

    //repeat password is empty
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void createNewUserWithoutPasswordRepeat(User user) {
        user.setRepeatPassword(null);
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.newUserForm.getErrorMessageOnMandatoryFields();
        Assert.assertEquals(actualErrorMessage, DIFF_PASSWORDS_MSG, "error message doesn't match!");
    }

    //passwords don't match
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void createNewUserWithDiffPasswords(User user) {
        user.setRepeatPassword(user.getPassword() + "diff");
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.newUserForm.getErrorMessageOnMandatoryFields();
        Assert.assertEquals(actualErrorMessage, DIFF_PASSWORDS_MSG, "error message doesn't match!");
    }

    //login name contains space
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void createUserWithSpaceInLogin(User user) {
        user.setLogin("with space");
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.manageUsersPage.getPopupErrorMessage();
        Assert.assertEquals(actualErrorMessage, SPACE_IN_LOGIN_MSG, "error message doesn't match!");
    }

    //login name contains restricted characters (<>/)
    @Test(dataProvider = "provideUserWithNotAllowedLoginChars")
    public void provideUserWithNotAllowedLoginChars(User user) {
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.manageUsersPage.getPopupErrorMessage();
        Assert.assertEquals(actualErrorMessage, RESTRICTED_LOGINCHARS_MSG, "error message doesn't match!");
    }

    //duplicated user name (trying to create user with already existing login name)
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void createDuplicatedUser(User user) {
        // create user
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String userNameFromUserEditPage = app.manageUsersPage.getUserNameFromEditPage();
        Assert.assertEquals(userNameFromUserEditPage, user.getLogin(), "user name doesn't match");
        //repeat the same user creation
        startNewUserCreation(user);
        app.newUserForm.submitUserCreation();
        String actualErrorMessage = app.manageUsersPage.getPopupErrorMessage();
        Assert.assertEquals(actualErrorMessage, DUPLICATE_USERLOGIN_MSG, "error message doesn't match!");
    }

//    //check that user is not created when clicking Cancel on New User Form
//    @Test(dataProvider = "provideUserWithMandatoryFields")
//    public void cancelUserCreation(User user) {
//        startNewUserCreation(user);
//        app.newUserForm.cancelUserCreation();
//        Assert.assertFalse(app.manageUsersPage.isUserCreated(user));
//    }

    private void startNewUserCreation(User user) {
        app.navigateToUsersPage();
        app.manageUsersPage.openNewUserForm();
        app.newUserForm.fillInUserCreationForm(user, false);
    }

    @AfterMethod
    public void closePopupErrorNotification(){
        app.manageUsersPage.closeErrorTopPopup();
    }

}