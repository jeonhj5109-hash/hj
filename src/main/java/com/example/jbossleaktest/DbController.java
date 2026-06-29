package com.example.jbossleaktest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

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
        // JBoss 트랜잭션 밖에서 커넥션 가져오기
        javax.sql.DataSource unwrapped = jdbcTemplate.getDataSource();
        java.sql.Connection conn = unwrapped.getConnection();
        conn.setAutoCommit(false);
        // close() 없음 → 진짜 누수
        Thread.sleep(300000); // 5분 대기 (커넥션 점유)
        return "누수 발생";
    }

    @GetMapping("/db/lock")
    public String lock() throws Exception {
        jdbcTemplate.update("UPDATE lock_test SET val='was_locked' WHERE id=1");
        return "락 획득 성공";
    }

    // 메모리 압박 (GC 유발)
    @GetMapping("/db/gc")
    public String gc() throws Exception {
        List<byte[]> list = new ArrayList<>();
        try {
            while (true) {
                list.add(new byte[1024 * 1024]); // 1MB씩 계속 할당
                Thread.sleep(100);
            }
        } catch (OutOfMemoryError e) {
            return "OOM 발생: " + e.getMessage();
        }
    }

}