package com.rent.spider;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/22

*/
public class Anjuke {
    public static ReentrantLock lock = new ReentrantLock();

    public static String Anjuke(String link) throws IOException{
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

    public static void main(String[] args) {
        String proxyHost = "127.0.0.1";
        String proxyPort = "11356";
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        // 对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);

        String url = "https://nj.zu.anjuke.com/fangyuan/p1/";
    }
}
