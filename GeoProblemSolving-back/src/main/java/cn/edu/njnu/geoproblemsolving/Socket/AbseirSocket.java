package cn.edu.njnu.geoproblemsolving.Socket;

import cn.edu.njnu.geoproblemsolving.Config.GetHttpSessionConfigurator;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/Abseir/{groupID}", configurator = GetHttpSessionConfigurator.class)
public class AbseirSocket {

    private Session session = null;
    private static final Map<String, Set<AbseirSocket>> groups = new ConcurrentHashMap<>();
    private static JSONObject members = new JSONObject();       // 在线成员

    @OnOpen
    public void onOpen(@PathParam("groupID") String groupID, Session session, EndpointConfig config) {
        this.session = session;
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        System.out.println(httpSession.getAttribute("userId").toString());

        if (!groups.containsKey(groupID)) {
            Set<AbseirSocket> group = new HashSet<>();
            group.add(this);
            groups.put(groupID, group);
        } else {
            groups.get(groupID).add(this);
        }

        JSONObject userJson = new JSONObject();
        userJson.put("userId", httpSession.getAttribute("userId").toString());
        userJson.put("userName", httpSession.getAttribute("name").toString());
        members.put(this.session.getId(), userJson);
        publicMembers(groupID, "members");//公布新的在线数组和演示者

    }

    @OnMessage
    public void onMessage(@PathParam("groupID") String groupID, String message) {
        JSONObject messageObject = JSONObject.parseObject(message);
        String messageType = messageObject.getString("type");
        switch (messageType) {
            case "ping":
                try {
//                    //向客户端发送消息(给自己
//                    for (AbseirSocket server : groups.get(groupID)) {
//                        if (this.equals(server)) {
//                            server.session.getBasicRemote().sendText(message);
//                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "operation":
                try {
                    //向客户端发送消息(给别人
                    for (AbseirSocket server : groups.get(groupID)) {
                        if (!this.equals(server)) {
                            server.session.getBasicRemote().sendText(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    @OnClose
    public void onClose(@PathParam("groupID") String groupID) {
        groups.get(groupID).remove(this);
        System.out.println("Socket disconnect, sessionID: " + this.session.getId() + " UserName: " + members.getJSONObject(this.session.getId()).getString("useName"));
        String nowUser = members.getJSONObject(this.session.getId()).getString("userId");

        if (groups.get(groupID).size() >= 1) {

            members.remove(this.session.getId());
        } else {
            groups.remove(groupID);
        }
    }

    @OnError
    public void onError(@PathParam("groupID") Throwable error) {

        System.out.println("发生错误");
        error.printStackTrace();
    }

    //发送在线用户清单
    private void publicMembers(@PathParam("groupID") String groupID, String messageType) {

        JSONArray memberList = new JSONArray();
        JSONObject messageMember = new JSONObject();

        for (AbseirSocket server : groups.get(groupID)) {
            JSONObject user = new JSONObject();
            user.put("Id", members.getJSONObject(server.session.getId()).getString("userId"));
            user.put("Name", members.getJSONObject(server.session.getId()).getString("userName"));
            memberList.add(user);
        }
        messageMember.put("type", messageType);
        messageMember.put("userList", memberList.toString());

        try {
            //向客户端发送消息
            for (AbseirSocket server : groups.get(groupID)) {
                server.session.getBasicRemote().sendText(messageMember.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

