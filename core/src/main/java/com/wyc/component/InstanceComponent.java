package com.wyc.component;

import com.wyc.util.UtilNetwork;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class InstanceComponent {
    @Getter
    static String ip = UtilNetwork.getLocalIp();
    @Getter
    static Integer port;

    public InstanceComponent(@Value("${server.port}") Integer port) {
        InstanceComponent.port = port;
    }

}
