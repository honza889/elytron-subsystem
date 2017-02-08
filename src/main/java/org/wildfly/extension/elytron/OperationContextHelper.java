package org.wildfly.extension.elytron;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
// TODO (jrp) do we really need this?
class OperationContextHelper {

    static boolean requiresRuntime(final OperationContext context) {
        return context.getProcessType().isServer() || !ModelDescriptionConstants.PROFILE.equals(context.getCurrentAddress().getElement(0).getKey());
    }
}
