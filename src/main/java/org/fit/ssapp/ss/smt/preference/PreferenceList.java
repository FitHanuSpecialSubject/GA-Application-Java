package org.fit.ssapp.ss.smt.preference;

import java.util.Set;

/**
 * PreferenceList -------------- Giải thích (Explanation) Vi: + Danh sách ưa thích của một cá thể
 * nào đó trong dữ liệu bài toán matching + Danh sách này sẽ bao gồm các cá thể của các set còn lại
 * (khác với set của cả thể sở hữu PreferenceList này) + Mỗi cá thể của các set còn lại khi ở trong
 * PreferenceList này sẽ có Số điểm, thứ hạng ưa thích được đánh giá bởi thằng chủ sở hữu preference
 * list này En: + Lazy -------------- Các thuộc tính: 1. position: vị trí, số thứ tự trong danh sách
 * dữ liệu matching (position of individual in matching data) 2. score: Điểm tương ứng với từng
 * "position" 3. rank: thứ hạng của "position" trong PreferenceList này 4. set: Số set tương ứng
 * -------------- Theo TA, các implementation của PreferenceList không cần phải bắt buộc có những
 * thuộc tính như trên (trong những trường hợp yêu cầu hiệu năng tính toán) nhưng nên implement
 * những hàm ở bên dưới. -------------- Cảm ơn vì đã đọc.
 *
 * @author Thành
 */
public interface PreferenceList {

  /**
   * get size of given set no.
   *
   * @param set set no
   * @return size
   */
  int size(int set);

  /**
   * get the least score node out of (currentNodes and newNode).
   *
   * @param set          set no
   * @param newNode      matching node
   * @param currentNodes matched nodes
   * @return index of the least score node
   */
  int getLeastNode(int set, int newNode, Set<Integer> currentNodes);

  /**
   * get lower score node out of two.
   *
   * @param set     set no
   * @param newNode n1
   * @param oldNode n2
   * @return index of the lower score node
   */
  int getLeastNode(int set, int newNode, int oldNode);

  /**
   * is proposeNode score greater than preferNodeCurrentNode.
   *
   * @param set                   set no
   * @param proposeNode           propose node
   * @param preferNodeCurrentNode Current node of prefer node
   * @return title true
   */
  boolean isScoreGreater(int set, int proposeNode, int preferNodeCurrentNode);

  /**
   * get score of given position.
   *
   * @param position position
   * @return score
   */
  double getScore(int position);

  /**
   * Validate score of all preference list
   * @return isUniform
   */
  boolean isUniformPreference();
}
