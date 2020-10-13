package com.rent.spider;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/8

*/
public class Houses {
    public static ReentrantLock lock = new ReentrantLock();

    public static String House(String link) throws IOException{
        String proxyHost = "127.0.0.1";
        String proxyPort = "19856";
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        // 对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
        Connection connect = Jsoup.connect(link);
        connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        Document document = connect.get();
        return document.toString();
    }

    public static void main(String[] args) throws IOException {
        String proxyHost = "127.0.0.1";
        String proxyPort = "11356";
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        // 对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);  //设置代理来访问豆瓣

        String s1 = "https://nanjing.qfang.com/rent/f";
        ArrayList<String> list = new ArrayList<>();
        for (int i =56; i <= 100; i++) {
            list.add(s1+""+i);
        }
        WebThead webt[]= new WebThead[55];  //存储爬网页的线程
        int webpos= 0;

        //遍历每一页的url,爬取数据
        for (String string : list) {
            Connection connect = Jsoup.connect(string);
            connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
            Document doc = connect.get();
            Elements pList = doc.getElementsByClass("main-left fl");
            Elements listCon = pList.get(0).getElementsByClass("items clearfix");

            webt[webpos]= new WebThead(string, doc);
            webt[webpos++].start();

            for (Element element : listCon) {
                Elements photoAndUrl = element.getElementsByClass("photo-wrap fl");
                //获取房子的图片链接
                String imgUrl = photoAndUrl.get(0).getElementsByTag("img").attr("data-original");
                //获取房子的具体信息链接
                String houseSingleUrl = photoAndUrl.get(0).getElementsByTag("a").attr("href");
            }
        }

        for(int i= 0; i< 10; i++) {  //防止爬取线程出现乱序
            try {
                webt[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

}
