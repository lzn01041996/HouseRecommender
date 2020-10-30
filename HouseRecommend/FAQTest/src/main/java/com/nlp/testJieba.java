package com.nlp;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.Hit;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import com.huaban.analysis.jieba.viterbi.FinalSeg;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/26

*/
public class testJieba {

    public static void main(String[] args) {
        String words = "中国是世界四大文明古国之一，有着悠久的历史，距今5000年前，" +
                "以中原地区为中心开始出现聚落组织进而成国家和和朝代，后历经多次演变和朝代更迭，持续时间较长的朝代有" +
                "夏、商、周、汉、晋、唐、宋、元、明、清等。中原王朝历史上不断与北方游牧民族交往、征战，众多民族融合成为中" +
                "华民族。20世纪初辛亥革命后，中国的君主政体退出历史舞台，取而代之的是共和政体。1949年中华人民共和国成立后，在中" +
                "国大陆建立了人民代表大会制度的政体。中国有着多彩的民俗文化，传统艺术形式有诗词、戏曲、书法和国画等，春节、元宵、清明、端" +
                "" +
                "午、中秋、重阳等是中国重要的传统节日.";
        String s = "没有锻炼基础的人，如何增肌与减脂？";
        String tes = "---";
        String s1 = tes.toUpperCase();
        String s2 = "hello";
        String s3 = s2.toUpperCase();

        System.out.println();
    }

    public String licenseKeyFormatting(String S, int K) {
        String newString =  S.replace("-","").toUpperCase();
        if (S.length() <= K){
            return S.toUpperCase();
        }
        int a = newString.length() % K == 0 ? K :  newString.length() % K;
        StringBuilder sb = new StringBuilder();
        sb.append(newString.substring(0,a));
        sb.append("-");
        while(a <=newString.length() - K){
            sb.append(newString.substring(a,a+K));
            sb.append("-");
            a = a+K;
        }
        return sb.toString().substring(0,sb.length()-1);
    }
}
