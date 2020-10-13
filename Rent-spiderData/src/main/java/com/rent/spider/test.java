package com.rent.spider;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/8

*/
public class test {
    public static void main(String[] args) throws IOException {
        String proxyHost = "127.0.0.1";
        String proxyPort = "11356";
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        // 对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);  //设置代理来访问豆瓣

        String link = "https://nanjing.qfang.com/rent/f32";
        Connection connect = Jsoup.connect(link);
        connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        Document doc = connect.get();
        Elements pList = doc.getElementsByClass("main-left fl");

        Elements listCon = pList.get(0).getElementsByClass("items clearfix");

        Element singleHouse = listCon.get(0);
        Elements photoAndUrl = singleHouse.getElementsByClass("photo-wrap fl");
        String imgUrl = photoAndUrl.get(0).getElementsByTag("img").attr("data-original");
        String houseSingleUrl = photoAndUrl.get(0).getElementsByTag("a").attr("href");
        //获取房子的名字
        String title = singleHouse.getElementsByClass("list-main-header clearfix").get(0).child(0).html().replace("&nbsp;"," ");
        System.out.println("房子的名字：" +title);

        Elements infosSingle = singleHouse.getElementsByClass("house-metas clearfix");
        if (infosSingle.get(0).childNodeSize()>=9){
            //获取房型
            String singleType = infosSingle.get(0).child(1).html();
            System.out.println("房型是：" + singleType);
            //获取面积
            String size = infosSingle.get(0).child(3).html();
            System.out.println("房子的面积是：" +size);
            //获取楼层
            String direcType = infosSingle.get(0).child(5).html();
            System.out.println("房子的装潢类型是：" + direcType);
            //获取出租类型
            String singleLayer = infosSingle.get(0).child(7).html();
            System.out.println("房子的楼层是：" + singleLayer);
            String rentType = infosSingle.get(0).child(9).child(0).html();
            System.out.println("房子的出租方式是：" + rentType);
        }

        Elements places = singleHouse.getElementsByClass("text fl");
        String html2 = places.get(0).child(0).html();
        String html1 = places.get(0).child(1).html();
        String html = places.get(0).child(2).html();
        String place = html2 + "-" + html1 + "-" + html;
        System.out.println("房子的位置是：" + place);


        Elements conditions = singleHouse.getElementsByClass("house-tags clearfix");
        String underPlace = conditions.get(0).getElementsByClass("default fl").html();
        String condition = "";
        //房子的价格
        String price = singleHouse.getElementsByClass("bigger").get(0).child(0).html();
        System.out.println("房子距离地铁的位置："+underPlace);
        System.out.println("房子的价格："+price);
    }
}
