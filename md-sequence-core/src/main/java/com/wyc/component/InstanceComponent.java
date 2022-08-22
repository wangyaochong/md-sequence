package com.wyc.component;

import com.wyc.util.UtilNetwork;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Data
public class InstanceComponent {
    @Getter
    static String ip;
    @Getter
    static Integer port;

    public InstanceComponent(@Value("${server.port}") Integer port, @Autowired Environment environment) {
        InstanceComponent.port = port;
        String property = environment.getProperty("server.ip");
        if (property == null) {
            ip = UtilNetwork.getLocalIp();
        } else {
            ip = property;
        }
    }

}
