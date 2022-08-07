package com.autologinplaystore;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
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
import java.util.PriorityQueue;

@RunWith(AndroidJUnit4.class)
public class AppLiveGooglePlayStoreLoginOp {
    private final String TEXT = "text";
    private final String ID = "id";
    private final String CLASS_NAME = "className";
    private final int GOOGLE_LOADING_TIMEOUT = 10000; //After signing, google perform action i.e. checking info[Depends on device and network]
    private final int GOOGLE_VERIFICATION_TIMEOUT = 5000; //i.e. page switch after verification.
    private final int SORT_TIMEOUT = 2000;// For finding element on same page which already loaded.
    private final String LOGIN_FAILED = "play store login failed";
    private final String CAPTCHA_FOUND = "captcha encountered while google login";
    private final String INCORRECT_EMAIL = "Incorrect google username";
    private final String INCORRECT_PASSWORD = "Incorrect google password";
    private final String SIGN_IN_BUTTON_MISSING = "Could not locate signIn";
    private final String VERIFICATION_REQUIRED_PROMPT = "Verification encountered";
    private final String REQUIRES2FA = "Account requires 2FA";
    private final String PASSWORD_CHANGE_PROMPT = "Password change encountered";
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
            UiObject2 signInButton = findElement("Sign in", TEXT, SORT_TIMEOUT);

            if (signInButton == null) {
                throw new UiObjectNotFoundException(SIGN_IN_BUTTON_MISSING);
            }
            signInButton.click();
            UiObject2 usernameInputField = findElement("android.widget.EditText", CLASS_NAME, GOOGLE_LOADING_TIMEOUT);
            if (usernameInputField == null) {
                throw new UiObjectNotFoundException("User Button missing.");
            }
            usernameInputField.setText(username);

            UiObject2 nextButton = findElement("Next", TEXT, SORT_TIMEOUT);
            nextButton.click();

            //  Check for password page load
            if (mDevice.wait(Until.findObject(By.text("Show password")), GOOGLE_VERIFICATION_TIMEOUT) == null) {
                // Check for incorrect email
                UiObject2 isEmailIncorrect = findElement("Couldn\'t find your Google Account", TEXT, SORT_TIMEOUT);
                if (isEmailIncorrect != null)
                    throw new AccountVerificationException(username + " not verified with google account.");
                // check for captcha
                UiObject2 captchaImage = findElement("captchaimg", ID, SORT_TIMEOUT) != null ? findElement("captchaimg", ID, SORT_TIMEOUT) : findElement("android.widget.Image", CLASS_NAME, SORT_TIMEOUT);
                if (captchaImage != null)
                    throw new AccountVerificationException("Captcha verification found.");
                throw new UiObjectNotFoundException(LOGIN_FAILED);
            }

            UiObject2 passwordInputField = mDevice.wait(Until.findObject(By.clazz("android.widget.EditText")), SORT_TIMEOUT); // Page already loaded for show password[SORT_TIMEOUT]
            passwordInputField.setText(password);


            if (osVersion < 5) {
                nextButton = findElement("com.google.android.gsf.login:id/next_button", ID, SORT_TIMEOUT); // Page already loaded for show password[SORT_TIMEOUT]
            } else {
                nextButton = findElement("Next", TEXT, SORT_TIMEOUT); // Page already loaded for show password[SORT_TIMEOUT]
            }
            if (nextButton == null) {
                throw new UiObjectNotFoundException(LOGIN_FAILED);
            }
            nextButton.click();

            boolean isPasswordAuthenticated = false;
            UiObject2 isAcceptButton = null;
            UiObject2 termsOfService = null;

            if (osVersion < 5) {
                termsOfService = mDevice.wait(Until.findObject(By.res("com.google.android.gsf.login", "android:id/button1")), GOOGLE_VERIFICATION_TIMEOUT);
                if (termsOfService != null) {
                    termsOfService.click();
                    isPasswordAuthenticated = true;
                }
            } else {
                isAcceptButton = findElement("I agree", TEXT, GOOGLE_VERIFICATION_TIMEOUT);
                if (isAcceptButton != null) {
                    termsOfService = findElement("signinconsentNext", ID, SORT_TIMEOUT);
                    isPasswordAuthenticated = true;
                    if (termsOfService != null) {
                        termsOfService.click();
                    }
                    isAcceptButton.click();
                }
            }
            if (!isPasswordAuthenticated) {
                checkPostPasswordResponse();
            }
        } catch (Exception e) {
            out(e.getMessage() + 0);
            throw e;
        }
    }


    private void checkPostPasswordResponse() throws AccountVerificationException, IncorrectCredException, PasswordChangeException, Requires2FAException, CaptchaFoundException, UiObjectNotFoundException {

        boolean isVerificationPrompt = false;
        boolean isPasswordChangePrompt = false;
        boolean isEmailIncorrect = false;
        boolean isPasswordCorrect = false;
        boolean captchaImage = false;
        boolean is2FARequired = false;
        int verySortTimeOut = 1000;


        // check for incorrect password
        isPasswordCorrect = findElement("Show password", TEXT, verySortTimeOut) != null;
        if (isPasswordCorrect)
            throw new IncorrectCredException(INCORRECT_PASSWORD);

        out("Incorrect password+ " + isPasswordCorrect);

        // check for verification prompt
        isVerificationPrompt = findElement("Verify it's you|Verify that it's you", TEXT, verySortTimeOut) != null;
        if (isVerificationPrompt)
            throw new AccountVerificationException(VERIFICATION_REQUIRED_PROMPT);

        // check for password change required
        isPasswordChangePrompt = findElement("Change password", TEXT, verySortTimeOut) != null; // Because already loaded
        if (isPasswordChangePrompt)
            throw new PasswordChangeException(PASSWORD_CHANGE_PROMPT);

        // check for incorrect email
        isEmailIncorrect = findElement("Couldn\'t find your Google Account", TEXT, verySortTimeOut) != null;
        if (isEmailIncorrect)
            throw new IncorrectCredException(INCORRECT_EMAIL);

        // check for 2FA
        is2FARequired = findElement("(?i)2-step verification", TEXT, verySortTimeOut) != null;
        if (is2FARequired)
            throw new Requires2FAException(REQUIRES2FA);

        // check for captcha
        captchaImage = findElement("captchaimg", ID, verySortTimeOut) != null ? true : findElement("android.widget.Image", CLASS_NAME, verySortTimeOut) != null;
        if (captchaImage) {
            throw new CaptchaFoundException(CAPTCHA_FOUND);
        }
        throw new UiObjectNotFoundException(LOGIN_FAILED);
    }

    private UiObject2 findElement(String identifier, String findBy, int timeOut) {
        UiObject2 element = null;
        if (findBy.equals(TEXT)) {
            element = mDevice.wait(Until.findObject(By.text(identifier)), timeOut);
        } else if (findBy.equals((ID))) {
            element = mDevice.wait(Until.findObject(By.res(identifier)), timeOut);
        } else if (findBy.equals(CLASS_NAME)) {
            element = mDevice.wait(Until.findObject(By.clazz(identifier)), timeOut);
        }
        return element;
    }

    protected void out(String str) {
        Log.d(" Devices logs :: ", str);
    }
}
/*
* Command
* adb -s adb-RZ8M91JT34M-osg5yz._adb-tls-connect._tcp. shell am instrument -w -r -e debug false -e class com.autologinplaystore.AppLiveGooglePlayStoreLoginOp com.autologinplaystore.test/androidx.test.runner.AndroidJUnitRunner
* */