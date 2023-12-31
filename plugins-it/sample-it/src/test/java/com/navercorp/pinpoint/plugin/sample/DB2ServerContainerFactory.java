/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.sample;

import com.navercorp.pinpoint.pluginit.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.DB2Container;
import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class DB2ServerContainerFactory {
    private DB2ServerContainerFactory() {
    }

    public static DB2Container newDB2ServerContainer(String loggerName) {
        final DB2Container db2Container = new DB2Container();
        db2Container.withInitScript("sql/init_db2.sql");

        db2Container.withLogConsumer(new Consumer<OutputFrame>() {
            private final Logger logger = LogManager.getLogger(loggerName);
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(LogUtils.removeLineBreak(outputFrame.getUtf8String()));
            }
        });
        return db2Container;
    }
}
