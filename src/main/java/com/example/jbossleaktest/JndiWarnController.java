package com.example.jbossleaktest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RestController
public class JndiWarnController {

    @GetMapping("/jndi/warn-test")
    @Transactional
    public String jndiWarnTest() throws Exception {
        // JNDI에서 직접 DataSource를 가져옴 (Spring이 모르는 방식)
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/OracleDS");

        try (Connection conn = ds.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 'OK' FROM DUAL");
            rs.next();
            return "결과: " + rs.getString(1);
        }
    }
}