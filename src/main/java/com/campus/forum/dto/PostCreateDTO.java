package com.campus.forum.dto;

import com.campus.forum.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostCreateDTO extends Post {
  private List<String> imageUrls;
}
