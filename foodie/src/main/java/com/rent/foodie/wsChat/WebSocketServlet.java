package com.rent.foodie.wsChat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/12/15

*/
@Component
@ServerEndpoint("/wsocket")
public class WebSocketServlet {
    //静态变量，用来记录当前在线连接数，应该把他涉及成线程安全的
    private static int onlineCount = 0;
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    public static Logger log = Logger.getLogger("logging");
    FAQRobot robot;
    {
        try {
            robot = new FAQRobot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //cocurrent包的线程安全set,用来存放每个客户端对应的websocket对象，若要实现服务端与单一客户端通信的话，
    //可以使用map来存放
    private static CopyOnWriteArraySet<WebSocketServlet> webSockets = new CopyOnWriteArraySet<>();
    private static ApplicationContext applicationContext;
    public static void setApplicationContext(ApplicationContext applicationContext){
        WebSocketServlet.applicationContext = applicationContext;
    }

    //连接建立成功调用的方法
    @OnOpen
    public void open(Session session){
        this.session = session;
        //加入到set当中
        webSockets.add(this);
        addOnlineCount();
        log.info("有新连接加入！当前在线人数为：" + getOnlineCount());
    }

    //连接关闭调用的方法
    @OnClose
    public void onClose(){
        webSockets.remove(this);
        subOnlineCount();
        log.info("有一连接关闭！当前在线人数:" + getOnlineCount() );
    }

    //接受数据
    @OnMessage
    public void OnMessage(String message,Session session){
        log.info("来自客户端的消息：" + message);
        //群发消息
        JSONObject jsonObject = JSONObject.parseObject(message);
        String content = jsonObject.getString("data");
        JSONObject jsonObject1 = JSONObject.parseObject(content);
        String mine = jsonObject1.getString("mine");
        JSONObject jsonObject2 = JSON.parseObject(mine);
        String content1 = jsonObject2.getString("content");
        for (WebSocketServlet webSocketServlet : webSockets) {
            try {
                String simple_pos = robot.answer(content1, "simple_pos");
                webSocketServlet.sendMessage(simple_pos);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }


    //发生错误时调用，错误方法调用玩，会再自动调用@OnClose方法
    @OnError
    public void onError(Throwable error){
        log.info("发生错误！");
        error.printStackTrace();
    }


    //自己需要添加的方法
    public void sendMessage(String message) throws IOException{
        Map<String,Object> map = new HashMap<>();
        map.put("emit","chatMessage");
        map.put("data",message);
        JSONObject json = new JSONObject(map);
        log.info(json.toString());
        this.session.getBasicRemote().sendText(json.toString());

    }

    public static synchronized int getOnlineCount(){
        return onlineCount;
    }

    public static synchronized  void addOnlineCount(){
        WebSocketServlet.onlineCount++;
    }

    public static synchronized void subOnlineCount(){
        WebSocketServlet.onlineCount--;
    }

}
