package com.campus.forum.util;

import com.baidu.aip.contentcensor.AipContentCensor;
import com.google.common.util.concurrent.RateLimiter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 百度云内容审核工具类
 */
@Component
public class BaiduAuditUtil {

  private static final Logger logger = LoggerFactory.getLogger(BaiduAuditUtil.class);

  @Value("${baidu.cloud.appid}")
  private String appId;

  @Value("${baidu.cloud.apikey}")
  private String apiKey;

  @Value("${baidu.cloud.secretkey}")
  private String secretKey;

  private AipContentCensor client;
  private RateLimiter rateLimiter;

  @PostConstruct
  public void init() {
    // 初始化客户端
    client = new AipContentCensor(appId, apiKey, secretKey);
    // 设置网络连接参数
    client.setConnectionTimeoutInMillis(5000);
    client.setSocketTimeoutInMillis(60000);
    // 初始化限流器，设置为 1 QPS（根据百度免费版限制调整）
    rateLimiter = RateLimiter.create(1.0);
    logger.info("百度内容审核客户端初始化完成，AppID: {}", appId);
  }

  /**
   * 文本审核
   * 
   * @param text 待审核文本
   * @return 审核结果
   */
  public JSONObject auditText(String text) {
    int maxRetries = 3;
    int retryCount = 0;

    while (true) {
      try {
        // 限流控制
        rateLimiter.acquire();

        JSONObject result = client.textCensorUserDefined(text);
        logger.info("文本审核结果: {}", result);
        return result;
      } catch (Exception e) {
        retryCount++;
        logger.error("文本审核失败 (尝试 {}): {}", retryCount, e.getMessage(), e);

        if (retryCount < maxRetries) {
          // 延迟重试
          try {
            TimeUnit.SECONDS.sleep(2);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        } else {
          logger.error("文本审核达到最大重试次数，返回null");
          return null;
        }
      }
    }
    return null;
  }

  /**
   * 图片审核（使用Base64编码）
   * 
   * @param imagePath 图片本地路径
   * @return 审核结果
   */
  public JSONObject auditImage(String imagePath) {
    int maxRetries = 3;
    int retryCount = 0;

    while (true) {
      try {
        // 限流控制
        rateLimiter.acquire();

        // 读取本地图片
        String uploadsRoot = System.getProperty("user.dir") + "/uploads";
        // 去掉imagePath开头的"/uploads"前缀
        String relativePath = imagePath.replaceFirst("^/uploads", "");
        String fullPath = uploadsRoot + relativePath;
        File file = new File(fullPath);
        
        if (!file.exists() || !file.isFile()) {
          logger.error("图片文件不存在: {}", fullPath);
          // 图片文件不存在，返回null，后续会被视为审核失败
          return null;
        }
        
        // 检查文件大小（不超过4MB）
        if (file.length() > 4 * 1024 * 1024) {
          logger.error("图片文件超过4MB限制: {}", file.length() / (1024 * 1024) + "MB");
          // 图片文件超过大小限制，返回null，后续会被视为审核失败
          return null;
        }
        
        try {

          FileInputStream fis = new FileInputStream(file);
          byte[] bytes = new byte[(int) file.length()];
          fis.read(bytes);
          fis.close();
          
          // 使用字节数组直接调用审核方法
          HashMap<String, Object> options = new HashMap<>();
          JSONObject result = client.imageCensorUserDefined(bytes, options);
          logger.info("图片审核结果: {}", result);
          return result;
        } catch (Exception e) {
          logger.error("读取图片文件失败: {}", e.getMessage(), e);
          // 读取图片文件失败，返回null，后续会被视为审核失败
          return null;
        }
      } catch (Exception e) {
        retryCount++;
        logger.error("图片审核失败 (尝试 {}): {}", retryCount, e.getMessage(), e);

        if (retryCount < maxRetries) {
          // 延迟重试
          try {
            TimeUnit.SECONDS.sleep(2);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        } else {
          logger.error("图片审核达到最大重试次数，返回null");
          return null;
        }
      }
    }
    return null;
  }

  /**
   * 判断审核结果是否违规
   * 
   * @param result 审核结果
   * @return 是否违规
   */
  public boolean isViolation(JSONObject result) {
    if (result == null) {
      logger.warn("审核结果为null，默认为合规");
      return false;
    }
    try {
      // 检查是否有错误码
      if (result.has("error_code")) {
        logger.error("百度API调用失败: {}", result.toString());
        return false;
      }

      int conclusionType = result.getInt("conclusionType");
      // 只有conclusionType=2时才判定为违规
      return conclusionType == 2;
    } catch (Exception e) {
      logger.error("解析审核结果失败: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * 获取审核状态码
   * 
   * @param result 审核结果
   * @return 审核状态码：0-未审核，1-正常，2-违规，3-审核失败
   */
  public int getAuditStatus(JSONObject result) {
    if (result == null) {
      logger.warn("审核结果为null，返回审核失败");
      return 3; // 审核失败
    }
    try {

      // 检查是否有错误码
      if (result.has("error_code")) {
        logger.error("审核异常，设置状态为3");
        return 3; // 审核失败
      }

      // 检查是否存在conclusionType字段
      if (!result.has("conclusionType")) {
        logger.error("审核异常，设置状态为3");
        return 3; // 审核失败
      }

      int conclusionType = result.getInt("conclusionType");

      if (conclusionType == 0 || conclusionType == 1) {
        if (conclusionType == 0) {
          logger.info("百度返回【合规】，设置状态为1");
        } else {
          logger.info("百度返回【疑似】，视为合规，设置状态为1");
        }
        return 1; // 合规或疑似，都放行
      } else if (conclusionType == 2) {
        logger.info("百度返回【不合规】，设置状态为2");
        return 2; // 明确违规，拦截
      } else {
        logger.error("审核异常，设置状态为3");
        return 3; // 异常情况
      }
    } catch (Exception e) {
      logger.error("审核异常，设置状态为3");
      logger.error("解析审核结果失败: {}", e.getMessage(), e);
      logger.error("审核结果: {}", result.toString());
      return 3; // 解析失败 → 审核失败
    }
  }

  /**
   * 获取违规详情
   * 
   * @param result 审核结果
   * @return 违规详情
   */
  public String getViolationDetails(JSONObject result) {
    if (result == null) {
      return "";
    }
    try {
      return result.toString();
    } catch (Exception e) {
      logger.error("获取违规详情失败: {}", e.getMessage(), e);
      return "";
    }
  }
}
