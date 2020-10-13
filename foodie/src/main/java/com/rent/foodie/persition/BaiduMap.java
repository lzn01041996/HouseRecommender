package com.rent.foodie.persition;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/19

*/
public class BaiduMap {
    static String AK = "GgWIqg7xAR9Umd3Uenr2fEMhAaFWTAnN";

    public static void main(String[] args) throws Exception {
       File infile = new File("D:\\IntelliJ IDEA-workspace\\HouseRent\\Rent-spiderData\\src\\main\\log\\logs.log");
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String inString = "";
        String path = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\newhid.csv";
        // 声明 InputStream 输入字节流
        StringBuffer buffer = new StringBuffer();
        while ((inString = reader.readLine()) != null){
            String[] split = inString.split("\\^");
            String url = "江苏省南京市"+split[2].split(" ")[0];
            Map<String, String> lngAndLat = BaiduMap.getLngAndLat(url);
            buffer.append(split[1].split("top=")[1] + "," + url + ","+lngAndLat.get("lng") + "," + lngAndLat.get("lat") + "\n");
        }

        //构建FileOutputStream对象,文件不存在会自动新建
        FileOutputStream fop = new FileOutputStream(path);
        // 构建OutputStreamWriter对象,参数可以指定编码"UTF-8";不设置，默认为操作系统默认编码;
        OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
        //写入缓冲区
        writer.append(buffer);
        // 关闭写入流,同时会把缓冲区内容写入文件
        writer.close();
        //关闭输出流，释放系统资源
        fop.close();
        reader.close();



        Map<String, String> lngAndLat = BaiduMap.getLngAndLat("江苏省南京市阳光广场");
        System.out.println(lngAndLat);
    }
    public static Map<String,String> getLngAndLat(String address)throws Exception{
        Map<String,String> map=new HashMap<String, String>();
        String url = "http://api.map.baidu.com/geocoder?address="+address+"&output=json&ak=GgWIqg7xAR9Umd3Uenr2fEMhAaFWTAnN";
        String json = loadJSON(url);
        JSONObject obj = JSONObject.fromObject(json);
        if(obj.get("status").toString().equals("OK")){

            Object typeObject = new JSONTokener(obj.get("result").toString()).nextValue();
        if(typeObject instanceof JSONArray) {
            map.put("error","请输入正确地址");
            return map;
        }
        //2.381886
        if(typeObject instanceof JSONObject) {
            String lng=obj.getJSONObject("result").getJSONObject("location").getString("lng");
            String lat=obj.getJSONObject("result").getJSONObject("location").getString("lat");
            map.put("lng", Double.parseDouble(lng)+"");
            map.put("lat", Double.parseDouble(lat)+"");
            return map;
        }
        }else{
            map.put("error","请输入正确地址");
        }
        return map;
    }
    public static String loadJSON (String url)throws Exception {
        StringBuilder json = new StringBuilder();
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}

