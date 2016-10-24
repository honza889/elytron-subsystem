package org.wildfly.extension.elytron;

import org.ietf.jgss.GSSCredential;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.common.function.ExceptionSupplier;
import org.wildfly.security.SecurityFactory;
import org.wildfly.security.auth.server.HttpAuthenticationFactory;
import org.wildfly.security.auth.server.ModifiableSecurityRealm;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.credential.GSSCredentialCredential;
import org.wildfly.security.http.HttpServerAuthenticationMechanism;
import org.wildfly.security.http.HttpServerAuthenticationMechanismFactory;
import sun.security.krb5.Config;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.wildfly.security.auth.server.IdentityLocator.fromName;

/**
 * Tests of LDAP related components (excluded from their natural TestCases to prevent repeated LDAP starting)
 * @author <a href="mailto:jkalina@redhat.com">Jan Kalina</a>
 */
public class LdapTestCase extends AbstractSubsystemTest {

    private KernelServices services;

    @BeforeClass
    public static void startLdapService() {
        TestEnvironment.startLdapService();
    }

    public LdapTestCase() throws Exception {
        super(ElytronExtension.SUBSYSTEM_NAME, new ElytronExtension());
    }

    @Before
    public void initializeSubsystem() throws Exception {
        services = super.createKernelServicesBuilder(new TestEnvironment()).setSubsystemXmlResource("ldap.xml").build();
        if (!services.isSuccessfulBoot()) {
            Assert.fail(services.getBootError().toString());
        }
    }

    @Test
    public void testDirContextInsecure() throws Exception {
        ServiceName serviceNameDirContext = Capabilities.DIR_CONTEXT_RUNTIME_CAPABILITY.getCapabilityServiceName("DirContextInsecure");
        ExceptionSupplier<DirContext, NamingException> dirContextSup = (ExceptionSupplier<DirContext, NamingException>) services.getContainer().getService(serviceNameDirContext).getValue();
        DirContext dirContext = dirContextSup.get();
        Assert.assertNotNull(dirContext);
        Assert.assertEquals("org.wildfly.security.auth.realm.ldap.DelegatingLdapContext", dirContext.getClass().getName());
        dirContext.close();
    }

    @Test
    public void testDirContextSsl() throws Exception {
        ServiceName serviceNameDirContext = Capabilities.DIR_CONTEXT_RUNTIME_CAPABILITY.getCapabilityServiceName("DirContextSsl");
        ExceptionSupplier<DirContext, NamingException> dirContextSup = (ExceptionSupplier<DirContext, NamingException>) services.getContainer().getService(serviceNameDirContext).getValue();
        DirContext dirContext = dirContextSup.get();
        Assert.assertNotNull(dirContext);
        Assert.assertEquals("org.wildfly.security.auth.realm.ldap.DelegatingLdapContext", dirContext.getClass().getName());
        dirContext.close();
    }

    @Test
    public void testLdapRealm() throws Exception {
        ServiceName serviceName = Capabilities.SECURITY_REALM_RUNTIME_CAPABILITY.getCapabilityServiceName("LdapRealm");
        ModifiableSecurityRealm securityRealm = (ModifiableSecurityRealm) services.getContainer().getService(serviceName).getValue();
        Assert.assertNotNull(securityRealm);

        RealmIdentity identity1 = securityRealm.getRealmIdentity(fromName("plainUser"));
        Assert.assertTrue(identity1.exists());
        identity1.dispose();

        RealmsTestCase.testModifiability(securityRealm);
    }

    @Test
    public void testLdapKeyStoreMinimalService() throws Exception {
        testLdapKeyStoreService("LdapKeyStoreMinimal", "firefly");
    }

    @Test
    public void testLdapKeyStoreMaximalService() throws Exception {
        testLdapKeyStoreService("LdapKeyStoreMaximal", "serenity");
    }

    private void testLdapKeyStoreService(String keystoreName, String alias) throws Exception {
        ServiceName serviceName = Capabilities.KEY_STORE_RUNTIME_CAPABILITY.getCapabilityServiceName(keystoreName);
        KeyStore keyStore = (KeyStore) services.getContainer().getService(serviceName).getValue();
        Assert.assertNotNull(keyStore);

        Assert.assertTrue(keyStore.containsAlias(alias));
        Assert.assertTrue(keyStore.isKeyEntry(alias));
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        Assert.assertEquals("OU=Elytron, O=Elytron, C=UK, ST=Elytron, CN=Firefly", cert.getSubjectDN().getName());
        Assert.assertEquals(alias, keyStore.getCertificateAlias(cert));

        Certificate[] chain = keyStore.getCertificateChain(alias);
        Assert.assertEquals("OU=Elytron, O=Elytron, C=UK, ST=Elytron, CN=Firefly", ((X509Certificate) chain[0]).getSubjectDN().getName());
        Assert.assertEquals("O=Root Certificate Authority, EMAILADDRESS=elytron@wildfly.org, C=UK, ST=Elytron, CN=Elytron CA", ((X509Certificate) chain[1]).getSubjectDN().getName());
    }

    @Test
    public void testLdapKeyStoreMinimalCli() throws Exception {
        testLdapKeyStoreCli("LdapKeyStoreMinimal", "firefly");
    }

    @Test
    public void testLdapKeyStoreMaximalCli() throws Exception {
        testLdapKeyStoreCli("LdapKeyStoreMaximal", "serenity");
    }

