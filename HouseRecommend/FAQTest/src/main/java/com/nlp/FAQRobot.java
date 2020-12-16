package com.nlp;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.Pair;
import com.huaban.analysis.jieba.WordDictionary;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/26

*/
class zhishiku {
    double sim = 0;
    ArrayList<String> q = new ArrayList<>();
    String a;
    ArrayList<String[]> q_vec;


    public double getSim() {
        return sim;
    }

    public void setSim(double sim) {
        this.sim = sim;
    }

    public ArrayList<String> getQ() {
        return q;
    }

    public void setQ(ArrayList<String> q) {
        this.q = q;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public ArrayList<String[]> getQ_vec() {
        return q_vec;
    }

    public void setQ_vec(ArrayList<String[]> q_vec) {
        this.q_vec = q_vec;
    }

    public ArrayList<String[]> getQ_word() {
        return q_word;
    }

    public void setQ_word(ArrayList<String[]> q_word) {
        this.q_word = q_word;
    }

    ArrayList<String[]> q_word;

    zhishiku() {
    }


    @Override
    public String toString() {
        return "zhishiku{" +
                "sim=" + sim +
                ", q=" + q +
                ", a='" + a + '\'' +
                ", q_vec=" + q_vec +
                ", q_word=" + q_word +

                '}';
    }
}

public class FAQRobot {

    String zhishitxt;
    int lastTxtLen;
    boolean usedVec;
    ArrayList<zhishiku> zhs = new ArrayList<>();
    public static Logger log = Logger.getLogger(FAQRobot.class);

    FAQRobot() throws Exception {
        this.zhishitxt = "FAQ_减肥.txt";
        this.lastTxtLen = 10;
        this.usedVec = false;
        this.reload();
    }

    FAQRobot(String zhishitxt, boolean usedVec) {
        this.zhishitxt = zhishitxt;
        this.usedVec = usedVec;
    }

    public void reload() throws Exception {
        load_qa();
        load_embedding();

        System.out.println("问答知识库载入完成....");
    }

    public void load_qa() throws Exception {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        System.out.println("问答知识库开始载入");
        File infile = new File("D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\FAQTest\\src\\main\\resources\\FAQ_减肥.txt");
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String inString = "";
        boolean flag = true;
        ArrayList<String> question = new ArrayList<>();
        ArrayList<String[]> qword = new ArrayList<>();
        String answer = "";
        while ((inString = reader.readLine()) != null) {
            int abovetxt = 0;
            if (inString.startsWith("#")) abovetxt = 0;
            else if (abovetxt != 2) {
                if (inString.startsWith("【问题】")) {
                    question.add(inString.substring(4));
                    List<String> list = segmenter.sentenceProcess(inString.substring(4));
                    String[] tmp = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        tmp[i] = list.get(i);
                    }
                    qword.add(tmp);
                    abovetxt = 2;
                } else {
                    answer += inString;
                    if (question.size() != 0) {
                        zhishiku zs = new zhishiku();
                        zs.setA(answer);
                        zs.setQ(new ArrayList<>(question));
                        zs.setQ_word(new ArrayList<String[]>(qword));
                        zhs.add(zs);
                        answer = "";
                        question.clear();
                        qword.clear();
                    }
                    abovetxt = 1;
                }
            }
        }
    }

    public void load_embedding() throws Exception {

    }


    public String answer(String s, String simType) {
        if (s.equals("") || s == null) {
            return "";
        }
        String outtext = "";
        if (simType == "all" || simType.equals("all")) {
            String[] types = {"simple", "simple_pos", "vec"};
            for (String type : types) {
                outtext = "method:\t";
                return "";
            }
        } else {
            outtext = maxSimTxt(s, 0.1, "simple");
        }
        return outtext;
    }

    public String maxSimTxt(String intxt, double simCondision, String simType) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        ArrayList<String> simtype = new ArrayList<>();
        Result parse = ToAnalysis.parse(intxt);
        simtype.add("simple");
        simtype.add("simple_pos");
        simtype.add("vec");
        if (!simtype.contains(simType)) {
            return "error: maxSimTxt的simType类型不存在：{}";
        }
        if ((simType == "vec") || simtype.contains("vec")) {
            simType = "simple_pos";
        }
        String answer = "";
        double maxs = 0;
        for (zhishiku zh : zhs) {
            ArrayList<String[]> questions = simType == "vec" ? zh.q_vec : zh.q_word;
            utils ut = new utils();
            double max = 0;
            for (String[] question : questions) {
                double similarity = ut.similarity(parse, question, simType);
                max = max > similarity ? max : similarity;
            }
            zh.setSim(max);
            if (zh.getSim() > maxs) {
                maxs = zh.getSim();
                answer = zh.getA();
            }
        }
        zhishiku maxSim = new zhishiku();
        log.info("maxSim = 0 " + maxs);

        if (maxs < simCondision) {
            return "抱歉，我没有理解您的意思。";
        }


        return answer;
    }


    public static void main(String[] args) throws Exception {
        FAQRobot robot = new FAQRobot();
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("A: " + robot.answer(in.nextLine(), "simple_pos"));
        }


    }


}

