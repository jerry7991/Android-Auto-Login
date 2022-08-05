package com.autologinplaystore;

import static java.lang.Thread.sleep;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.uiautomation.AccountVerificationException;
import com.uiautomation.CaptchaFoundException;
import com.uiautomation.IncorrectCredException;
import com.uiautomation.PasswordChangeException;
import com.uiautomation.Requires2FAException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class AppLiveGooglePlayStoreLogin {
    private final String TEXT = "text";
    private final String ID = "id";
    private final String CLASS_NAME = "className";
    private final String DESCRIPTION = "description";
    private final int FIND_ELEMENT_RETRIES = 10;
    private final int DELAYED_FIND_ELEMENT_RETRIES = 20;
    private final int ON_ERROR_FIND_ELEMENT_RETRIES = 1;
    private final int LAUNCH_TIMEOUT = 5000;
    private final String LOGIN_FAILED = "play store login failed";
    private final String CAPTCHA_FOUND = "captcha encountered while google login";
    private final String INCORRECT_EMAIL = "Incorrect google username";
    private final String INCORRECT_PASSWORD = "Incorrect google password";
    private final String SIGN_IN_BUTTON_MISSING = "Could not locate signIn";
    private final String VERIFICATION_REQUIRED_PROMPT = "Verification encountered";
    private final String REQUIRES2FA = "Account requires 2FA";
    private final String PASSWORD_CHANGE_PROMPT = "Password change encountered";
    private final String PASSWORD_PROMPT_AGAIN = "Asking for password";
    long startTime;
    private UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    @Before
    public void setUiAutomationEnvironment() {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList("adb shell settings put global window_animation_scale 0.0",
                "adb shell settings put global transition_animation_scale 0.0",
                "adb shell settings put global animator_duration_scale 0.0"));
        try {
            Log.d("setUiAutomationEnvironment", "starting adb environment");
            startTime = System.currentTimeMillis();
            Log.d("starting at the time ", startTime + "");
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void resetUiAutomationEnvironment() {
        startTime = System.currentTimeMillis() - startTime;
        Log.d("Time taken ", startTime + "");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLogin() throws Exception {
        out("Going to set waitForIdleTimeout to 0");
        try {
            String username = "faltuadmihu@gmail.com";
            String password = "FaltuAdmi";


            float osVersion = 11;

            UiObject signInButton = osVersion < 5 ? findElement("Existing", FIND_ELEMENT_RETRIES, TEXT) : findElement("(?i)sign in", FIND_ELEMENT_RETRIES, TEXT);
            if (uiObjectNotEmpty(signInButton)) {
                out("sign in button found...");
                boolean signButtonClicked = signInButton.click();
                out("signButtonClicked  status : " + signButtonClicked + 0);
            } else {
                out("sign in button was missing...");
                throw new UiObjectNotFoundException(SIGN_IN_BUTTON_MISSING);
            }


            UiObject usernameInputField = osVersion < 5 ? findElement("com.google.android.gsf.login:id/username_edit", DELAYED_FIND_ELEMENT_RETRIES, ID) : findElement("android.widget.EditText", DELAYED_FIND_ELEMENT_RETRIES, CLASS_NAME);
            if (uiObjectNotEmpty(usernameInputField)) {
//                usernameInputField.click();
                usernameInputField.setText(username);
            } else {
                out(" failed to find username input field");
                throw new UiObjectNotFoundException(LOGIN_FAILED);
            }


            UiObject nextButton = uiObjectNotEmpty(findElement("(?i)next", FIND_ELEMENT_RETRIES, TEXT)) ? findElement("(?i)next", FIND_ELEMENT_RETRIES, TEXT) : findElement("identifierNext", FIND_ELEMENT_RETRIES, ID);
            if (uiObjectNotEmpty(nextButton)) {
                nextButton.click();
            }

            UiObject passwordInputField = osVersion < 5 ? findElement("com.google.android.gsf.login:id/password_edit", FIND_ELEMENT_RETRIES, ID) : findElement("android.widget.EditText", DELAYED_FIND_ELEMENT_RETRIES, CLASS_NAME);

            if (uiObjectNotEmpty(passwordInputField)) {
                passwordInputField.click();
                passwordInputField.setText(password);
            } else {
                // Check for incorrect email
                UiObject isEmailIncorrect = findElement("Couldn\'t find your Google Account", FIND_ELEMENT_RETRIES, TEXT);

                // check for captcha
                UiObject captchaImage = uiObjectNotEmpty(findElement("captchaimg", FIND_ELEMENT_RETRIES, ID)) ? findElement("captchaimg", FIND_ELEMENT_RETRIES, ID) : findElement("android.widget.Image", FIND_ELEMENT_RETRIES, CLASS_NAME);

                throw new UiObjectNotFoundException(LOGIN_FAILED);
            }

            if (osVersion < 5) {
                nextButton = findElement("com.google.android.gsf.login:id/next_button", FIND_ELEMENT_RETRIES, ID);
            } else {
                nextButton = uiObjectNotEmpty(findElement("(?i)next", FIND_ELEMENT_RETRIES, TEXT)) ? findElement("(?i)next", FIND_ELEMENT_RETRIES, TEXT) : findElement("passwordNext", FIND_ELEMENT_RETRIES, ID);
            }

            if (uiObjectNotEmpty(nextButton)) {
                nextButton.click();
            } else {
                throw new UiObjectNotFoundException(LOGIN_FAILED);
            }

            UiObject termsOfService = null;
            UiObject isAcceptButton = null;

            if (osVersion < 5) {
                termsOfService = new UiObject(new UiSelector().className("android.widget.Button").packageName("com.google.android.gsf.login").resourceIdMatches("android:id/button1"));
            } else {
                String acceptButtonText = "I agree";
                for (int i = 0; i < FIND_ELEMENT_RETRIES; i++) {
                    termsOfService = findElement(acceptButtonText, ON_ERROR_FIND_ELEMENT_RETRIES, TEXT);
                    if (!uiObjectNotEmpty(termsOfService)) {
                        termsOfService = findElement("signinconsentNext", ON_ERROR_FIND_ELEMENT_RETRIES, ID);
                        if (uiObjectNotEmpty(termsOfService)) {
                            break;
                        }
                    }
                }
            }

            if (uiObjectNotEmpty(termsOfService)) {
                termsOfService.click();
//                sleep(3000);
            } else {
                boolean isVerificationPrompt = false;
                boolean isPasswordChangePrompt = false;
                boolean isEmailIncorrect = false;
                boolean isPasswordCorrect = false;
                boolean isPasswordField = false;
                boolean captchaImage = false;
                boolean is2FARequired = false;

                for (int i = 0; i < FIND_ELEMENT_RETRIES; i++) {

                    // check for verification prompt
                    isVerificationPrompt = uiObjectNotEmpty(findElement("Verify it's you|Verify that it's you", ON_ERROR_FIND_ELEMENT_RETRIES, TEXT));
                    if (isVerificationPrompt)
                        throw new AccountVerificationException(VERIFICATION_REQUIRED_PROMPT);

                    // check for password change required
                    isPasswordChangePrompt = uiObjectNotEmpty(findElement("Change password", ON_ERROR_FIND_ELEMENT_RETRIES, TEXT));
                    if (isPasswordChangePrompt)
                        throw new PasswordChangeException(PASSWORD_CHANGE_PROMPT);

                    // check for incorrect email
                    isEmailIncorrect = uiObjectNotEmpty(findElement("Couldn\'t find your Google Account", ON_ERROR_FIND_ELEMENT_RETRIES, TEXT));
                    if (isEmailIncorrect)
                        throw new IncorrectCredException(INCORRECT_EMAIL);

                    // check for incorrect password
                    isPasswordCorrect = uiObjectNotEmpty(findElement("Wrong password. Try again or click Forgot password to reset it.", ON_ERROR_FIND_ELEMENT_RETRIES, TEXT));
                    if (isPasswordCorrect)
                        throw new IncorrectCredException(INCORRECT_PASSWORD);

                    // check for 2FA
                    is2FARequired = uiObjectNotEmpty(findElement("(?i)2-step verification", ON_ERROR_FIND_ELEMENT_RETRIES, TEXT));
                    if (is2FARequired)
                        throw new Requires2FAException(REQUIRES2FA);

                    // check for captcha
                    captchaImage = uiObjectNotEmpty(findElement("captchaimg", ON_ERROR_FIND_ELEMENT_RETRIES, ID)) ? true : uiObjectNotEmpty(findElement("android.widget.Image", ON_ERROR_FIND_ELEMENT_RETRIES, CLASS_NAME));
                    if (captchaImage) {
                        throw new CaptchaFoundException(CAPTCHA_FOUND);
                    }

                    // check for still asking for password
                    isPasswordField = uiObjectNotEmpty(findElement("Welcome", ON_ERROR_FIND_ELEMENT_RETRIES, TEXT));
                    if (isPasswordField)
                        throw new UiObjectNotFoundException(PASSWORD_PROMPT_AGAIN);
                }
                throw new UiObjectNotFoundException(LOGIN_FAILED);
            }
        } catch (Exception e) {
            out(e.getMessage() + 0);
            throw e;
        } finally {
            mDevice.pressHome();
        }
    }

    private UiObject findElement(String identifier, int timeout, String findBy) throws InterruptedException {
        UiObject element = null;
        for (int i = 0; i < timeout && !uiObjectNotEmpty(element); i++) {
            out("Finding element " + identifier + " i: " + i);
            if (findBy.equals(TEXT)) {
                out("finding element by text with : " + identifier + 0);
                element = new UiObject(new UiSelector().textMatches(identifier));
                out("Element : " + identifier + " element : " + element.toString());
                if (element == null) {
                    String newIdentifier = identifier.substring(0, 1).toUpperCase() + identifier.substring(1).toLowerCase();
                    element = new UiObject(new UiSelector().textMatches(newIdentifier));
                }
            } else if (findBy.equals(ID))
                element = new UiObject(new UiSelector().resourceIdMatches(identifier));
            else if (findBy.equals(CLASS_NAME))
                element = new UiObject(new UiSelector().className(identifier));
            else if (findBy.equals(DESCRIPTION))
                element = new UiObject(new UiSelector().descriptionMatches(identifier));
            if (uiObjectNotEmpty(element)) {
                break;
            }
            sleep(1000);
        }
        return element;
    }

    private boolean uiObjectNotEmpty(UiObject element) {
        return element != null && element.exists();
    }

    private void printLogWithTime(String method, long startTime) {
        out(method + " took " + (System.currentTimeMillis() - startTime) + " ms" + 0);
    }

    protected void startActivity(String package_name) throws IOException {
        // Launch the app
        mDevice.executeShellCommand("am start " + package_name);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(package_name).depth(0)), LAUNCH_TIMEOUT);
    }

    protected void out(String str) {
        Log.d(" Devices logs :: ", str);
    }
}