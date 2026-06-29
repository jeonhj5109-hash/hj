package com.example.jbossleaktest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 정상 조회 (커넥션 정상 반납)
    @GetMapping("/db/normal")
    public String normal() {
        String result = jdbcTemplate.queryForObject(
                "SELECT 'OK' FROM DUAL", String.class);
        return "정상 조회: " + result;
    }

    // 느린 쿼리 (스레드 점유)
    @GetMapping("/db/slow")
    public String slow() throws Exception {
        jdbcTemplate.execute("BEGIN DBMS_LOCK.SLEEP(30); END;");
        return "느린 쿼리 완료";
    }

    // 커넥션 누수 (반납 안 함)
    @Autowired
    private javax.sql.DataSource dataSource;

    @GetMapping("/db/leak")
    public String leak() throws Exception {
        java.sql.Connection conn = dataSource.getConnection();
        // close() 없음 → 누수 발생
        return "커넥션 누수 발생";
    }

    @GetMapping("/db/lock")
    public String lock() throws Exception {
        jdbcTemplate.update("UPDATE lock_test SET val='was_locked' WHERE id=1");
        return "락 획득 성공";
    }
}