    private void testLdapKeyStoreCli(String keystoreName, String alias) throws Exception {
        ModelNode operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_CHILDREN_NAMES_OPERATION);
        operation.get(ClientConstants.CHILD_TYPE).set(ElytronDescriptionConstants.ALIAS);
        List<ModelNode> nodes = assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).asList();
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals(alias, nodes.get(0).asString());

        operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        operation.get(ClientConstants.NAME).set(ElytronDescriptionConstants.STATE);
        Assert.assertEquals(ServiceController.State.UP.toString(), assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).asString());

        operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        operation.get(ClientConstants.NAME).set(ElytronDescriptionConstants.SIZE);
        Assert.assertEquals(1, assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).asInt());

        operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName).add("alias", alias);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        operation.get(ClientConstants.NAME).set(ElytronDescriptionConstants.CREATION_DATE);
        Assert.assertNotNull(assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).asString());

        operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName).add("alias", alias);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        operation.get(ClientConstants.NAME).set(ElytronDescriptionConstants.ENTRY_TYPE);
        Assert.assertEquals(KeyStore.PrivateKeyEntry.class.getSimpleName(), assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).asString());

        operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName).add("alias", alias);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        operation.get(ClientConstants.NAME).set(ElytronDescriptionConstants.CERTIFICATE);
        Assert.assertFalse(assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).isDefined()); // chain defined, certificate should be blank

        operation = new ModelNode();
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", keystoreName).add("alias", alias);
        operation.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        operation.get(ClientConstants.NAME).set(ElytronDescriptionConstants.CERTIFICATE_CHAIN);
        ModelNode result = assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT);
        Assert.assertEquals("OU=Elytron,O=Elytron,C=UK,ST=Elytron,CN=Firefly", result.asList().get(0).get(ElytronDescriptionConstants.SUBJECT).asString());
        Assert.assertEquals("O=Root Certificate Authority,1.2.840.113549.1.9.1=#1613656c7974726f6e4077696c64666c792e6f7267,C=UK,ST=Elytron,CN=Elytron CA", result.asList().get(1).get(ElytronDescriptionConstants.SUBJECT).asString());
    }

    @Test
    public void testLdapKeyStoreCopyRemoveAlias() throws Exception {
        ServiceName serviceName = Capabilities.KEY_STORE_RUNTIME_CAPABILITY.getCapabilityServiceName("LdapKeyStoreMaximal");
        LdapKeyStoreService ldapKeyStoreService = (LdapKeyStoreService) services.getContainer().getService(serviceName).getService();
        KeyStore keyStore = ldapKeyStoreService.getModifiableValue();
        Assert.assertNotNull(keyStore);

        Key key = keyStore.getKey("serenity", "Elytron".toCharArray());
        Certificate[] chain = keyStore.getCertificateChain("serenity");
        Assert.assertNotNull(key);
        Assert.assertNotNull(chain);
        Assert.assertEquals(1, keyStore.size());

        // create two copies
        keyStore.setKeyEntry("serenity1", key, "password1".toCharArray(), chain);
        keyStore.setKeyEntry("serenity2", key, "password2".toCharArray(), chain);
        Assert.assertNotNull(keyStore.getKey("serenity1", "password1".toCharArray()));
        Assert.assertNotNull(keyStore.getKey("serenity2", "password2".toCharArray()));
        Assert.assertEquals(3, keyStore.size());
        Assert.assertEquals(3, Collections.list(keyStore.aliases()).size());

        ModelNode operation = new ModelNode(); // check count of copies through subsystem
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", "LdapKeyStoreMaximal");
        operation.get(ClientConstants.OP).set(ClientConstants.READ_CHILDREN_NAMES_OPERATION);
        operation.get(ClientConstants.CHILD_TYPE).set(ElytronDescriptionConstants.ALIAS);
        Assert.assertEquals(3, assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT).asList().size());

        keyStore.deleteEntry("serenity1"); // remove through keystore operation
        Assert.assertNull(keyStore.getKey("serenity1", "password1".toCharArray()));
        Assert.assertEquals(2, keyStore.size());

        operation = new ModelNode(); // remove through subsystem operation
        operation.get(ClientConstants.OP_ADDR).add("subsystem", "elytron").add("ldap-key-store", "LdapKeyStoreMaximal").add("alias", "serenity2");
        operation.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
        assertSuccess(services.executeOperation(operation)).get(ClientConstants.RESULT);
        Assert.assertNull(keyStore.getKey("serenity2", "password2".toCharArray()));
        Assert.assertEquals(1, keyStore.size());
    }

    @Test
    public void testKerberosSecurityFactory() throws Exception {
        ServiceName serviceName = Capabilities.SECURITY_FACTORY_CREDENTIAL_RUNTIME_CAPABILITY.getCapabilityServiceName("KerberosFactory");
        SecurityFactory<GSSCredentialCredential> factory = (SecurityFactory) services.getContainer().getService(serviceName).getValue();
        Assert.assertNotNull(factory);

        GSSCredentialCredential gcc = factory.create();
        GSSCredential gc = gcc.getGssCredential();
        Assert.assertNotNull(gc);
        //Config config = Config.getInstance();
    }

    @Test
    public void testSpnegoSecurityFactory() throws Exception {
        ServiceName serviceName = Capabilities.HTTP_AUTHENTICATION_FACTORY_RUNTIME_CAPABILITY.getCapabilityServiceName("SpnegoHttpAuthFactory");
        HttpAuthenticationFactory factory = (HttpAuthenticationFactory) services.getContainer().getService(serviceName).getValue();
        HttpServerAuthenticationMechanism mech = factory.createMechanism("SPNEGO");

        Assert.assertNotNull(factory);
        Assert.assertNotNull(mech);

        mech.evaluateRequest(null);
    }

    private ModelNode assertSuccess(ModelNode response) {
        if (!response.get(OUTCOME).asString().equals(SUCCESS)) {
            Assert.fail(response.toJSONString(false));
        }
        return response;
    }
}
