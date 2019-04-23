package com.fgnb.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.minitouch.Minitouch;
import com.fgnb.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 * minitouch
 */
@Slf4j
//虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean
@Component
@ServerEndpoint(value = "/minitouch/{deviceId}")
public class MinitouchSocketServer {

	public static Map<String,Session> minitouchSessionMap = new ConcurrentHashMap<>();

	private String deviceId;
	private Session session;

	private Minitouch minitouch;

	@OnOpen
	public void onOpen(@PathParam("deviceId") String deviceId, Session session) throws Exception {

		this.session = session;
		this.deviceId = deviceId;

        log.info("[{}]已连接MinitouchSocketServer，sessionid => {}",deviceId,session.getId());

		WebSocketUtil.sendText(session,"minitouch websocket连接成功");

		//检测手机是否连接
		AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
		if(androidDevice == null || !androidDevice.isConnected()){
			log.info("[{}]设备未连接",deviceId);
			WebSocketUtil.sendText(session,deviceId+"手机未连接");
			return;
		}
		//检测手机是否闲置
		if(androidDevice.getDevice().getStatus() != Device.IDLE_STATUS){
			log.info("[{}]设备未处于闲置状态",deviceId);
			WebSocketUtil.sendText(session,deviceId+"设备未处于闲置状态");
			return;
		}

		//如果有其他连接在使用该手机，则不往下处理并提示用户该手机在被占用
        Session otherSession = minitouchSessionMap.get(deviceId);
        if(otherSession!=null && otherSession.isOpen()){
            log.info("[{}]检查到有其他连接正在使用minitouch服务,不做任何处理，当前sessionid => {}，正在使用minitouch的连接sessionid => {}",deviceId,session.getId(),otherSession.getId());
            WebSocketUtil.sendText(session,deviceId+"手机正在被"+otherSession.getId()+"连接占用,请稍后重试");
            return;
        }

		//将设备id和session放入map
		minitouchSessionMap.put(deviceId,session);

		WebSocketUtil.sendText(session,"开始启动手机minitouch服务");
		//1.启动手机minitouch服务
		minitouch = new Minitouch(androidDevice);
		minitouch.start();
		WebSocketUtil.sendText(session,"启动手机minitouch服务成功");

	}


	/**
	 * minitouc websocket关闭
	 *
	 * 由于用户直接关闭控制手机的网页时，minicap websocket有时关闭很慢，而minitouch每次都很快，所以在minitouch socket断开后，把所有需要回收的资源都放在此处做处理
	 *
	 * 设备拔线离线需要回收资源，也是通过关闭minitouch websocket连接来回收stf资源
	 */
	@OnClose
	public void onClose() {
        log.info("[{}]断开MinitouchSocketServer连接，sessionid => {}",deviceId,session.getId());
		//由于可能有多个连接访问进来，只允许一个连接使用，意味着只有minitouch被成功初始化的连接才是占用的连接，占用连接断开才做资源回收操作
        if(minitouch!=null){
			//断开连接后 移除map里的session
            minitouchSessionMap.remove(deviceId);
            //释放minitouch资源
			minitouch.stop();
		}

	}


	@OnMessage
	public void onMessage(String message) {
		handleMessage(message);
	}

	@OnError
	public void onError(Throwable t){
		log.error("[{}]minitouch onError，sessionid => {}",deviceId,session.getId(),t);
	}


	private void handleMessage(String msg){
		JSONObject jsonObject = JSON.parseObject(msg);
		String operation = (String) jsonObject.get("operation");
		//将前端传递过来的尺寸比例乘以minitou输出的屏幕尺寸 得到坐标
		switch (operation){
			case "m":
				minitouch.moveTo(objToFloat(jsonObject.get("pX")), objToFloat(jsonObject.get("pY")));
				break;
			case "d":
				minitouch.touchDown(objToFloat(jsonObject.get("pX")), objToFloat(jsonObject.get("pY")));
				break;
			case "u":
				minitouch.touchUp();
				break;
//			case "g":
//				minitouch.inputKeyevent(3);
//				break;
//			case "b":
//				minitouch.inputKeyevent(4);
//				break;
//			case "p":
//				minitouch.inputKeyevent(26);
//				break;
//			case "menu":
//				minitouch.inputKeyevent(82);
//				break;
		}
	}

	private float objToFloat(Object o){
		if(o instanceof BigDecimal){
			return  ((BigDecimal)o).floatValue() ;
		}else {
			return  ((Integer)o).floatValue() ;
		}
	}
}