package com.studio.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Angular frontend uses real browser paths (/dashboard, /mock/start, ...), so a hard refresh
 * or a direct link hits the server, not just client-side JS. Forward those to index.html so
 * Angular's router can take over. Excludes /api/** (handled elsewhere) and anything with a dot
 * (static assets like main.js, styles.css, favicon.ico), which are served directly.
 */
@Controller
public class SpaForwardController {

    @RequestMapping({"/{path:^(?!api)[^.]*$}", "/{path:^(?!api)[^.]*$}/{sub:[^.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
