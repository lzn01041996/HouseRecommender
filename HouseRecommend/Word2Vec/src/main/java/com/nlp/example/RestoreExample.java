package com.nlp.example;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.FileNotFoundException;

/** Created by zhaoyy on 2016/12/19. */
public class RestoreExample {

  public static void main(String[] args) throws FileNotFoundException {
    Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel("D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\Word2Vec\\src\\main\\resources\\log.log");
    System.out.println(word2Vec.wordsNearest("抑郁症", 10));
  }
}
