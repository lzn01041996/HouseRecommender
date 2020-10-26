package com.nlp;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import sun.rmi.log.ReliableLog;

import java.util.*;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/26

*/
public class utils {
    Map<String, Double> POS_WEIGHT = new HashMap<>();
    private static Logger logger = Logger.getLogger(utils.class);
    utils(){
        POS_WEIGHT.put("Ag",1.0);//形语素
        POS_WEIGHT.put("a",0.5);//形容词
        POS_WEIGHT.put("ad",0.5);//副形词
        POS_WEIGHT.put("an",1.0);//名形词
        POS_WEIGHT.put("b",1.0);//区别词
        POS_WEIGHT.put("c",0.2);//连词
        POS_WEIGHT.put("dg",0.5);//副语素
        POS_WEIGHT.put("d",0.5);//副词
        POS_WEIGHT.put("e",0.5);//叹词
        POS_WEIGHT.put("f",0.5);//方位词
        POS_WEIGHT.put("g",0.5);//语素
        POS_WEIGHT.put("h",0.5);//前接成分
        POS_WEIGHT.put("i",0.5);//成语
        POS_WEIGHT.put("j",0.5);//简称略语
        POS_WEIGHT.put("k",0.5);//后接成分
        POS_WEIGHT.put("l",0.5);//习用语
        POS_WEIGHT.put("m",0.5);//数词
        POS_WEIGHT.put("Ng",0.5);//名语素
        POS_WEIGHT.put("n",1.0);//名词
        POS_WEIGHT.put("nr",1.0);//人名
        POS_WEIGHT.put("ns",1.0);//地名
        POS_WEIGHT.put("nt",1.0);//机构团体
        POS_WEIGHT.put("nz",1.0);//其他专名
        POS_WEIGHT.put("o",0.5);//拟声词
        POS_WEIGHT.put("p",0.3);//介词
        POS_WEIGHT.put("q",0.5);//量词
        POS_WEIGHT.put("r",0.2);//代词
        POS_WEIGHT.put("s",1.0);//处所词
        POS_WEIGHT.put("tg",0.5);//时语素
        POS_WEIGHT.put("t",0.5);//时间词
        POS_WEIGHT.put("u",0.5);//助词
        POS_WEIGHT.put("vg",0.5);//动语素
        POS_WEIGHT.put("v",1.0);//动词
        POS_WEIGHT.put("vd",1.0);//副动词
        POS_WEIGHT.put("vn",1.0);//名动词
        POS_WEIGHT.put("w",0.01);//标点符号
        POS_WEIGHT.put("x",0.5);//非语素字
        POS_WEIGHT.put("y",0.5);//语气词
        POS_WEIGHT.put("z",0.5);//状态词
        POS_WEIGHT.put("un",0.3);//未知词
    }

    public Logger getlogger(String name, ReliableLog.LogFile logFile){
        logger.setLevel(Level.DEBUG);
        return logger;
    }

    public double similarity(Result a,String[] b, String method){
        List<String> list =  Arrays.asList(b);
        if (a.size()==0 || b.length == 0){
            return 0;
        }
        double sim_weight = 0;
        double total_weight = 0;

        if (method == "simple" || method.equals("simple")){
            return (a.size() & a.size()) / a.size();
        }else if (method == "simple_pos" || method.equals("simple_pos")){

            for (Term term : a) {
                sim_weight += list.contains(term.getName()) ? POS_WEIGHT.get(term.getNatureStr()) : 0;
                total_weight += POS_WEIGHT.get(term.getNatureStr());
            }
            return  total_weight > 0 ? sim_weight / total_weight : 0;
        }else if (method == "vec" || method.equals("vec")){
            //********
            sim_weight = 0;
            total_weight = 0;

        }
        Set<Integer> set = new HashSet<>();
        return 0;
    }


}
