package com.campus.forum.dto;

import com.campus.forum.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentCreateDTO extends Comment {
  private List<String> imageUrls;
}
