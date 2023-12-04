
package com.navercorp.pinpoint.plugin.sample;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author SJ
 *
 */
public class SampleTypeProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(SamplePluginConstants.DB2, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        context.addServiceType(SamplePluginConstants.DB2_EXECUTE_QUERY, AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
        AnnotationKeyMatchers.exact(AnnotationKey.ARGS0));
    }

}
