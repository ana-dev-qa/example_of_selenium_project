package tests;

import data.User;
import data.UserGenerator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UsersPageAccessTests extends BaseTest {
    private final String NO_PAGE_PERMISSION_MSG = "You have no permissions to view this page";
    private User nonAdminUser;

    @BeforeMethod
    public void createUserWithoutPermissions() {
        UserGenerator userGenerator = new UserGenerator();
        nonAdminUser = userGenerator.generateUserWithMandatoryFields();
        app.loginAsRoot();
        app.navigateToUsersPageViaMenu();
        app.manageUsersPage.openNewUserForm();
        app.newUserForm.fillInUserCreationForm(nonAdminUser, false);
        app.newUserForm.submitUserCreation();
    }

    //checks that not permitted user cannot access "Create Users" page
    @Test
    public void tryToAccessCreatePageWithoutPermission() {
        app.navigateToLoginPage();
        app.loginPage.login(nonAdminUser.getLogin(), nonAdminUser.getPassword());
        app.navigateToUsersPage();
        Assert.assertEquals(app.commonElements.getErrorPageMessage(), NO_PAGE_PERMISSION_MSG, "wrong error message!");
    }

    @AfterClass
    public void removeCreatedUser() {
        app.loginAsRoot();
        app.navigateToUsersPage();
        app.manageUsersPage.deleteUserIfExist(nonAdminUser);
    }
}
