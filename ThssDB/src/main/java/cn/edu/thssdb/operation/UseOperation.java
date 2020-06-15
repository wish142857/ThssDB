package cn.edu.thssdb.operation;

import cn.edu.thssdb.schema.Manager;

public class UseOperation extends BaseOperation {
  private String newDatabaseName;      // 数据库名称

  /**
   * [method] 构造方法
   */
  public UseOperation(String name) {
    this.newDatabaseName = name;
  }

  /**
   * [method] 执行操作
   */
  public void exec()  {
    Manager manager = Manager.getInstance();
    if (databaseName != null) manager.quitDatabase(databaseName);
    manager.switchDatabase(newDatabaseName);
  }

  public String getName() {
    return newDatabaseName;
  }
}
