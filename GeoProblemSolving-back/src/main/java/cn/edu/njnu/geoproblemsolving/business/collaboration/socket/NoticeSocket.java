package cn.edu.njnu.geoproblemsolving.business.collaboration.socket;

import cn.edu.njnu.geoproblemsolving.Config.GetHttpSessionConfigurator;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/NoticeSocket", configurator = GetHttpSessionConfigurator.class)
public class NoticeSocket {

    private Session session = null;
    private static final Map<String, NoticeSocket> servers = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = dateFormat.format(new Date());
        System.out.println("Notice已连接：" + "用户名-" + httpSession.getAttribute("name") + "-----" + "连接时间：" + nowDate);
        this.session = session;
        servers.put(httpSession.getAttribute("userId").toString(), this);

    }

    //接收消息后所调用的方法
    @OnMessage
    public void onMessage(String message) {
        // System.out.println("Notice on message");
        try {
            JSONObject messageJson = JSONObject.parseObject(message);
            // System.out.println("-----Notice onmessage: --------:" + messageJson);
            if (!messageJson.getString("type").equals("ping")) {
                if(messageJson.getString("type").equals("test")) {
                    // broadcast online participants
                    broadcastMemberInfo();
                } else {
                    for (Map.Entry<String, NoticeSocket> server : servers.entrySet()) {
                        String serverKey = server.getKey();
                        String recipientId = messageJson.getString("recipientId");
                        if (serverKey.equals(recipientId)) {
                            JSONObject noticeRes = new JSONObject();
                            noticeRes.put("type", messageJson.getString("type"));
                            noticeRes.put("msg", "Notice");
                            servers.get(server.getKey()).session.getBasicRemote().sendText(noticeRes.toString());
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() throws IOException {
        String unConnectId = "";
        Set<String> strings = servers.keySet();
        // System.out.println("Onclose ---------------NoticeServer: " + strings.size());
        for (Map.Entry<String, NoticeSocket> server : servers.entrySet()) {
            // System.out.println(server.getValue().toString());
            // System.out.println(this.toString());
            if (server.getValue().equals(this)) {
                unConnectId = server.getKey();
                servers.remove(server.getKey());
                break;
            }
        }
        // broadcast online participants
        broadcastMemberInfo();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = dateFormat.format(new Date());
        // System.out.println("Notice 有用户断开连接：" + unConnectId + "-----" + "断开时间：" + nowDate);
        System.out.println("Notice 有用户断开连接："  + "-----" + "断开时间：" + nowDate);
        System.out.println("---------------------------------------------------------------------------");
    }

    @OnError
    public void onError(Throwable error) {
        for (Map.Entry<String, NoticeSocket> server : servers.entrySet()) {
            if (server.getValue().equals(this)) {
                servers.remove(server.getKey());
                break;
            }
        }
        System.out.println("--------Notice on error----------------:" + error.toString());
    }

    private void broadcastMemberInfo() throws IOException {
        try {
            ArrayList<String> participants = new ArrayList<>();
            for (Map.Entry<String, NoticeSocket> server : servers.entrySet()) {
                String serverKey = server.getKey();
                participants.add(serverKey);
            }
            JSONObject notice = new JSONObject();
            notice.put("type", "members");
            notice.put("participants", participants);
            for (Map.Entry<String, NoticeSocket> server : servers.entrySet()) {
                servers.get(server.getKey()).session.getBasicRemote().sendText(notice.toString());
            }
        }catch (Exception e){
            System.out.println("------broadcastMemberInfo error ---------");
            System.out.println(e.toString());
        }

    }
}
