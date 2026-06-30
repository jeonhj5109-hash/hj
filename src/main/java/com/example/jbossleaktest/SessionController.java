package com.example.jbossleaktest;

import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SessionController {

    @PostMapping("/api/login")
    public Map<String, Object> login(@RequestParam String userId, HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userId + " 사원");
        session.setAttribute("loginTime", System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("node", nodeInfo());
        result.put("sessionId", session.getId());
        return result;
    }

    @GetMapping("/api/session-info")
    public Map<String, Object> sessionInfo(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();
        result.put("node", nodeInfo());

        if (session == null || session.getAttribute("userId") == null) {
            result.put("loggedIn", false);
            result.put("message", "세션 없음 - 복제 실패 또는 미로그인");
            return result;
        }
        result.put("loggedIn", true);
        result.put("sessionId", session.getId());
        result.put("userId", session.getAttribute("userId"));
        result.put("userName", session.getAttribute("userName"));
        result.put("loginTime", session.getAttribute("loginTime"));
        return result;
    }

    @PostMapping("/api/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("node", nodeInfo());
        return result;
    }

    private String nodeInfo() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostName() + " (" + addr.getHostAddress() + ")";
    }
}