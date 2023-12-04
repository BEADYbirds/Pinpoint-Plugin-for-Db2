package com.navercorp.pinpoint.plugin.jdbc.mssql;

import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DB2Container; // DB2 컨테이너 클래스 임포트


import java.util.Properties;

public class Db2Server implements SharedTestLifeCycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DB2Container db2Container;

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        db2Container = new DB2Container(); // DB2 컨테이너 인스턴스 생성
        db2Container.start(); // DB2 컨테이너 시작

        return DatabaseContainers.toProperties(db2Container); // 데이터베이스 연결 정보를 Properties 객체로 변환
    }

    @Override
    public void afterAll() {
        if (db2Container != null) {
            db2Container.stop(); // DB2 컨테이너 정지
        }
    }
}