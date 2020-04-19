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

public class NewUserFormPositiveTests extends BaseTest {
    @BeforeClass
    public void login() {
        app.loginAsRoot();
    }

    @DataProvider
    public Iterator<Object[]> provideUsersWithMandatoryAndOptionalFields() {
        List<User> users = new ArrayList<>();
        UserGenerator userGenerator = new UserGenerator();
        users.add(userGenerator.generateUserWithMandatoryFields());
        users.add(userGenerator.generateUserWithFullName());
        users.add(userGenerator.generateUserWithEmail());
        users.add(userGenerator.generateUserWithJabber());
        users.add(userGenerator.generateUsersWithAllOptionalFields());
        return users.stream().map((g) -> new Object[]{g}).collect(Collectors.toList()).iterator();
    }

    @DataProvider
    public Object[] provideOneUserWithAllFields() {
        UserGenerator userGenerator = new UserGenerator();
        User user = userGenerator.generateUsersWithAllOptionalFields();
        String specialSymbolsAddition = "!±@#$%^&*()-_=+{}[];:\"'|\\<>,.?/~`";
        user.setPassword(user.getPassword() + specialSymbolsAddition);
        user.setRepeatPassword(user.getPassword());
        //here long name is truncated on UI so making it shorter
        user.setFullName(specialSymbolsAddition);
        user.setEmail(user.getEmail() + specialSymbolsAddition);
        user.setJabber(user.getJabber() + specialSymbolsAddition);
        return new Object[]{user};
    }


    //check that user created with different options:
    //only mandatory fields, or optional fields 1 by 1, or all optional fields are filled in
    @Test(dataProvider = "provideUsersWithMandatoryAndOptionalFields")
    public void createNewUser(User user) {
        createUser(user);
        String userNameFromUserEditPage = app.manageUsersPage.getUserNameFromEditPage();
        // check created user name on edit page automatically opened after user creation
        if (user.getFullName() != null) {
            Assert.assertEquals(userNameFromUserEditPage, user.getFullName(), "user name doesn't match");
        } else {
            Assert.assertEquals(userNameFromUserEditPage, user.getLogin(), "user name doesn't match");
        }
        // find created used and check user info in the users list (login, full name, email/jabber)
        app.navigateToUsersPage();
        Assert.assertTrue(app.manageUsersPage.isUserCreated(user));
        User createdUserInfo = app.manageUsersPage.getCreatedUserInfo(user);
        if (user.getFullName() == null) {
            user.setFullName(user.getLogin()); //if user full name is not defined, then in full name section login name is shown instead
        }
        Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
    }

    //this test shows that special symbols are allowed in every field except of login
    // (corresponding negative test for login is in negative tests)
    @Test(dataProvider = "provideOneUserWithAllFields")
    public void createUserWithSpecialSymbolsInFields(User user) {
        createUser(user);
        String userNameFromUserEditPage = app.manageUsersPage.getUserNameFromEditPage();
        // check user name on edit page automatically opened after user creation
        if (user.getFullName() != null) {
            Assert.assertEquals(userNameFromUserEditPage, user.getFullName(), "user name doesn't match");
        } else {
            Assert.assertEquals(userNameFromUserEditPage, user.getLogin(), "user name doesn't match");
        }
        // find created used and check user info in the users list (login, full name, email/jabber)
        app.navigateToUsersPage();
        Assert.assertTrue(app.manageUsersPage.isUserCreated(user));
        User createdUserInfo = app.manageUsersPage.getCreatedUserInfo(user);
        if (user.getFullName() == null) {
            user.setFullName(user.getLogin()); //if user full name is not defined, then in full name section login name is shown instead
        }
        Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
    }

    private void createUser(User user) {
        app.navigateToUsersPage();
        app.manageUsersPage.openNewUserForm();
        app.newUserForm.fillInUserCreationForm(user, false);
        app.newUserForm.submitUserCreation();
    }

    @AfterMethod
    // delete test user after each creation
    public void teardown(Object[] parameters) {
        User user = (User) parameters[0];
        app.navigateToUsersPage();
        app.manageUsersPage.deleteUser(user);
    }

}