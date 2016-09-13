/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extension.elytron._private;

import static org.jboss.logging.Logger.Level.INFO;

import java.security.KeyStore;
import java.security.Provider;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;
import org.wildfly.extension.elytron.Configurable;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityRealm;

/**
 * Messages for the Elytron subsystem.
 *
 * <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@MessageLogger(projectCode = "WFLYELY", length = 5)
public interface ElytronSubsystemMessages extends BasicLogger {

    /**
     * A root logger with the category of the package name.
     */
    ElytronSubsystemMessages ROOT_LOGGER = Logger.getMessageLogger(ElytronSubsystemMessages.class, "org.wildfly.extension.elytron");

    @LogMessage(level = INFO)
    @Message(id = 1, value = "Activating Elytron Subsystem Elytron Version=%s, Subsystem Version=%s")
    void activatingElytronSubsystem(String elytronVersion, String subsystemVersion);

    /**
     * {@link OperationFailedException} if the same realm is injected multiple times for a single domain.
     *
     * @param realmName - the name of the {@link SecurityRealm} being injected.
     * @param domainName - the name of the {@link SecurityDomain} the realm is being injected for.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 2, value = "Can not inject the same realm '%s' in a single security domain '%s'.")
    OperationFailedException duplicateRealmInjection(final String realmName, final String domainName);

    /**
     * An {@link IllegalArgumentException} if the supplied operation did not contain an address with a value for the required key.
     *
     * @param key - the required key in the address of the operation.
     * @return The {@link IllegalArgumentException} for the error.
     */
    @Message(id = 3, value = "The operation did not contain an address with a value for '%s'.")
    IllegalArgumentException operationAddressMissingKey(final String key);

    /**
     * A {@link StartException} if it is not possible to initialise the {@link Service}.
     *
     * @param cause the cause of the failure.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 4, value = "Unable to start the service.")
    StartException unableToStartService(@Cause Exception cause);

    /**
     * An {@link OperationFailedException} if it is not possible to access the {@link KeyStore} at RUNTIME.
     *
     * @param cause the underlying cause of the failure
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 5, value = "Unable to access KeyStore to complete the requested operation.")
    OperationFailedException unableToAccessKeyStore(@Cause Exception cause);

    /**
     * An {@link OperationFailedException} for operations that are unable to populate the result.
     *
     * @param cause the underlying cause of the failure.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 6, value = "Unable to populate result.")
    OperationFailedException unableToPopulateResult(@Cause Exception cause);

    /**
     * An {@link OperationFailedException} where an operation can not proceed as it's required service is not UP.
     *
     * @param serviceName the name of the service that is required.
     * @param state the actual state of the service.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 7, value = "The required service '%s' is not UP, it is currently '%s'.")
    OperationFailedException requiredServiceNotUp(ServiceName serviceName, State state);

    /**
     * An {@link OperationFailedException} where the name of the operation does not match the expected names.
     *
     * @param actualName the operation name contained within the request.
     * @param expectedNames the expected operation names.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 8, value = "Invalid operation name '%s', expected one of '%s'")
    OperationFailedException invalidOperationName(String actualName, String... expectedNames);

    /**
     * An {@link OperationFailedException} where an operation can not be completed.
     *
     * @param cause the underlying cause of the failure.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 9, value = "Unable to complete operation.")
    OperationFailedException unableToCompleteOperation(@Cause Throwable cause);

    /**
     * An {@link OperationFailedException} where this an attempt to save a KeyStore without a File defined.
     *
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 10, value = "Unable to complete operation.")
    OperationFailedException cantSaveWithoutFile();

    /**
     * A {@link StartException} for when provider registration fails due to an existing registration.
     *
     * @param name the name of the provider registration failed for.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 11, value = "A Provider is already registered for '%s'")
    StartException providerAlreadyRegistered(String name);

    /**
     * A {@link StartException} where a service can not identify a suitable {@link Provider}
     *
     * @param type the type being searched for.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 12, value = "No suitable provider found for type '%s'")
    StartException noSuitableProvider(String type);

    /**
     * A {@link OperationFailedException} for when an attempt is made to define a domain that has a default realm specified that
     * it does not actually reference.
     *
     * @param defaultRealm the name of the default-realm specified.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 13, value = "The default-realm '%s' is not in the list or realms referenced by this domain.")
    OperationFailedException defaultRealmNotReferenced(String defaultRealm);

    /**
     * A {@link StartException} for when the properties file backed realm can not be started due to problems loading the
     * properties files.
     *
     * @param cause the underlying cause of the error.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 14, value = "Unable to load the properties files required to start the properties file backed realm.")
    StartException unableToLoadPropertiesFiles(@Cause Exception cause);

    /**
     * A {@link StartException} where a custom component has been defined with configuration but does not implement
     * the {@link Configurable} interface.
     *
     * @param className the class name of the custom component implementation being loaded.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 15, value = "The custom component implementation '%s' doe not implement 'org.wildfly.extension.elytron.Configurable' however configuration has been supplied.")
    StartException componentNotConfigurable(final String className);

    /**
     * An {@link OperationFailedException} where validation of a specified regular expression has failed.
     *
     * @param pattern the regular expression that failed validation.
     * @param cause the reported {@link Exception} during validation.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 16, value = "The supplied regular expression '%s' is invalid.")
    OperationFailedException invalidRegularExpression(String pattern, @Cause Exception cause);

    // id = 17 - Free to be used.

    /**
     * A {@link StartException} where a Key or Trust manager factory can not be created for a specific algorithm.
     *
     * @param type the type of manager factory being created.
     * @param algorithm the requested algorithm.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 18, value = "Unable to create %s for algorithm '%s'.")
    StartException unableToCreateManagerFactory(final String type, final String algorithm);

    /**
     * A {@link StartException} where a specific type can not be found in an injected value.
     *
     * @param type the type required.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 19, value = "No '%s' found in injected value.")
    StartException noTypeFound(final String type);

    /**
     * A {@link OperationFailedException} for when the properties file used by the realm can not be reloaded.
     *
     * @param cause the underlying cause of the error.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 20, value = "Unable to reload the properties files required to by the properties file backed realm.")
    OperationFailedException unableToReLoadPropertiesFiles(@Cause Exception cause);

    /*
     * Identity Resource Messages - 1000
     */

    @Message(id = 1000, value = "Identity with name [%s] already exists.")
    OperationFailedException identityAlreadyExists(final String principalName);

    @Message(id = 1001, value = "Could not create identity with name [%s].")
    OperationFailedException couldNotCreateIdentity(final String principalName, @Cause Exception cause);

    @Message(id = 1002, value = "Identity with name [%s] not found.")
    String identityNotFound(final String principalName);

    @Message(id = 1003, value = "Could not delete identity with name [%s].")
    OperationFailedException couldNotDeleteIdentity(final String principalName, @Cause Exception cause);

    @Message(id = 1004, value = "Identity with name [%s] not authorized.")
    String identityNotAuthorized(final String principalName);

    @Message(id = 1005, value = "Could not read identity [%s] from security domain [%s].")
    OperationFailedException couldNotReadIdentity(final String principalName, final ServiceName domainServiceName, @Cause Exception cause);

    @Message(id = 1006, value = "Unsupported password type [%s].")
    RuntimeException unsupportedPasswordType(final Class passwordType);

    @Message(id = 1007, value = "Could not read identity with name [%s].")
    OperationFailedException couldNotReadIdentity(final String principalName, @Cause Exception cause);

    @Message(id = 1008, value = "Failed to obtain the authorization identity.")
    OperationFailedException couldNotObtainAuthorizationIdentity(@Cause Exception cause);

    @Message(id = 1009, value = "Failed to add attribute.")
    OperationFailedException couldNotAddAttribute(@Cause Exception cause);

    @Message(id = 1010, value = "Failed to remove attribute.")
    OperationFailedException couldNotRemoveAttribute(@Cause Exception cause);

    @Message(id = 1011, value = "Could not create password.")
    OperationFailedException couldNotCreatePassword(@Cause Exception cause);

    @Message(id = 1012, value = "Unexpected password type [%s].")
    OperationFailedException unexpectedPasswordType(final String passwordType);


}
