package data;

import java.util.Random;

public class UserGenerator {
    private static final int MIN_KEYFIELD_LENGTH = 1;
    private static final int MAX_KEYFIELD_LENGTH = 50;

    //user with mandatory fields only
    public User generateUserWithMandatoryFields() {
        Random rand = new Random();
        User user = new User();
        user.setLogin(String.format("login-%s_%s", rand.nextInt(500), System.currentTimeMillis()));
        user.setPassword("testpassword");
        user.setRepeatPassword("testpassword");
        return user;
    }

    //user with optional field: full name
    public User generateUserWithFullName() {
        Random rand = new Random();
        User user = generateUserWithMandatoryFields();
        user.setFullName("firstname" + rand.nextInt(500) + " secondname" + System.currentTimeMillis());
        return user;
    }

    //user with optional field: email
    public User generateUserWithEmail() {
        User user = generateUserWithMandatoryFields();
        user.setEmail(System.currentTimeMillis() + "email@google.com");
        return user;
    }

    //user with optional field: jabber
    public User generateUserWithJabber() {
        User user = generateUserWithMandatoryFields();
        user.setJabber(System.currentTimeMillis() + "user@jabber.org");
        return user;
    }

    //user with all optional fields
    public User generateUserWithAllOptionalFields() {
        Random rand = new Random();
        User user = generateUserWithMandatoryFields();
        user.setFullName("firstname" + rand.nextInt(500) + " secondname" + System.currentTimeMillis());
        user.setEmail(System.currentTimeMillis() + "email@google.com");
        user.setJabber(System.currentTimeMillis() + "user@jabber.org");
        return user;
    }

    //user with 1 symbol in every field except login
    //why these except cases: to be able to find the user later by this field, as search by 1 symbol will return many other users
    public User generateUsersWithMinFieldLengthExceptLogin() {
        Random rand = new Random();
        User user = generateUserWithAllFieldsOfLength(MIN_KEYFIELD_LENGTH);
        user.setLogin(String.format("login-%s_%s", rand.nextInt(500), System.currentTimeMillis()));
        return user;
    }

    //user with 1 symbol in every field except email
    public User generateUsersWithMinFieldLengthExceptEmail() {
        User user = generateUserWithAllFieldsOfLength(MIN_KEYFIELD_LENGTH);
        user.setEmail(System.currentTimeMillis() + "email@google.com");
        return user;
    }

    //user with 1 symbol in every field except email
    public User generateUsersWithMinFieldLengthExceptFullName() {
        User user = generateUserWithAllFieldsOfLength(MIN_KEYFIELD_LENGTH);
        user.setFullName("test full name" + System.currentTimeMillis());
        return user;
    }

    //the following special symbols are allowed in all fields except for login: !±@#$%^&*()-_=+{}[];:\"'|\\<>,.?/~`;
    public User generateUserWithSpecialSymbolsInAllFields() {
        String specialSymbolsAddition = "!±@#$%^&*()-_=+{}[];:\"'|\\<>,.?/~`";
        String specialSymbolsAdditionForLogin = "!±@#$%^&*()-_=+{}[];:\"'|\\,.?~`"; //with exception of <>/
        User user = generateUserWithMandatoryFields();
        user.setLogin(user.getLogin()+specialSymbolsAdditionForLogin);
        user.setPassword(specialSymbolsAddition);
        user.setRepeatPassword(specialSymbolsAddition);
        user.setFullName(specialSymbolsAddition);
        user.setEmail(specialSymbolsAddition + "email@google.com");
        user.setJabber(specialSymbolsAddition + "user@jabber.org");
        return user;
    }

    //generates user with each field = 50 symbols (this is max for login/full name)
    public User generateUsersWithMaxFieldsLength() {
        return generateUserWithAllFieldsOfLength(MAX_KEYFIELD_LENGTH);
    }

    //generates user with more than max fields length ( >50 symbols)
    public User generateUsersWithMoreThanMaxFieldsLength() {
        return generateUserWithAllFieldsOfLength(MAX_KEYFIELD_LENGTH*2);
    }

    // provides user with fields of defined length
    private User generateUserWithAllFieldsOfLength(int fieldsLength) {
        User user = new User();
        user.setLogin(generateRandomStringOfLenght(fieldsLength));
        user.setPassword(generateRandomStringOfLenght(fieldsLength));
        user.setRepeatPassword(user.getPassword());
        user.setFullName(generateRandomStringOfLenght(fieldsLength));
        user.setEmail(generateRandomStringOfLenght(fieldsLength));
        user.setJabber(generateRandomStringOfLenght(fieldsLength));
        return user;
    }

    //email != Username@domain.extension
    public User generateUserWithInvalidEmailFormat() {
        User user = generateUserWithMandatoryFields();
        user.setEmail(System.currentTimeMillis() + "testemail");
        return user;
    }

    //jabber != Username@domain.extension
    public User generateUserWithInvalidJabberFormat() {
        User user = generateUserWithMandatoryFields();
        user.setJabber(System.currentTimeMillis() + "testjabber");
        return user;
    }

    private String generateRandomStringOfLenght(int length) {
        String chars = "qwertuiopasdfghjklzxcvbnm01233456789";
        Random rand = new Random();
        StringBuilder generatedString = new StringBuilder();
        while (generatedString.length() < length) {
            generatedString.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return generatedString.toString();
    }

}
