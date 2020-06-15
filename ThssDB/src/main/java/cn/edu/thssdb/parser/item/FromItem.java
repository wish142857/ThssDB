package cn.edu.thssdb.parser.item;

import cn.edu.thssdb.parser.item.OnItem;

import java.util.ArrayList;

public class FromItem {

  private String tableNameA = null;
  private String tableNameB = null;
  private JoinType joinType;
  private ArrayList<OnItem> onItems;

  public enum JoinType{
    NONE, PRODUCT, NATURAL_INNER_JOIN, INNER_JOIN_ON,
    LEFT_OUTER_JOIN_ON, RIGHT_OUTER_JOIN_ON, FULL_OUTER_JOIN_ON
  }

  public String getTableNameA() {
    return tableNameA;
  }

  public String getTableNameB() {
    return tableNameB;
  }

  public JoinType getJoinType() {
    return joinType;
  }

  public ArrayList<OnItem> getOnItems() {
    return onItems;
  }

  public FromItem(String tableNameA, String tableNameB, JoinType joinType, ArrayList<OnItem> onItems){
    if (tableNameA != null) this.tableNameA = tableNameA.toUpperCase();
    if (tableNameB != null) this.tableNameB = tableNameB.toUpperCase();
    this.joinType = joinType;
    this.onItems = onItems;
  }
}
