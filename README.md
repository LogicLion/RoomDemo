
# Room特性
1. **编译时 sql 语句检查。** 相信大家都有过 app 跑起来，执行到 db 语句的时候 crash，检查之后发现原来是 sql 语句少了一个 ) 或者其它符号之类的经历。Room 会在编译阶段检查你的 DAO 中的 sql 语句，如果写错了（包括 sql 语法错误跟表名、字段名等等错误），会直接编译失败并提醒你哪里不对。
2. **sql 查询直接关联到 Java 对象。** 这个应该不用详细解释了，虽然很多第三方 db 库早已经实现。
3. **耗时操作主动要求异步处理。** 这一点还是挺值得注意的，Room 会在执行 db 操作时判断是不是在 UI 线程，比如当你需要插入一条记录到数据库时，Room 会让你放到异步线程去做，否则会直接 crash 掉 app 来告诉你不这样做容易阻塞 UI 线程。虽说死相难看了点（个人觉得打个警告不就完了么?），但对于开发者开发出高质量的应用还是有帮助的。
4. **基于注解编译时自动生成代码。** 这个应该算是 Room 工作原理的核心所在了，你要写的代码之所以这么少，说白了还不是因为 Google 给你写好了很多？希望以后有时间能写一篇源码分析出来，那个时候再讲吧。
5. **API 设计符合 Sql标准。** 方便扩展进行各种 db 操作。
6. **Room提供了使用RxJava中的Single、Flowable、Maybe对象的异步查询的方法，来实现观察者模式。**（一个目标物件管理所有相依于它的观察者物件，并且在它本身的状态改变时主动发出通知），也就是说在rxjava加持下，增删改操作后数据库数据更新后，会主动调用query

## Maybe

```
@Query(“SELECT * FROM Users WHERE id = :userId”)
Maybe<User> getUserById(String userId);
```
发生了什么呢？
1. 若数据库中没有用户，那么Maybe就会被complete（RxJava中概念）
2. 若数据库中有一个用户，那么Maybe就会触发onSuccess方法并且被complete
3. 若数据库中用户信息在Maybe被complete之后被更新了，啥都不会发生

## Single

```
@Query(“SELECT * FROM Users WHERE id = :userId”)
Single<User> getUserById(String userId);
```
就会发生这些事情：
1. 若数据库中没有用户，那么Single就会触发onError(EmptyResultSetException.class)
2. 若数据库中有一个用户，那么Single就会触发onSuccess
3. 若数据库中用户信息在Single.onComplete调用之后被更新了，啥都不会发生，因为数据流已经完成了

## Flowable

```
@Query(“SELECT * FROM Users WHERE id = :userId”)
Flowable<User> getUserById(String userId);
```
Flowable会这样运行：
1. 若数据库中没有用户，那么Flowable就不会发射事件，既不运行onNext,也不运行onError
2. 若数据库中有一个用户，那么Flowable就会触发onNext
3. 若数据库中用户信息被更新了，Flowable就会自动发射事件，允许你根据更新的数据来更新UI界面

# Room使用
## 添加依赖
```
//Room
compile 'android.arch.persistence.room:runtime:1.1.0-alpha3'
annotationProcessor 'android.arch.persistence.room:compiler:1.1.0-alpha3'

// RxJava support for Room
compile 'android.arch.persistence.room:rxjava2:1.1.0-alpha1'
```
## Room的三大组件

- Entity。实体，说白了就是我们最常见的一个对象
- DAO。Data Access Object，把你 Entity 所有的 CRUD 业务代码封装在这里就好
- Database。数据库，Room 提供了一个非常方便的静态方法来供我们创建数据库

```
@Entity(tableName = "user")
public class UserBean {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String age;

    public UserBean(int id,String name, String age) {
        this.id=id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
```
> @Entity(tableName = "user") 声明这是一个实体类。其中，tableName 如果不写，那么默认类名就是表名。
> @PrimaryKey(autoGenerate = true) 声明这是一个主键。其中，autoGenerate = true 代表自动生成
> @Ignore声明忽略该属性

```
@Dao
public interface UserDAO {
    @Query("select * from user")
    Flowable<List<UserBean>> getUserList();

    @Query("select * from user where name=:name")
    Flowable<UserBean> getUserByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(UserBean userBean);

    @Delete
    void deleteUser(UserBean userBean);

    @Update
    void updateUser(UserBean userBean);
}
```
> @Dao 声明这是 DAO。
> CRUD操作全部使用注解声明，需要具体 sql 语句的，直接在注解里书写就好。只有query使用sql语句，增删改根据传入的对象操作， 根据每个entity的主键作为判断的依据。

```
@Database(entities = {UserEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    //单例
    private static AppDatabase sInstance;
    public static AppDatabase getDatabase(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "user.db").build();
        }
        return sInstance;
    }
    public static void onDestroy() {
        sInstance = null;
    }

    //声明提供DAO对象
    public abstract UserEntityDao getUserEntityDao();
}
```

> @Database 声明这是一个数据库类。
> 这个类封装成单例，在任何地方需要执行数据库操作的时候，可以直接获得来使用，Room 提供了一个静态的方法，用来在默认的构造方法里创建了一个数据库，我在这里起的名称是 user.db。 
> @exportSchema 比较有意思，Google 建议是传 true，这样可以把 Scheme 导出到一个文件夹里面，Google 还建议你把这个文件上传到 VCS，具体的可以直接点进去看注释。
把所有 Entity 的 DAO 接口类全部声明成 abstract 的到这里来。
> Google 会在编译时自动帮我们生成这些抽象类和方法的实现，代码在 app/build/generated/source/apt/debug