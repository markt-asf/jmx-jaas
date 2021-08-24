package org.apache.markt;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class DecodeLoginModule implements LoginModule {

    private static final int MAX_PASSWORD_LENGTH = 1024;

    private volatile Subject subject;
    private volatile CallbackHandler callbackHandler;

    private volatile boolean committed = false;
    private volatile Principal principal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("No callbacj handler provided");
        }

        NameCallback nameCallback = new NameCallback("Username; ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        Callback[] callbacks = new Callback[2];
        callbacks[0] = nameCallback;
        callbacks[1] = passwordCallback;

        String username = null;
        char[] password = null;

        try {
            callbackHandler.handle(callbacks);
            username = nameCallback.getName();
            password = passwordCallback.getPassword();
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException(e.toString());
        }

        // Validate username and password
        if (Users.USERNAME.equals(username)) {
            char[] encodedPassword = Users.ENCODED_PASSWORD;
            char[] decodedPassword = Decoder.decode(encodedPassword);

            if (!constantTimeCompare(password, decodedPassword)) {
                throw new LoginException("Invalid password");
            }
        }

        // If we get this far both the user name and the password match
        principal = new SimplePrincipal(username);

        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (principal == null) {
            return false;
        }

        subject.getPrincipals().add(principal);
        // Give the subject the control role access
        subject.getPrincipals().add(new SimplePrincipal("controlRole"));

        committed = true;
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        if (principal == null) {
            return false;
        }

        if (committed) {
            logout();
        } else {
            principal = null;
        }

        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(principal);
        committed = false;
        principal = null;
        return true;
    }


    private boolean constantTimeCompare(char[] a, char[] b) {
        if (a.length > MAX_PASSWORD_LENGTH) {
            return false;
        }

        if (b.length > MAX_PASSWORD_LENGTH) {
            return false;
        }

        // Need to pad the passwords as we have the actual passwords not hashes.
        char[] paddedA = new char[MAX_PASSWORD_LENGTH];
        char[] paddedB = new char[MAX_PASSWORD_LENGTH];

        for (int i = 0; i < MAX_PASSWORD_LENGTH; i++) {
            if (i < a.length) {
                paddedA[i] = a[i];
            } else {
                paddedA[i] = 'X';
            }

            if (i < b.length) {
                paddedB[i] = b[i];
            } else {
                paddedB[i] = 'X';
            }
        }

        // Now compare
        boolean result = true;
        for (int i = 0; i < MAX_PASSWORD_LENGTH; i++) {
            if (paddedA[i] != paddedB[i]) {
                result = false;
            }
        }

        return result;
    }
}
