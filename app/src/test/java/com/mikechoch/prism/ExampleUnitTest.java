package com.mikechoch.prism;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void testFullName() {

        assertTrue(isFullNameValid("parth bhoiwala"));
        assertTrue(isFullNameValid("Michael DiCioccio"));
        assertTrue(isFullNameValid("Shiv Shah Boggarapu"));
        assertTrue(isFullNameValid("Alpha Beta Gamma Delta"));
        assertTrue(isFullNameValid("MC'Donalds"));
        assertTrue(isFullNameValid("Le"));
        assertTrue(isFullNameValid("Satttish"));

        assertFalse(isFullNameValid("a"));
        assertFalse(isFullNameValid("'Parth"));
        assertFalse(isFullNameValid("parth. bhoiwala"));
        assertFalse(isUsernameValid(""));
        assertFalse(isFullNameValid(" "));
        assertFalse(isUsernameValid(" alpha"));
        assertFalse(isUsernameValid("parth bh0!wala"));
        assertFalse(isUsernameValid("Partttth Bhoiwala"));
        assertFalse(isUsernameValid("part~!@ bhoiwala shah shiv"));
        assertFalse(isUsernameValid("$hah"));
        assertFalse(isUsernameValid("parth-bhoiwala Shah"));
        assertFalse(isUsernameValid("mike Di''Cioccio"));

    }


    @Test
    public void testUsername() {
        assertTrue(isUsernameValid("parth.bhoiwala"));
        assertTrue(isUsernameValid("parth_bhoiwala"));
        assertTrue(isUsernameValid("parth.bhoiwala.914"));
        assertTrue(isUsernameValid("parttttth_shah"));
        assertTrue(isUsernameValid("parth_bhoiwla.shah_914"));
        assertTrue(isUsernameValid("a.alpha.b.beta_"));
        assertTrue(isUsernameValid("a_beta_alpha"));
        assertTrue(isUsernameValid("a1beta2gamma3"));
        assertTrue(isUsernameValid(""));
    }



    public boolean isFullNameValid(String fullName) {

        if (fullName.length() < 2) {
            // TODO: show error "Name must be as least 2 characters long"
            return false;
        }
        if (fullName.length() > 70) {
            // TODO: show error "Name cannot be longer than 70 characters"
            return false;
        }
        if (!Pattern.matches("^[a-zA-Z ']+", fullName)) {
            // TODO: show error "Name can only contain alphabets, space and apostrophe"
            return false;
        }
        if (Pattern.matches(".*(.)\\1{3,}.*", fullName)) {
            // TODO: show error "Name cannot have more than 3 repeating characters"
            return false;
        }
        if (Pattern.matches(".*(['])\\1{1,}.*", fullName)) {
            // TODO: show error "Name cannot have more than 1 apostrophe"
            return false;
        }
        if (!Character.isAlphabetic(fullName.charAt(0))) {
            // TODO: show error "Name must start with a letter"
            return false;
        }
        return true;
    }

    public boolean isUsernameValid(String username) {
        if (username.length() < 5) {
            // TODO: show error "Username must be as least 5 characters long"
            return false;
        }
        if (username.length() > 30) {
            // TODO: show error "Username cannot be longer than 30 characters"
            return false;
        }
        if (!Pattern.matches("^[a-z0-9._']+", username)) {
            // TODO: show error "Username can only contain letters, numbers, period and underscore"
            return false;
        }
        if (Pattern.matches(".*([a-z0-9])\\1{5,}.*", username)) {
            // TODO: show error "Username cannot have more than 3 repeating characters"
            return false;
        }
        if (Pattern.matches(".*([._])\\1{1,}.*", username)) {
            // TODO: show error "Username cannot have more than 1 repeating symbol"
            return false;
        }
        if (!Character.isAlphabetic(username.charAt(0))) {
            // TODO: show error "Username must start with a letter"
            return false;
        }
        return true;
        
    }
}