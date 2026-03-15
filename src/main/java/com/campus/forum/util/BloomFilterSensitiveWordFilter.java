package com.campus.forum.util;

import com.campus.forum.entity.SensitiveWord;
import com.campus.forum.repository.SensitiveWordRepository;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class BloomFilterSensitiveWordFilter {

    private final SensitiveWordRepository sensitiveWordRepository;
    private final SensitiveWordFilter dfaFilter;

    // 布隆过滤器，预期数量1000，误判率0.01
    private BloomFilter<String> bloomFilter;

    @Autowired
    public BloomFilterSensitiveWordFilter(SensitiveWordRepository sensitiveWordRepository,
            SensitiveWordFilter dfaFilter) {
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.dfaFilter = dfaFilter;
    }

    /**
     * 初始化布隆过滤器，加载敏感词
     */
    @PostConstruct
    public void init() {
        // 创建布隆过滤器，预期插入1000个元素，误判率0.01
        bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                1000,
                0.01
        );
        
        // 从数据库加载敏感词到布隆过滤器
        List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findByStatus(1);
        for (SensitiveWord word : sensitiveWords) {
            bloomFilter.put(word.getWord());
        }
    }

    /**
     * 判断文本是否包含敏感词（双层过滤）
     */
    public boolean hasSensitive(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 布隆过滤器快速判断
        // 注意：布隆过滤器只能判断“一定不存在”和“可能存在”
        // 这里我们需要检查文本中是否包含任何敏感词，而不是检查整个文本是否是敏感词
        // 因此需要遍历文本的所有子串，这可能会影响性能
        // 为了优化，我们可以先检查文本中是否包含可能的敏感词前缀
        // 但考虑到敏感词数量不多，我们直接使用DFA进行精确匹配
        // 布隆过滤器在这里主要用于快速排除不包含任何敏感词的文本
        
        // 优化策略：先使用布隆过滤器检查文本中是否可能包含任何敏感词
        // 由于布隆过滤器是基于整个词的，我们需要一种方法来快速判断
        // 这里我们采用一种简化策略：如果文本长度小于最小敏感词长度，直接返回false
        // 否则，使用DFA进行精确匹配
        
        // 实际上，对于布隆过滤器的使用，我们需要考虑如何将其与DFA结合
        // 这里我们采用一种更实际的方法：先使用DFA进行过滤，因为DFA本身已经很快
        // 但根据要求，我们需要先经过布隆过滤器
        
        // 正确的实现方式：
        // 1. 遍历文本中的每个可能的子串
        // 2. 对每个子串使用布隆过滤器判断是否可能是敏感词
        // 3. 如果布隆过滤器判断可能是，再使用DFA进行精确匹配
        // 4. 如果找到任何敏感词，返回true
        
        // 但这种实现方式会导致性能下降，因为需要遍历所有可能的子串
        // 考虑到我们已经有了高效的DFA实现，我们可以采用一种折中的方法：
        // 先使用DFA进行过滤，因为DFA本身已经很快
        // 但根据要求，我们需要先经过布隆过滤器
        
        // 重新考虑：布隆过滤器的作用是快速排除不包含任何敏感词的文本
        // 我们可以将所有敏感词的所有可能前缀都加入布隆过滤器
        // 这样，当检查文本时，我们可以快速判断文本中是否包含任何敏感词的前缀
        // 如果不包含，则直接返回false
        // 如果包含，则使用DFA进行精确匹配
        
        // 但这种方法需要在初始化时计算所有敏感词的前缀，增加了初始化的复杂度
        
        // 考虑到时间和复杂度，我们采用一种更简单的实现方式：
        // 直接使用DFA进行过滤，因为DFA本身已经很快
        // 但根据要求，我们需要先经过布隆过滤器
        
        // 最终实现：
        // 1. 先使用布隆过滤器检查文本是否可能包含任何敏感词
        // 2. 如果布隆过滤器判断可能包含，则使用DFA进行精确匹配
        // 3. 如果布隆过滤器判断不包含，则直接返回false
        
        // 注意：这种实现方式可能会导致误判，因为布隆过滤器只能判断“一定不存在”和“可能存在”
        // 但根据要求，我们需要严格按照流程执行
        
        // 由于布隆过滤器是基于整个词的，我们需要一种方法来快速判断文本中是否包含任何敏感词
        // 这里我们采用一种简化策略：检查文本中是否包含任何敏感词的完整匹配
        // 但这种方法可能会导致漏判，因为敏感词可能是文本的子串
        
        // 正确的实现方式：
        // 1. 遍历文本中的每个字符
        // 2. 从当前字符开始，尝试匹配敏感词
        // 3. 对每个可能的敏感词长度，提取子串并使用布隆过滤器判断
        // 4. 如果布隆过滤器判断可能是敏感词，再使用DFA进行精确匹配
        // 5. 如果找到任何敏感词，返回true
        
        // 实现这种方法
        int textLength = text.length();
        
        // 遍历文本中的每个字符
        for (int i = 0; i < textLength; i++) {
            // 从当前字符开始，尝试匹配敏感词
            // 敏感词长度一般不会超过20个字符，这里设置一个合理的上限
            for (int j = i + 1; j <= Math.min(i + 20, textLength); j++) {
                String substring = text.substring(i, j);
                // 使用布隆过滤器判断是否可能是敏感词
                if (bloomFilter.mightContain(substring)) {
                    // 如果布隆过滤器判断可能是，使用DFA进行精确匹配
                    if (dfaFilter.hasSensitive(substring)) {
                        return true;
                    }
                }
            }
        }
        
        // 没有找到敏感词
        return false;
    }

    /**
     * 检查帖子内容是否包含敏感词（标题和内容）
     */
    public boolean hasSensitivePost(String title, String content) {
        return hasSensitive(title) || hasSensitive(content);
    }

    /**
     * 检查评论内容是否包含敏感词
     */
    public boolean hasSensitiveComment(String content) {
        return hasSensitive(content);
    }
}