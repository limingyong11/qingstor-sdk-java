## CopyObject Example



### Code Snippet

Initialize the Bucket service with access-key-id and secret-access-key

```
EnvContext env = new EnvContext(accessKey,accessSecret);
String zoneKey = "pek3a";
String bucketName = "testBucketName";
Bucket bucket = new Bucket(env, zoneKey, bucketName);

```

then you can copy objects


```
    private void putObjectCopy(Bucket bucket, String copySource, String newObjectKey) {
        try {
            Bucket.PutObjectInput input = new Bucket.PutObjectInput();
            input.setXQSCopySource(copySource); // CopySource looks like this: "/bucketName/folder/fileName"
            Bucket.PutObjectOutput output = bucket.putObject(newObjectKey, input); // NewObjectKey looks like this: "folder-copied/fileName"
            if (output.getStatueCode() == 201) {
                // Created
                System.out.println("Put Object Copy: Copied.");
                System.out.println("From " + copySource + " to " + newObjectKey);
            } else {
                // Failed
                System.out.println("Put Object Copy: Failed to copy.");
                System.out.println("From " + copySource + " to " + newObjectKey);
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