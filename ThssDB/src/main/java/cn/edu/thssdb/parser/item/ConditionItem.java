package cn.edu.thssdb.parser.item;
import cn.edu.thssdb.exception.UnknownColumnException;
import cn.edu.thssdb.parser.item.ColumnFullNameItem;
import cn.edu.thssdb.query.QueryColumn;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ComparisonType;
import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;

public class ConditionItem {
  /*
    type  meaning
    0     A op B
    1     A op const
    2     const op A
    3     const op const
    4     A is null
   */

  private boolean value;
  private ComparerItem c1 = null;
  private ComparerItem c2 = null;

  private int idx1 = -1;
  private int idx2 = -1;
  private Entry e1;
  private Entry e2;
  private int type;
  private ComparisonType cmp;
  private boolean is_bool;

  public ConditionItem(ComparerItem c1, ComparerItem c2, String cmp) {
    this.c1 = c1;
    this.c2 = c2;
    this.cmp = ComparisonType.string2ComparisonType(cmp);
    if (c1.getIsC()) {
      if (c2.getIsNull()) type = 4;
      if (c2.getIsC()) {
        type = 0;
      } else {
        type = 1;
      }
    } else {
      if (c2.getIsC()) {
        type = 2;
      } else {
        type = 3;
      }
    }
    this.is_bool = false;
  }

  public ConditionItem(boolean value) {
    this.value = value;
    this.is_bool = true;
  }

  // 计算row是否满足条件
  public boolean evaluate(Row row) {
    if (this.is_bool) {
      return this.value;
    }
    int cmp_res = 0;
    switch (this.type) {
      case 0: {
        Entry tmp1 = row.getEntries().get(idx1);
        Entry tmp2 = row.getEntries().get(idx2);
        try {
          cmp_res = tmp1.compareTo(tmp2);
        } catch (NullPointerException e) {
          return false;
        }
        break;
      }
      case 1: {
        Entry tmp = row.getEntries().get(idx1);
        try {
          cmp_res = tmp.compareTo(e1);
        } catch (NullPointerException e) {
          return false;
        }
        break;
      }
      case 2: {
        Entry tmp = row.getEntries().get(idx1);
        try {
          cmp_res = e1.compareTo(tmp);
        } catch (NullPointerException e) {
          return false;
        }
        break;
      }
      case 3: {
        try {
          cmp_res = e1.compareTo(e2);
        } catch (NullPointerException e) {
          return false;
        }
        break;
      }
      default: {
        break; // IS NULL
      }
    }

    boolean res;
    switch (cmp) {
      case LT: res = cmp_res < 0; break; // <
      case GT: res = cmp_res > 0; break;// >
      case NGT: res = cmp_res <= 0; break; // <=
      case NLT: res = cmp_res >= 0; break; // >=
      case EQ: res = cmp_res == 0; break; // ==
      case NEQ: res = cmp_res != 0; break; // >=
      default: res = row.getEntries().get(idx1).value == null; break;
    }
    return res;
  }

  public boolean getValue() {
    return this.value;
  }

  public void convertConditionToIndex(ArrayList<QueryColumn> columns) {
    if (c1.getIsC()) {
      int i = 0;
      for (QueryColumn column : columns) {
        if (column.compareTo(c1.getC())) {
          idx1 = i;
          break;
        }
        i++;
      }
      if (idx1 == -1) throw new UnknownColumnException();
      if (c2.getIsNull()) {
        type = 4;
        return;
      }
      if (c2.getIsC()) {
        type = 0;
        i = 0;
        for (QueryColumn column : columns) {
          if (column.compareTo(c2.getC())) {
            idx2 = i;
            break;
          }
          i++;
        }
        if (idx2 == -1) throw new UnknownColumnException();
      } else {
        type = 1;
        ColumnType type = columns.get(idx1).getType();
        String l = c2.getL().getString();
        Comparable val = null;
        if (c2.getL().getType() != LiteralValueItem.Type.NULL) {
          val = ColumnType.getColumnTypeValue(type, l);
        }
        e1 = new Entry(val);
      }
    } else {
      if (c2.getIsC()) {
        type = 2;
        int i = 0;
        for (QueryColumn column : columns) {
          if (column.compareTo(c2.getC())) {
            idx1 = i;
            break;
          }
          i++;
        }
        if (idx1 == -1) throw new UnknownColumnException();
        ColumnType type = columns.get(idx1).getType();
        String l = c1.getL().getString();
        Comparable val = null;
        if (c1.getL().getType() != LiteralValueItem.Type.NULL) {
          val = ColumnType.getColumnTypeValue(type, l);
        }
        e1 = new Entry(val);
      } else {
        type = 3;
        // 两个都是值，无法判断类型，默认字符串比较
        e1 = new Entry(c1.getL().getString());
        if (c2.getIsNull()) return;
        e2 = new Entry(c2.getL().getString());
      }

    }

  }

  // 返回是不是属于第一个table
  public boolean setSingleTableIdx(int firstTableColumns) {
    if (idx1 >= firstTableColumns) {
      idx1 -= firstTableColumns;
      return false;
    }
    return true;
  }

  // 返回哪个表的是主键
  public int getPrimaryIndexTable(int firstTableColumns, int primary1, int primary2) {
    if (idx1 >= firstTableColumns) {
      if (idx1 == primary1 + firstTableColumns) return idx1;
    } else {
      if (idx1 == primary1) return idx1;
    }
    if (idx2 >= firstTableColumns) {
      if (idx2 == primary2 + firstTableColumns) return idx2;
    } else {
      if (idx2 == primary2) return idx2;
    }
    return -1;
  }

  public int getType() {
    return type;
  }

  public int getIdx1() {
    return idx1;
  }

  public int getIdx2() {
    return idx2;
  }

  public ComparisonType getCmp() {
    return cmp;
  }

  public Entry getE1() {
    return e1;
  }
}
