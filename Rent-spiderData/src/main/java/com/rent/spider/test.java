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

        String link = "https://nj.zu.anjuke.com/fangyuan/p3/";
        Connection connect = Jsoup.connect(link);
        connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        Document doc = connect.get();
        System.out.println(doc.toString());
    }
}
