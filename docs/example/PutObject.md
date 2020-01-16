## PutObjects Example



### Code Snippet

Initialize the Bucket service with access-key-id and secret-access-key

```
EnvContext env = new EnvContext(accessKey,accessSecret);
String zoneKey = "pek3a";
String bucketName = "testBucketName";
Bucket bucket = new Bucket(env, zoneKey, bucketName);

```

then you can put object

```java
try {
    putObject(bucket, "folder/test.mp3", "/Users/chengww/Downloads/test.mp3");
} catch (FileNotFoundException e) {
    e.printStackTrace();
}

```

```java
    /**
     * Put a file to the bucket.
     *
     * @param bucket    bucket
     * @param objectKey looks like this: "folder/fileName".<br/>
     *                  If objectKey = "fileName", we will put the object into the bucket's root.
     * @param filePath  local file path
     * @throws FileNotFoundException if file does not exist, the exception will occurred.
     */
    private void putObject(Bucket bucket, String objectKey, String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory())
            throw new FileNotFoundException("File does not exist or it is a directory.");

        Bucket.PutObjectInput input = new Bucket.PutObjectInput();
        input.setBodyInputFile(file);
        try {
            Bucket.PutObjectOutput output = bucket.putObject(objectKey, input);
            if (output.getStatueCode() == 201) {
                System.out.println("PUT Object OK.");
                System.out.println("key = " + objectKey);
                System.out.println("path = " + filePath);
            } else {
                // Failed
                System.out.println("Failed to PUT object.");
                System.out.println("key = " + objectKey);
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