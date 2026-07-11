package com.resideo.dashboard.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {
        "/dashboard",
        "/dashboard/**",
        "/executions/**",
        "/live",
        "/reports",
        "/analytics",
        "/flaky-tests",
        "/test-explorer",
        "/devices",
        "/thermostats",
        "/mqtt",
        "/serial",
        "/environments",
        "/configurations",
        "/schedules",
        "/users",
        "/integrations",
        "/settings",
        "/login",
    })
    public String forward() {
        return "forward:/index.html";
    }
}
