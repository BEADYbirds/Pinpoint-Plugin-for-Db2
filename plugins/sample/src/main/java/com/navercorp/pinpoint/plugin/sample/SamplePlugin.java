/**
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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.plugin.sample._01_Injecting_BasicMethodInterceptor.Sample_01_Inject_BasicMethodInterceptor;
import com.navercorp.pinpoint.plugin.sample._02_Injecting_Custom_Interceptor.Sample_02_Inject_Custom_Interceptor;
import com.navercorp.pinpoint.plugin.sample._03_Interceptor_Scope__Prevent_Duplicated_Trace.Sample_03_Use_Interceptor_Scope_To_Prevent_Duplicated_Trace;
import com.navercorp.pinpoint.plugin.sample._04_Interceptor_Scope__Data_Sharing.Sample_04_Interceptors_In_A_Scope_Share_Value;
import com.navercorp.pinpoint.plugin.sample._05_Constructor_Interceptor.Sample_05_Constructor_Interceptor;
import com.navercorp.pinpoint.plugin.sample._06_Constructor_Interceptor_Scope_Limitation.Sample_06_Constructor_Interceptor_Scope_Limitation;
import com.navercorp.pinpoint.plugin.sample._07_MethodFIlter.Sample_07_Use_MethodFilter_To_Intercept_Multiple_Methods;
import com.navercorp.pinpoint.plugin.sample._08_Interceptor_Annotations.Sample_08_Interceptor_Annotations;
import com.navercorp.pinpoint.plugin.sample._09_Adding_Getter.Sample_09_Adding_Getter;
import com.navercorp.pinpoint.plugin.sample._10_Adding_Field.Sample_10_Adding_Field;
import com.navercorp.pinpoint.plugin.sample._11_Configuration_And_ObjectRecipe.Sample_11_Configuration_And_ObjectRecipe;
import com.navercorp.pinpoint.plugin.sample._12_Asynchronous_Trace.Sample_12_Asynchronous_Trace;
import com.navercorp.pinpoint.plugin.sample._13_RPC_Client.Sample_13_RPC_Client;
import com.navercorp.pinpoint.plugin.sample._14_RPC_Server.Sample_14_RPC_Server;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class SamplePlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String DB2_SCOPE = SamplePluginConstants.DB2_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final JdbcUrlParserV2 jdbcUrlParser = new SampleJdbcUrlParser();

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        SampleConfig config = new SampleConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer();
        addDriverTransformer();
        addPreparedStatementTransformer();
        addCallableStatementTransformer();
        addStatementTransformer();

    }

    private void addConnectionTransformer() {
        transformTemplate.transform("com.ibm.db2.jcc4.DB2Connection",
                DB2ConnectionTransform.class);
    }

    public static class DB2ConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            SampleConfig config = new SampleConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, DB2_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement")
                    .addScopedInterceptor(statementCreate, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, DB2_SCOPE);

            // preparedStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int[]")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils
                    .findMethod(target, "prepareStatement", "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate,DB2_SCOPE);
            InstrumentUtils
                    .findMethod(target, "prepareStatement", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);

            // preparecall
            InstrumentUtils.findMethod(target, "prepareCall", "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "prepareCall", "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils
                    .findMethod(target, "prepareCall", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);

            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethod(target, "setAutoCommit", "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, DB2_SCOPE);
            }

            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, DB2_SCOPE);
            }

            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, DB2_SCOPE);
            }

            return target.toBytecode();
        }
    }

    ;

    private void addDriverTransformer() {
        transformTemplate
                .transform("com.ibm.db2.jcc4.DB2Driver", DriverTransformer.class);
    }

    public static class DriverTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);

            InstrumentUtils
                    .findMethod(target, "connect", "java.lang.String", "java.util.Properties")
                    .addScopedInterceptor(DriverConnectInterceptorV2.class,
                            va(SamplePuginConstants.DB2_JDBC, true), DB2_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addPreparedStatementTransformer() {
        transformTemplate.transform("com.ibm.db2.jcc4.DB2PreparedStatement",
                PreparedStatementTransform.class);
    }

    public static class PreparedStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {

            SampleConfig config = new SampleConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "execute")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), DB2_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), DB2_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), DB2_SCOPE);

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter
                        .excludes("setRowId", "setNClob", "setSQLXML");

                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, DB2_SCOPE,
                            ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }

    ;

    private void addStatementTransformer() {
        transformTemplate.transform("com.ibm.db2.jcc4.DB2ServerStatement",
                DB2StatementTransform.class);
    }

    private void addCallableStatementTransformer() {
        transformTemplate.transform("com.ibm.db2.jcc4.DB2CallableStatement",
               DB2CallableStatementTransform.class);
    }

    public static class DB2StatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, DB2_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);

            return target.toBytecode();
        }
    }

    public static class DB2CallableStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);


            final Class<? extends Interceptor> callableStatementInterceptor = CallableStatementRegisterOutParameterInterceptor.class;
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                    .addScopedInterceptor(callableStatementInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                    .addScopedInterceptor(callableStatementInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                    .addScopedInterceptor(callableStatementInterceptor, DB2_SCOPE);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
