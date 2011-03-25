/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */


package com.sun.xml.ws.security.impl.kerberos;

/* JAAS imports */
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/* Java imports */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * <p>
 * Prompts and reads from the command line for answers to authentication
 * questions.
 * This can be used by a JAAS application to instantiate a
 * CallbackHandler
 * @see javax.security.auth.callback
 */
public class TextCallbackHandler implements CallbackHandler {

    /**
     * <p>Creates a callback handler that prompts and reads from the
     * command line for answers to authentication questions.
     * This can be used by JAAS applications to instantiate a
     * CallbackHandler.

     */
    public TextCallbackHandler() {
    }

    /**
     * Handles the specified set of callbacks.
     *
     * @param callbacks the callbacks to handle
     * @throws IOException if an input or output error occurs.
     * @throws UnsupportedCallbackException if the callback is not an
     * instance of NameCallback or PasswordCallback
     */
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        ConfirmationCallback confirmation = null;

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {
                TextOutputCallback tc = (TextOutputCallback) callbacks[i];

                String text;
                switch (tc.getMessageType()) {
                    case TextOutputCallback.INFORMATION:
                        text = "";
                        break;
                    case TextOutputCallback.WARNING:
                        text = "Warning: ";
                        break;
                    case TextOutputCallback.ERROR:
                        text = "Error: ";
                        break;
                    default:
                        throw new UnsupportedCallbackException(
                                callbacks[i], "Unrecognized message type");
                }

                String message = tc.getMessage();
                if (message != null) {
                    text += message;
                }
                if (text != null) {
                    System.err.println(text);
                }

            } else if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback) callbacks[i];

                if (nc.getDefaultName() == null) {
                    System.err.print(nc.getPrompt());
                } else {
                    System.err.print(nc.getPrompt()
                            + " [" + nc.getDefaultName() + "] ");
                }
                System.err.flush();

                String result = readLine();
                if (result.equals("")) {
                    result = nc.getDefaultName();
                }

                nc.setName(result);

            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callbacks[i];

                System.err.print(pc.getPrompt());
                System.err.flush();

                pc.setPassword(Password.readPassword(System.in));

            } else if (callbacks[i] instanceof ConfirmationCallback) {
                confirmation = (ConfirmationCallback) callbacks[i];

            } else {
                throw new UnsupportedCallbackException(
                        callbacks[i], "Unrecognized Callback");
            }
        }

        /* Do the confirmation callback last. */
        if (confirmation != null) {
            doConfirmation(confirmation);
        }
    }

    /* Reads a line of input */
    private String readLine() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    private void doConfirmation(ConfirmationCallback confirmation)
            throws IOException, UnsupportedCallbackException {
        String prefix;
        int messageType = confirmation.getMessageType();
        switch (messageType) {
            case ConfirmationCallback.WARNING:
                prefix = "Warning: ";
                break;
            case ConfirmationCallback.ERROR:
                prefix = "Error: ";
                break;
            case ConfirmationCallback.INFORMATION:
                prefix = "";
                break;
            default:
                throw new UnsupportedCallbackException(
                        confirmation, "Unrecognized message type: " + messageType);
        }

        class OptionInfo {

            String name;
            int value;

            OptionInfo(String name, int value) {
                this.name = name;
                this.value = value;
            }
        }

        OptionInfo[] options;
        int optionType = confirmation.getOptionType();
        switch (optionType) {
            case ConfirmationCallback.YES_NO_OPTION:
                options = new OptionInfo[]{
                            new OptionInfo("Yes", ConfirmationCallback.YES),
                            new OptionInfo("No", ConfirmationCallback.NO)
                        };
                break;
            case ConfirmationCallback.YES_NO_CANCEL_OPTION:
                options = new OptionInfo[]{
                            new OptionInfo("Yes", ConfirmationCallback.YES),
                            new OptionInfo("No", ConfirmationCallback.NO),
                            new OptionInfo("Cancel", ConfirmationCallback.CANCEL)
                        };
                break;
            case ConfirmationCallback.OK_CANCEL_OPTION:
                options = new OptionInfo[]{
                            new OptionInfo("OK", ConfirmationCallback.OK),
                            new OptionInfo("Cancel", ConfirmationCallback.CANCEL)
                        };
                break;
            case ConfirmationCallback.UNSPECIFIED_OPTION:
                String[] optionStrings = confirmation.getOptions();
                options = new OptionInfo[optionStrings.length];
                for (int i = 0; i < options.length; i++) {
                    options[i] = new OptionInfo(optionStrings[i], i);
                }
                break;
            default:
                throw new UnsupportedCallbackException(
                        confirmation, "Unrecognized option type: " + optionType);
        }

        int defaultOption = confirmation.getDefaultOption();

        String prompt = confirmation.getPrompt();
        if (prompt == null) {
            prompt = "";
        }
        prompt = prefix + prompt;
        if (!prompt.equals("")) {
            System.err.println(prompt);
        }

        for (int i = 0; i < options.length; i++) {
            if (optionType == ConfirmationCallback.UNSPECIFIED_OPTION) {
                // defaultOption is an index into the options array
                System.err.println(
                        i + ". " + options[i].name
                        + (i == defaultOption ? " [default]" : ""));
            } else {
                // defaultOption is an option value
                System.err.println(
                        i + ". " + options[i].name
                        + (options[i].value == defaultOption ? " [default]" : ""));
            }
        }
        System.err.print("Enter a number: ");
        System.err.flush();
        int result;
        try {
            result = Integer.parseInt(readLine());
            if (result < 0 || result > (options.length - 1)) {
                result = defaultOption;
            }
            result = options[result].value;
        } catch (NumberFormatException e) {
            result = defaultOption;
        }

        confirmation.setSelectedIndex(result);
    }
}
