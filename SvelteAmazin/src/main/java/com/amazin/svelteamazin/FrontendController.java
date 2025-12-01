package com.amazin.svelteamazin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
    @GetMapping(value = {"/{p:[^\\.]*}", "/{p:^(?!api).*}/{q:[^\\.]*}", "/{a:^(?!api).*}/{b:^(?!api).*}/{c:[^\\.]*}"})
    public String index() {
        return "forward:/index.html";
    }
}
