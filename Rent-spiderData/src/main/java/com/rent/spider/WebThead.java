package com.rent.spider;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.logging.FileHandler;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/8

*/
public class WebThead extends Thread {
    String string;
    Document doc;

    WebThead(String str, Document docc){
        string= str;
        doc= docc;
    }
    private static Logger logger = Logger.getLogger(WebThead.class);

    synchronized public void run() {
        Houses.lock.lock();
        try {
            Elements pList = doc.getElementsByClass("main-left fl");
            Elements listCon = pList.get(0).getElementsByClass("items clearfix");

        for (Element element : listCon) {
            Thread.sleep(3000);
            Elements photoAndUrl = element.getElementsByClass("photo-wrap fl");
            //获取房子的图片链接
            String imgUrl = photoAndUrl.get(0).getElementsByTag("img").attr("data-original");
            //获取房子的具体信息链接
            String houseSingleUrl = photoAndUrl.get(0).getElementsByTag("a").attr("href");
            Thread.sleep(3000);
            String title = element.getElementsByClass("list-main-header clearfix").get(0).child(0).html().replace("&nbsp;"," ");

            Elements infosSingle = element.getElementsByClass("house-metas clearfix");
            String singleType = "";
            String size = "";
            String direcType = "";
            String singleLayer = "";
            String rentType = "";
            if (infosSingle.get(0).childNodeSize()>=9){
                //获取房型
                singleType = infosSingle.get(0).child(1).html();
                //获取面积
                size = infosSingle.get(0).child(3).html();
                //获取楼层
                direcType = infosSingle.get(0).child(5).html();
                //获取出租类型
                singleLayer = infosSingle.get(0).child(7).html();
                rentType = infosSingle.get(0).child(9).child(0).html();
            }

            Thread.sleep(2000);
            Elements places = element.getElementsByClass("text fl");
            String html2 = places.get(0).child(0).html();
            String html1 = places.get(0).child(1).html();
            String html = places.get(0).child(2).html();
            String place = html2 + "-" + html1 + "-" + html;

            Thread.sleep(2000);
            Elements conditions = element.getElementsByClass("house-tags clearfix");
            String underPlace = conditions.get(0).getElementsByClass("default fl").html();
            String schoolPlace = conditions.get(0).getElementsByClass("school fl").get(0).child(0).html();
            //房子的价格
            String price = element.getElementsByClass("bigger").get(0).child(0).html();
            //输出信息到日志
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(imgUrl.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(houseSingleUrl.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(title.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(singleType.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(size.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(direcType.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(singleLayer.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(rentType.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(place.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(underPlace.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(schoolPlace.replace("\t","").replace("\r","").replace("\n","")).append("^")
                    .append(price.replace("\t","").replace("\r","").replace("\n",""));
            logger.info(stringBuilder.toString());



        }
        }catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Houses.lock.unlock();    	//释放锁
        }



    }
}
