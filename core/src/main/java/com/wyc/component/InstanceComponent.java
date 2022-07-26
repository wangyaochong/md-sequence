package com.wyc.component;

import com.wyc.util.UtilNetwork;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InstanceComponent {
    String ip = UtilNetwork.getLocalIp();
    @Value("${server.port}") Integer port;
}
