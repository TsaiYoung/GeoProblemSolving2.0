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
@ServerEndpoint(value = "/Mindmap/{groupID}", configurator = GetHttpSessionConfigurator.class)
public class MindmapSocket {

    private Session session = null;
    private static final Map<String, Set<MindmapSocket>> groups = new ConcurrentHashMap<>();
    private static JSONObject members = new JSONObject();       // 在线成员
    private static JSONObject Controller = new JSONObject();    // 正在画图者
    private static final Map<String, JSONArray> requireLists = new ConcurrentHashMap<>(); // 请求队列

    @OnOpen
    public void onOpen(@PathParam("groupID") String groupID, Session session, EndpointConfig config) {
        this.session = session;
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        System.out.println(httpSession.getAttribute("userId").toString());

        if (!groups.containsKey(groupID)) {
            Set<MindmapSocket> group = new HashSet<>();
            group.add(this);
            groups.put(groupID, group);
        } else {
            groups.get(groupID).add(this);
        }

        if (!requireLists.containsKey(groupID)) { //群组若不存在请求队列则新建
            JSONArray list = new JSONArray();
            requireLists.put(groupID, list);
        }

        if (Controller.size() == 0) {
            JSONObject userJson = new JSONObject();
            userJson.put("userId", "7014115d-2054-4c5e-99ed-ed786574cd32");
            userJson.put("userName", "nobody");
            Controller.put(groupID, userJson);//设置权限者为空
        }

    }

    @OnMessage
    public void onMessage(@PathParam("groupID") String groupID, String message) {
        JSONObject messageObject = JSONObject.parseObject(message);
        String messageType = messageObject.getString("messageType");
        switch (messageType) {
            case "Ping":
                try {
//                    //向客户端发送消息(给自己
//                    for (MindmapSocket server : groups.get(groupID)) {
//                        if (this.equals(server)) {
//                            server.session.getBasicRemote().sendText(message);
//                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Message":
                try {
                    //向客户端发送消息(给别人
                    for (MindmapSocket server : groups.get(groupID)) {
                        if (!this.equals(server)) {
                            server.session.getBasicRemote().sendText(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Join":
                String userId = messageObject.getString("userId");
                String userName = messageObject.getString("userName");
                JSONObject userJson = new JSONObject();
                userJson.put("userId", userId);
                userJson.put("userName", userName);
                members.put(this.session.getId(), userJson);

                System.out.println("Socket connect,  UserId: " + userId + ", UserName: " + messageObject.getString("userName"));
                publicMembers(groupID, "Join", requireLists);//公布新的在线数组和演示者

                break;
            case "Authority":
                if (messageObject.getString("value").equals("Require")) {

                    // 先删除原来的排序
                    String nowUser = members.getJSONObject(this.session.getId()).getString("userId");
                    ResetRequireLists(groupID, nowUser);
                    // 重新排序
                    userJson = new JSONObject();
                    userJson.put("userId", messageObject.getString("userId"));
                    userJson.put("userName", messageObject.getString("userName"));
                    requireLists.get(groupID).add(userJson);

                    if (Controller.getJSONObject(groupID).getString("userId").equals("7014115d-2054-4c5e-99ed-ed786574cd32")) {
                        ReGrant(groupID);//重新赋予权限
                    }
                    publicMembers(groupID, "Authority", requireLists);//公布新的请求队列和演示者

                } else if (messageObject.getString("value").equals("Release")) {
                    String nowUser = members.getJSONObject(this.session.getId()).getString("userId");
                    ResetRequireLists(groupID, nowUser);

                    ReGrant(groupID);//重新赋予权限
                    publicMembers(groupID, "Authority", requireLists);//公布新的请求队列和演示者
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

            ResetRequireLists(groupID, nowUser);

            if (Controller.getJSONObject(groupID).getString("userId").equals(nowUser)) { //若退出的用户为演示者则重新分配权限
                ReGrant(groupID);
            }

            members.remove(this.session.getId());
            publicMembers(groupID, "Left", requireLists);//发布在线用户群组及演示者
        } else {
            groups.remove(groupID);
            requireLists.clear();

            JSONObject userJson = new JSONObject();
            userJson.put("userId", "7014115d-2054-4c5e-99ed-ed786574cd32");
            userJson.put("userName", "nobody");
            Controller.put(groupID, userJson);//设置权限者为空
        }
    }

    @OnError
    public void onError(@PathParam("groupID") Throwable error) {

        System.out.println("发生错误");
        error.printStackTrace();
    }

    //发送在线用户清单
    private void publicMembers(@PathParam("groupID") String groupID, String messageType, Map requireLists) {

        JSONArray memberList = new JSONArray();
        JSONObject messageMember = new JSONObject();

        for (MindmapSocket server : groups.get(groupID)) {
            JSONObject user = new JSONObject();
            user.put("id", members.getJSONObject(server.session.getId()).getString("userId"));
            user.put("name", members.getJSONObject(server.session.getId()).getString("userName"));
            memberList.add(user);
        }
        messageMember.put("messageType", messageType);
        messageMember.put("controller", Controller.getJSONObject(groupID).toString());
        messageMember.put("requireList", requireLists.get(groupID).toString());
        messageMember.put("userList", memberList.toString());

        try {
            //向客户端发送消息
            for (MindmapSocket server : groups.get(groupID)) {
                server.session.getBasicRemote().sendText(messageMember.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ReGrant(String groupID) {//重新设置演示者

        if (!requireLists.get(groupID).isEmpty()) {//若请求队列的请求用户不为空

            String firstUser = requireLists.get(groupID).getJSONObject(0).getString("userId");

            boolean exist = false;
            for (MindmapSocket server : groups.get(groupID)) {//从群组的在线人员里找是否有申请者
                if (firstUser.equals(members.getJSONObject(server.session.getId()).getString("userId"))) {
                    exist = true;//用户存在
                    break;
                }
            }

            if (exist) {
                Controller.put(groupID, requireLists.get(groupID).getJSONObject(0));//将权限赋予队首用户
            } else {//用户不存在，删除队首用户重新赋予权限
                requireLists.get(groupID).remove(0);
                ReGrant(groupID);//再来一遍判断请求队列
            }

        } else {//若请求队列的请求用户为空，则权限搁置

            JSONObject userJson = new JSONObject();
            userJson.put("userId", "7014115d-2054-4c5e-99ed-ed786574cd32");
            userJson.put("userName", "nobody");
            Controller.put(groupID, userJson);//设置权限者为空
        }
    }

    private void ResetRequireLists(String groupID, String userId) {

        JSONArray list = requireLists.get(groupID);
        for (int i = 0; i < list.size(); i++) {
            if (list.getJSONObject(i).getString("userId").equals(userId)) {
                list.remove(i);
                break;
            }
        }
    }

}
