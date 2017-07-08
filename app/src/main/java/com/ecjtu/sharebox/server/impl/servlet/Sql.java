package com.ecjtu.sharebox.server.impl.servlet;


import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.servlet.BaseServlet;


/**
 * Created by KerriGan on 2016/4/25.
 */
public class Sql implements BaseServlet {


    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {

        String sql = httpReq.getParameterValue("sql");


//        Context context=DBManager.getContext();
//        DBManager manager=new DBManager(context);

//        SQLiteDatabase database=DBManager.getDataBase();

//        //打开或创建test.db数据库
//        SQLiteDatabase db = openOrCreateDatabase("test.db", Context.MODE_PRIVATE, null);
//        db.execSQL("DROP TABLE IF EXISTS person");
//        //创建person表
//        db.execSQL("CREATE TABLE person (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, age SMALLINT)");
//        Person person = new Person();
//        person.name = "john";
//        person.age = 30;
//        //插入数据
//        db.execSQL("INSERT INTO person VALUES (NULL, ?, ?)", new Object[]{person.name, person.age});
//
//        person.name = "david";
//        person.age = 33;
//        //ContentValues以键值对的形式存放数据
//        ContentValues cv = new ContentValues();
//        cv.put("name", person.name);
//        cv.put("age", person.age);
//        //插入ContentValues中的数据
//        db.insert("person", null, cv);
//
//        cv = new ContentValues();
//        cv.put("age", 35);
//        //更新数据
//        db.update("person", cv, "name = ?", new String[]{"john"});
//
//        Cursor c = db.rawQuery("SELECT * FROM person WHERE age >= ?", new String[]{"33"});
//        while (c.moveToNext()) {
//            int _id = c.getInt(c.getColumnIndex("_id"));
//            String name = c.getString(c.getColumnIndex("name"));
//            int age = c.getInt(c.getColumnIndex("age"));
//            Log.i("db", "_id=>" + _id + ", name=>" + name + ", age=>" + age);
//        }
//        c.close();
//
//        //删除数据
//        db.delete("person", "age < ?", new String[]{"35"});
//
//        //关闭当前数据库
//        db.close();


//        database.beginTransaction();
//        switch (string2Int(sql.toLowerCase()))
//        {
//            case IExecsql:
//                database.execSQL(sql);
//                break;
//            case IUpdate:
//                break;
//            case IDelete:
//                break;
//            case IQuery:
//
//                break;
//        }
//
//        database.endTransaction();
//        database.close();

    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

    }


    public static String UPDATE = "update";

    public static String EXECSQL = "execsql";

    public static String DELETE = "delete";

    public static String QUERY = "query";

    public static final int IUpdate = 1;

    public static final int IExecsql = 2;

    public static final int IDelete = 3;

    public static final int IQuery = 4;

    public int string2Int(String s) {
        if (s.compareTo(UPDATE) == 0)
            return IUpdate;
        else if (s.compareTo(EXECSQL) == 0)
            return IExecsql;
        else if (s.compareTo(DELETE) == 0)
            return IDelete;
        else if (s.compareTo(QUERY) == 0)
            return 4;

        return IExecsql;
    }


}
