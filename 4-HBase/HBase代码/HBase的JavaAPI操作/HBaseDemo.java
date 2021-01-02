package Test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseDemo {
    //获取配置信息
    public static Configuration configuration;

    static {
        configuration = HBaseConfiguration.create();
    }

    //1.判断表是否存在
    public static boolean isExist(String tableName){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);

            //管理表
            HBaseAdmin admin = (HBaseAdmin)connection.getAdmin();

            return admin.tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //2.在hbase创建表
    public static void createTable(String tableName,String... columnfamily){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            //管理表
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();

            //1.表如果存在，请输入其他表名
            if(isExist(tableName)){
                System.out.println("表存在,请输入其他表名");
            }else{
                //2.注意:创建表的话，需要创建一个描述器
                HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(tableName));

                //3.创建列族
                for(String cf:columnfamily){
                    htd.addFamily(new HColumnDescriptor(cf));
                }

                //4.创建表
                admin.createTable(htd);
                System.out.println("创建表成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("表已经存在");
        }
    }

    //3.删除hbase的表
    public static void deleteTable(String tableName) {
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            //管理表
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();

            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //2.如果表存在，删除
                admin.disableTable(TableName.valueOf(tableName));
                admin.deleteTable(TableName.valueOf(tableName));
                System.out.println("表已经删除");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //4.添加数据put 'user','rowkey','info:name','tony'
    public static void addRow(String tableName,String rowkey,String cf,String column,String value){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));
            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //2.用put方式加入数据
                Put p = new Put(Bytes.toBytes(rowkey));
                //3.加入数据
                p.addColumn(Bytes.toBytes(cf),Bytes.toBytes(column),Bytes.toBytes(value));
                t.put(p);

                System.out.println("添加数据成功");

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("添加数据失败");
        }
    }

    //5.删除表中一行数据
    public static void deleteRow(String tableName,String rowkey,String cf ){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));
            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //1.根据rowkey删除数据
                Delete delete = new Delete(Bytes.toBytes(rowkey));
                //2.删除
                t.delete(delete);
                System.out.println("删除数据成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("删除数据失败");
        }
    }

    //6.删除多行数据
    public static void deleteAll(String tableName,String... rowkeys){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));
            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //1.把delete封装到集合
                List<Delete> list = new ArrayList<Delete>();
                //2.遍历
                for (String row:rowkeys){
                    Delete d = new Delete(Bytes.toBytes(row));
                    list.add(d);
                }
                t.delete(list);
                System.out.println("删除数据成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("删除数据失败");
        }
    }

    //7.扫描表数据 scan全表扫描
    public static void scanAll(String tableName){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));

            //1.实例scan
            Scan s = new Scan();
            //2.拿到Scanner对象
            ResultScanner rs = t.getScanner(s);

            //3.遍历
            for (Result r:rs){
                Cell[] cells = r.rawCells();
                //遍历具体数据
                for (Cell c : cells){
                    System.out.print("行键为:"+Bytes.toString(CellUtil.cloneRow(c))+"  ");
                    System.out.print("列族为:"+Bytes.toString(CellUtil.cloneFamily(c))+"  ");
                    System.out.print("列名为:"+Bytes.toString(CellUtil.cloneQualifier(c))+"  ");
                    System.out.println("值为:"+Bytes.toString(CellUtil.cloneValue(c)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //8.过滤数据
    public static void getRow(String tableName,String rowkey) throws IOException {
        Connection connection = ConnectionFactory.createConnection(configuration);
        //拿到表对象
        Table t = connection.getTable(TableName.valueOf(tableName));

        //1.扫描指定数据需要实例对象Get
        Get get = new Get(Bytes.toBytes(rowkey));
        //2.可加过滤条件
        get.addFamily(Bytes.toBytes("info"));
        Result rs = t.get(get);
        //3.遍历
        Cell[] cells = rs.rawCells();
        for (Cell c : cells){
            System.out.print("行键为:"+Bytes.toString(CellUtil.cloneRow(c))+"  ");
            System.out.print("列族为:"+Bytes.toString(CellUtil.cloneFamily(c))+"  ");
            System.out.print("列名:"+Bytes.toString(CellUtil.cloneQualifier(c))+"  ");
            System.out.println("值为:"+Bytes.toString(CellUtil.cloneRow(c))+"  ");
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(isExist("ORDER_INFO"));
        createTable("Message","info");
        deleteTable("Tony");
        addRow("Message","105","info","age","12");
        deleteRow("Message","102","info");
        deleteAll("Message","101","103");
        scanAll("Message");
        getRow("Message","104");

    }
}
