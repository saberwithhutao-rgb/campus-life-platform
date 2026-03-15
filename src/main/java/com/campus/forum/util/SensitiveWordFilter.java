package com.campus.forum.util;

import com.campus.forum.entity.SensitiveWord;
import com.campus.forum.repository.SensitiveWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Component
public class SensitiveWordFilter {

  private final SensitiveWordRepository sensitiveWordRepository;

  // DFA字典树的根节点
  private final Map<Character, Map<Character, ?>> root = new HashMap<>();

  @Autowired
  public SensitiveWordFilter(SensitiveWordRepository sensitiveWordRepository) {
    this.sensitiveWordRepository = sensitiveWordRepository;
  }

  /**
   * 初始化敏感词库，构建DFA字典树
   */
  @PostConstruct
  public void init() {
    List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findByStatus(1);
    for (SensitiveWord word : sensitiveWords) {
      addWord(word.getWord());
    }
  }

  /**
   * 向DFA字典树中添加敏感词
   */
  private void addWord(String word) {
    if (word == null || word.isEmpty()) {
      return;
    }

    Map<Character, Map<Character, ?>> current = root;
    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      if (!current.containsKey(c)) {
        current.put(c, new HashMap<Character, Object>());
      }
      current = (Map<Character, Map<Character, ?>>) current.get(c);
    }
    // 标记敏感词结束
    current.put('\0', null);
  }

  /**
   * 判断文本是否包含敏感词
   */
  public boolean hasSensitive(String text) {
    if (text == null || text.isEmpty()) {
      return false;
    }

    for (int i = 0; i < text.length(); i++) {
      if (checkSensitive(text, i)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查从指定位置开始是否包含敏感词
   */
  private boolean checkSensitive(String text, int start) {
    Map<Character, Map<Character, ?>> current = root;
    for (int i = start; i < text.length(); i++) {
      char c = text.charAt(i);
      if (!current.containsKey(c)) {
        return false;
      }
      current = (Map<Character, Map<Character, ?>>) current.get(c);
      if (current.containsKey('\0')) {
        return true;
      }
    }
    return false;
  }
}