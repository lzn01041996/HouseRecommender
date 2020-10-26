package com.nlp.example;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/23

*/
public class Word2VecRaw {
    private static Logger log = LoggerFactory.getLogger(Word2VecRaw.class);

    public static void main(String[] args) throws IOException {
       log.info("*************加载数据************");
        SentenceIterator iter = new LineSentenceIterator(new File("D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\Word2Vec\\src\\main\\resources\\pathToWriteto"));
        iter.setPreProcessor(new SentencePreProcessor() {
            @Override
            public String preProcess(String s) {
                return s.toLowerCase();
            }
        });

        //每行用空格分个得到单词
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        log.info("建立模型.......");
        //batchSize 是你一次处理的单词数量。
        //minWordFrequency 是单词必须出现在语料库中的最小次数。 在这里，如果它出现少于5次，则不会学习。 单词必须出现在多个上下文中才能学习有关它们的有用特征。 在非常大的语料库中，提高最小值是合理的。
        //useAdaGrad - Adagrad为每个特征创建不同的梯度。 在这里，我们并不关心这一点。
        //layerSize 指定单词向量中的特征数。这等于特征空间中的维数。由500个特征表示的词成为500维空间中的点。
        //learningRate 是每个更新系数的步长，因为单词在特征空间中被重新定位。
        //minLearningRate 是学习率的底板。学习速率随着你训练的单词数量的减少而衰减。如果学习率下降太多，网络的学习就不再有效了。这保持系数移动。
        //iterate 告诉网络它正在训练的数据集的批次。
        //tokenizer 从当前批次中为它提供单词。
        //vec.fit() 告诉配置的网络开始训练。
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();
        log.info("fitting word2vec model......");

        vec.fit();

        // 写入词向量
        WordVectorSerializer.writeWord2VecModel(vec,"pathToWriteto.txt");

        log.info("最接近的10个词:");
        Collection<String> lst = vec.wordsNearest("抑郁症", 10);
        System.out.println(lst);
        //输出: [night, week, year, game, season, during, office, until, -]
    }
}
