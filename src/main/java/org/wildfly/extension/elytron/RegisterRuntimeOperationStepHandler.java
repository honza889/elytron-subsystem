package org.wildfly.extension.elytron;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.wildfly.common.Assert;

/**
 * An implementation of an {@link OperationStepHandler} that simply registers a
 * {@linkplain OperationContext.Stage#RUNTIME runtime} step if the {@link OperationContext#isDefaultRequiresRuntime()}
 * returns {@code true}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class RegisterRuntimeOperationStepHandler implements OperationStepHandler {

    /**
     * {@inheritDoc}
     * <p>
     * If the {@link OperationContext#isDefaultRequiresRuntime()} returns {@code true} the
     * {@link #getRuntimeStep() runtime step} will be registered on the context.
     * </p>
     */
    @Override
    public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
        if (OperationContextHelper.requiresRuntime(context)) {
            context.addStep(Assert.assertNotNull(getRuntimeStep()), OperationContext.Stage.RUNTIME);
        }
    }

    /**
     * Returns the runtime step to register if the {@link OperationContext#isDefaultRequiresRuntime()} returns
     * {@code true} for the {@linkplain OperationStepHandler#execute(OperationContext, ModelNode) execution}.
     *
     * @return the runtime step to register
     */
    abstract OperationStepHandler getRuntimeStep();
}
