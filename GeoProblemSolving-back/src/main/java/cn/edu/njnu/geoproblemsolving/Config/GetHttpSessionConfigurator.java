package cn.edu.njnu.geoproblemsolving.Config;


import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

@Configuration
public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response)
    {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        if (httpSession!=null){
            // System.out.println("-----2----GetHttpSessionConfigurator--" + httpSession.getId());
            // System.out.println("获得到userId："+httpSession.getAttribute("userId"));
            config.getUserProperties().put(HttpSession.class.getName(),httpSession);
        }
    }
}