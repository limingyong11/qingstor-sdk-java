## 删除一个 Bucket

首先我们需要初始化一个 Bucket 对象来对 Bucket 进行操作：

```java
EnvContext env = new EnvContext("ACCESS_KEY_ID_EXAMPLE", "SECRET_ACCESS_KEY_EXAMPLE");
String zoneName = "pek3a";
String bucketName = "testBucketName";
Bucket bucket = new Bucket(env, zoneKey, bucketName);
```

上面代码中出现的对象：

- bucket 对象用于操作 Bucket，可以使用所有 Bucket 和 Object 级别的 API。

对象创建完毕后，我们需要执行真正的删除 Bucket 操作：

```java
    private void deleteBucket(Bucket bucket) {
        try {
            Bucket.DeleteBucketOutput output = bucket.delete();
            if (output.getStatueCode() == 204) {
                // Deleted
                System.out.println("Delete Bucket: Deleted.");
            } else {
                // Failed
                System.out.println("Failed to Delete Bucket.");
                System.out.println("StatueCode = " + output.getStatueCode());
                System.out.println("Message = " + output.getMessage());
                System.out.println("RequestId = " + output.getRequestId());
                System.out.println("Code = " + output.getCode());
                System.out.println("Url = " + output.getUrl());
            }
        } catch (QSException e) {
            e.printStackTrace();
        }
    }
```

上面代码中出现的函数：

- bucket.delete() 在 `pek3a` 区域删除一个名为 `testBucketName` 的 Bucket。

上面代码中出现的对象：

- output 对象是 bucket.delete() 函数的返回体。
- output.getMessage() 是 output 对象的一个方法，用于返回错误信息。
