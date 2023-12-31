/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.sample;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author dawidmalina
 *
 */
public final class SamplePluginConstants {
    private SamplePluginConstants() {
    }

    public static final String DB2_SCOPE = "DB2_JDBC";

    public static final ServiceType DB2 = ServiceTypeFactory.of(2900, "DB2", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType DB2_EXECUTE_QUERY = ServiceTypeFactory.of(2901, "DB2_EXECUTE_QUERY",
            "DB2", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}
