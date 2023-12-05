-- member 테이블 생성
CREATE TABLE member (
                        id INT GENERATED ALWAYS AS IDENTITY,
                        name CHAR(1), -- 또는 VARCHAR 사용
                        joined TIMESTAMP
);

-- test 테이블 생성
CREATE TABLE test (
                      id INT GENERATED ALWAYS AS IDENTITY,
                      name VARCHAR(45),
                      age INT
);

-- concatCharacters 프로시저 생성
CREATE PROCEDURE concatCharacters (
    IN a CHAR(1),
    IN b CHAR(1),
    OUT c CHAR(2)
)
BEGIN
    SET c = a || b;
END;

-- swapAndGetSum 프로시저 생성
CREATE PROCEDURE swapAndGetSum (
    INOUT a INT,
    INOUT b INT
)
BEGIN
    DECLARE temp INT;
    SET temp = a;
    SET a = b;
    SET b = temp;
    SELECT b + a;
END;
