package core;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {

    @GetMapping("/helloProvider")
    public String helloProvider(){
        return "你好,我是服务提供者";
    }
}