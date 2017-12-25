## 创建一个 Bucket

首先我们需要初始化一个 Bucket 对象来对 Bucket 进行操作：

``` java
import com.qingstor.sdk.config.EvnContext;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.service.*;
import com.qingstor.sdk.Bucket.PutBucketOutput;

EvnContext evn = new EvnContext("ACCESS_KEY_ID_EXAMPLE", "SECRET_ACCESS_KEY_EXAMPLE");
String zoneName = "pek3a";
String bucketName = "testBucketName";
Bucket bucket = new Bucket(evn, zoneKey, bucketName);
```

上面代码中出现的对象：
- bucket 对象用于操作 Bucket，可以使用所有 Bucket 和 Object 级别的 API。


对象创建完毕后，我们需要执行真正的创建 Bucket 操作：

``` java
try {
	PutBucketOutput output = bucket.put();
	System.out.println(output.getMessage());
} catch(QSException e) {
	e.printStackTrace();	
}

```

上面代码中出现的函数：
- bucket.put() 在 `pek3a` 区域创建一个名为 `testBucketName` 的 Bucket。 

上面代码中出现的对象：
- output 对象是 bucket.put() 函数的返回体。
- output.getMessage() 是 output 对象的一个方法，用于返回响应字符串。

