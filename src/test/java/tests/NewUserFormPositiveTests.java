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
    public Object[] provideUserWithMandatoryFields() {
        UserGenerator userGenerator = new UserGenerator();
        User user = userGenerator.generateUserWithMandatoryFields();
        return new Object[]{user};
    }

    @DataProvider
    public Iterator<Object[]> provideUsersWithMandatoryAndOptionalFields() {
        List<User> users = new ArrayList<>();
        UserGenerator userGenerator = new UserGenerator();
        users.add(userGenerator.generateUserWithMandatoryFields());
        users.add(userGenerator.generateUserWithFullName());
        users.add(userGenerator.generateUserWithEmail());
        users.add(userGenerator.generateUserWithJabber());
        users.add(userGenerator.generateUserWithAllOptionalFields());
        return users.stream().map((g) -> new Object[]{g}).collect(Collectors.toList()).iterator();
    }

    @DataProvider
    public Object[] provideUserWithSpecialSymbols() {
        UserGenerator userGenerator = new UserGenerator();
        User user = userGenerator.generateUserWithSpecialSymbolsInAllFields();
        return new Object[]{user};
    }

    @DataProvider
    public Iterator<Object[]> provideUsersWithMinFieldsLength() {
        List<User> users = new ArrayList<>();
        UserGenerator userGenerator = new UserGenerator();
        users.add(userGenerator.generateUsersWithMinFieldLengthExceptLogin());
        users.add(userGenerator.generateUsersWithMinFieldLengthExceptFullName());
        users.add(userGenerator.generateUsersWithMinFieldLengthExceptEmail());
        return users.stream().map((g) -> new Object[]{g}).collect(Collectors.toList()).iterator();
    }

    @DataProvider
    public Iterator<Object[]> provideUsersWithMaxAndMoreFieldsLength() {
        List<User> users = new ArrayList<>();
        UserGenerator userGenerator = new UserGenerator();
        users.add(userGenerator.generateUsersWithMaxFieldsLength());
        users.add(userGenerator.generateUsersWithMoreThanMaxFieldsLength());
        return users.stream().map((g) -> new Object[]{g}).collect(Collectors.toList()).iterator();
    }


    /* check that users with diferent options are created and user info on users page is correct:
    case 1: only mandatory fields are filled in (login/password/repeat password)
    case 2: mandatory fields + 1 optional (full name) are filled in
    case 3: mandatory fields + 1 optional (email) are filled in
    case 4: mandatory fields + 1 optional (jabber) are filled in
    case 5: mandatory fields + all optional (full name/email/jabber) are filled in */
    @Test(dataProvider = "provideUsersWithMandatoryAndOptionalFields")
    public void createNewUser(User user) {
        createUser(user);
        // if user full name is empty, then login name is shown instead on all pages, so assigning login name to full name field before asserts
        if (user.getFullName() == null) {
            user.setFullName(user.getLogin());
        }
        // check created user name on edit page automatically opened after user creation
        String userNameFromUserEditPage = app.editUserPage.getUserName();
        Assert.assertEquals(userNameFromUserEditPage, user.getFullName(), "user name doesn't match");
        // find created user using search form and check his info in the users list (login, full name, email/jabber)
        app.navigateToUsersPageViaMenu();
        Assert.assertTrue(app.manageUsersPage.isUserFoundByLogin(user));
        User createdUserInfo = app.manageUsersPage.getUserInfoForProvidedLogin(user);
        Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
    }

    /* edge case test: -> GREY AREA - REQUIREMENTS ARE NOT SPECIFIED - TEST FAILS ON USER INFO ASSERTION
    test for special symbols "!±@#$%^&*()-_=+{}[];:\"'|\\<>,.?/~`"
    shows that they are allowed in every field (only login doesn't allow "<>/" and space)
    (corresponding negative test for login with special symbols is in negative tests)*/
    @Test(dataProvider = "provideUserWithSpecialSymbols")
    public void createUserWithSpecialSymbols(User user) {
        createUser(user);
        // check created user name on edit page automatically opened after user creation
        String userNameFromUserEditPage = app.editUserPage.getUserName();
        Assert.assertEquals(userNameFromUserEditPage, user.getFullName(), "user name doesn't match");
        // find created user using search form and check his info in the users list (login, full name, email/jabber)
        app.navigateToUsersPageViaMenu();
        //extract part of login without special symbols as search with special symbols doesn't work properly
        String partOfLogin = user.getLogin().substring(0, user.getLogin().indexOf('!'));
        Assert.assertTrue(app.manageUsersPage.isUserFoundByString(partOfLogin));
        User createdUserInfo = app.manageUsersPage.getUserInfoBy(partOfLogin);
        Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
    }

    //edge case: creates user with minimal fields length (=1 symbol) -> GREY AREA, MIN LENGTH IS NOT SPECIFIED AND ACCEPTED AS 1 SYMBOL
    @Test(dataProvider = "provideUsersWithMinFieldsLength")
    public void createUserWithMinFieldsLength(User user) {
        createUser(user);
        // check created user name on edit page automatically opened after user creation
        String userNameFromUserEditPage = app.editUserPage.getUserName();
        Assert.assertEquals(userNameFromUserEditPage, user.getFullName(), "user name doesn't match");
        // find created user using search form and check his info in the users list (login, full name, email/jabber)
        app.navigateToUsersPageViaMenu();
        //search by non 1 symbol field to provide search result uniqueness
        if (user.getLogin().length() > 1) {
            Assert.assertTrue(app.manageUsersPage.isUserFoundByLogin(user));
            User createdUserInfo = app.manageUsersPage.getUserInfoForProvidedLogin(user);
            Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
        }
        if (user.getEmail().length() > 1) {
            Assert.assertTrue(app.manageUsersPage.isUserFoundByEmail(user));
            User createdUserInfo = app.manageUsersPage.getUserInfoForProvidedEmail(user);
            Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
        }
        if (user.getFullName().length() > 1) {
            Assert.assertTrue(app.manageUsersPage.isUserFoundByFullName(user));
            User createdUserInfo = app.manageUsersPage.getUserInfoForProvidedFullName(user);
            Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
        }
    }

    /* edge case: create user with max allowed login/full name fields (=50 symbols) and more than max (>50)
    GREY AREA, MAX LENGTH IS SPECIFIED ONLY FOR LOGIN ANF FULL NAME AS 50 SYMBOLS, FOR ALL OTHER FIELDS NO LIMITATION
    case 1: all fields length = 50
    case 2: all fields length > 50 (but login and full name are auto-truncated till 50 symbols) */
    @Test(dataProvider = "provideUsersWithMaxAndMoreFieldsLength")
    public void createUserWithMaxFieldsLength(User user) {
        int maxFieldLenght = 50;
        createUser(user);
        //prepare user for comparison: truncate login/email to 50 symbols as they will be truncated on UI
        user.setLogin(user.getLogin().substring(0, maxFieldLenght));
        user.setFullName(user.getFullName().substring(0, maxFieldLenght));
        // check created user name on edit page automatically opened after user creation
        String userNameFromUserEditPage = app.editUserPage.getUserName();
        Assert.assertEquals(userNameFromUserEditPage, user.getFullName(), "user name doesn't match");
        // find created user using search form and check his info in the users list (login, full name, email/jabber)
        app.navigateToUsersPageViaMenu();
        Assert.assertTrue(app.manageUsersPage.isUserFoundByLogin(user));
        User createdUserInfo = app.manageUsersPage.getUserInfoForProvidedLogin(user);
        Assert.assertEquals(createdUserInfo, user, "user info doesn't match!");
    }

    //check that user is not created when clicking Cancel on New User Form
    @Test(dataProvider = "provideUserWithMandatoryFields")
    public void cancelUserCreation(User user) {
        app.navigateToUsersPageViaMenu();
        app.manageUsersPage.openNewUserForm();
        app.newUserForm.fillInUserCreationForm(user, false);
        app.newUserForm.cancelUserCreation();
        Assert.assertFalse(app.manageUsersPage.isUserFoundByLogin(user));
    }

    private void createUser(User user) {
        app.navigateToUsersPageViaMenu();
        app.manageUsersPage.openNewUserForm();
        app.newUserForm.fillInUserCreationForm(user, false);
        app.newUserForm.submitUserCreation();
    }

    @AfterMethod
    // delete test user after each creation
    public void teardown(Object[] parameters) {
        User user = (User) parameters[0];
        app.navigateToUsersPage();
        app.manageUsersPage.deleteUserIfExist(user);
    }

}
