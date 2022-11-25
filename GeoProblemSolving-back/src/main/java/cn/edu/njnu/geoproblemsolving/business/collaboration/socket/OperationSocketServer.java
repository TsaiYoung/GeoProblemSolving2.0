package cn.edu.njnu.geoproblemsolving.business.collaboration.socket;

import cn.edu.njnu.geoproblemsolving.Config.GetHttpSessionConfigurator;
import cn.edu.njnu.geoproblemsolving.business.collaboration.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author ：mzy
 * @Date ：2021/4/25
 * @version: 1.0.0
 */
@Component
@ServerEndpoint(value = "/OperationServer/{toolId}/{aid}", configurator = GetHttpSessionConfigurator.class)
public class OperationSocketServer {

    private static CollaborationService collaborationService;

    @Autowired
    public void setCollaborationService(CollaborationService collaborationService) {
        OperationSocketServer.collaborationService = collaborationService;
    }


    @OnOpen
    public void onOpen(@PathParam("toolId") String toolId, @PathParam("aid") String aid, Session session, EndpointConfig config) {
        String groupKey = toolId + aid;
        collaborationService.operationStart(groupKey, session, config);


        HttpSession httpSession = ((HttpSession) config.getUserProperties().get(HttpSession.class.getName()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = dateFormat.format(new Date());
        System.out.println("Operation 已连接：" + "用户名-" + httpSession.getAttribute("name") + "-----" + nowDate);

    }

    /**
     * operation content 1: type消息类型（members, test, collaboration-init, mode, control-apply, control-stop, operation, computation, resource）
     * operation content 2: sender发送者, receivers接收者, behavior行为, object操作对象, participants在线参与者, waiting等待人数, operator当前操作者, mode协同模式
     *
     * @param toolId
     * @param aid
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(@PathParam("toolId") String toolId, @PathParam("aid") String aid, String message) {
        collaborationService.operationTransfer(toolId, aid, message);
    }

    @OnClose
    public void onClose(@PathParam("toolId") String toolId, @PathParam("aid") String aid, Session session) {
        String groupKey = toolId + aid;
        collaborationService.operationClose(groupKey, session);
        System.out.println("------------Operation OnClose-----:");
    }

    @OnError
    public void onError(Throwable error) {
        System.out.println("operation socket error: " + error.toString());
        error.printStackTrace();
    }

}
