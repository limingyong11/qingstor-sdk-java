## Basic Image Progress

Used for basic image progress to images stored in QingStor storage service,
such as format conversion, image crop, rotate, add a watermark and so on.

The image formats that are currently supported are these below:

- png
- tiff
- webp
- jpeg
- pdf
- gif
- svg

> The image after encryption is not supported to do image progress.

> And the maximum size of a picture is 10M.

See the demo below to do image progress use ImageProgressClient.

See [API Docs](https://docs.qingcloud.com/qingstor/data_process/image_process/) for more information.

## Code Snippet

```java
    private void imageProgressClient() throws QSException {
        EvnContext evn = new EvnContext("key", "secret");
        Bucket bucket = new Bucket(
                evn, sh1a, "bucketName");
        ImageProcessClient client = new ImageProcessClient("head.jpg", bucket);

        // Get image info
        Bucket.ImageProcessOutput imageProcessOutput = client.info().imageProcess();
        String result = inputStream2Json(imageProcessOutput.getBodyInputStream());
        if (imageProcessOutput.getStatueCode() >= 200 && imageProcessOutput.getStatueCode() < 300) {
            // Success
            ImageInfoBean imageInfo = new Gson().fromJson(result, ImageInfoBean.class);

            System.out.println("Width = " + imageInfo.getWidth() + " px");
            System.out.println("Height = " + imageInfo.getHeight() + " px");
            System.out.println("Orientation = " + imageInfo.getOrientation());
            System.out.println("isAlpha = " + imageInfo.isAlpha());
            System.out.println("Type = " + imageInfo.getType());
            System.out.println("Space = " + imageInfo.getSpace());
        } else { // Failed
            OutputModel outputModel = new Gson().fromJson(result, OutputModel.class);
            outputModel.setStatueCode(imageProcessOutput.getStatueCode());

            System.out.println("Get image info request failed.");
            System.out.println("StatueCode = " + outputModel.getStatueCode());
            System.out.println("Code = " + outputModel.getCode());
            System.out.println("RequestId = " + outputModel.getRequestId());
            System.out.println("Message = " + outputModel.getMessage());
            System.out.println("Url = " + outputModel.getUrl());
        }

        // Do image progress
        ImageProcessClient actionClient = new ImageProcessClient("head.jpg", bucket);
        actionClient.crop(new ImageProcessClient.CropParam.Builder() // Image crop
                .width(100)
                .height(100)
                .gravity(0) // 0 = center; 1 = north; 2 = east; 3 = south; 4 = west; 5 = north west;
                            // 6 = north east;7 = south west; 8 = south east;; 9 = auto; default = 0.
                .build())
                .rotate(new ImageProcessClient.RotateParam(90)) // Rotate
                .resize(new ImageProcessClient.ResizeParam.Builder() // Resize
                        .width(200)
                        .height(200)
                        .mode(0) // 0 means fixed width, abbreviated filling, 1 for automatic adjustment based on width and height;
                                 // 2 is abbreviated according to the width to height ratio of 4:4. If only one of the width and height is set, it is scaling according to the width or height. The default is 0.
                        .build())
                .waterMark(new ImageProcessClient.WaterMarkParam.Builder("text")
                        .color("#FFFFFF")
                        .dpi(400)
                        .opacity(0.8)
                        .build())
                .waterMarkImage(new ImageProcessClient.WaterMarkImageParam.Builder("https://www.qingcloud.com/static/assets/images/icons/common/footer_logo.svg")
                        .left(400)
                        .top(400)
                        .opacity(1)
                    .build())
                .format(new ImageProcessClient.FormatParam("png")); // Available: jpeg, png, webp, tiff

        // Download image with a expired request url
        RequestHandler requestHandler = actionClient.getImageProgressExpiredUrlRequest(System.currentTimeMillis()/1000 + 1000);
        System.out.println("Image download url = " + requestHandler.getExpiresRequestUrl());
        // Download image with a network connection
        Bucket.ImageProcessOutput output = actionClient.imageProcess();
        try {
            InputStream bodyInputStream = output.getBodyInputStream();
            if (output.getStatueCode() >= 200 && output.getStatueCode() < 300) {
                // Success, save image
                File ff = new File("Your file kept path");
                OutputStream out = null;

                out = new FileOutputStream(ff);
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = bodyInputStream.read(buffer, 0, 1024)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                out.close();
            } else {
                // Handle error
                String jsonResult = inputStream2Json(bodyInputStream);
                OutputModel outputModel = new Gson().fromJson(jsonResult, OutputModel.class);
                outputModel.setStatueCode(imageProcessOutput.getStatueCode());

                System.out.println("StatueCode = " + outputModel.getStatueCode());
                System.out.println("Code = " + outputModel.getCode());
                System.out.println("RequestId = " + outputModel.getRequestId());
                System.out.println("Message = " + outputModel.getMessage());
                System.out.println("Url = " + outputModel.getUrl());

            }

            if (bodyInputStream != null) {
                bodyInputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
```

## Sign With Server
If you need sign with server, please use Client to get the RequestHandler, then do signture.

View [sign with server](./sign_with_server.md) for more information.

Next, take the 90 degree operation of the picture rotation as an example.

```java
    RequestHandler requestHandler1 = new ImageProcessClient("head.jpg", bucket)
        .rotate(new ImageProcessClient.RotateParam(90))
        .imageProgressRequest();// Rotate;
    String stringToSignature = requestHandler1.getStringToSignature();
    // ServerAuthorization. Get response from server. We just sign in local here.
    String serverAuthorization = QSSignatureUtil.generateSignature("secretKey",
        stringToSignature);
    requestHandler1.setSignature("your_access_key", serverAuthorization);
    requestHandler1.send();
```

## Appendix

The relevant methods and classes referenced of the above codes:

```java
    private String inputStream2Json(InputStream bodyInputStream) {
        StringBuilder result = new StringBuilder();
        try {
            if (bodyInputStream != null) {
                String readContent;
                BufferedReader reader = new BufferedReader(new InputStreamReader(bodyInputStream));
                while ((readContent = reader.readLine()) != null) {
                    result.append(readContent);
                    result.append(System.getProperty("line.separator"));
                }
                bodyInputStream.close();
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

```

```java
    class ImageInfoBean {

        /**
         * width : 1134
         * height : 1844
         * orientation : 0
         * alpha : true
         * type : png
         * space : srgb
         */

        private int width;
        private int height;
        private int orientation;
        private boolean alpha;
        private String type;
        private String space;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getOrientation() {
            return orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        public boolean isAlpha() {
            return alpha;
        }

        public void setAlpha(boolean alpha) {
            this.alpha = alpha;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSpace() {
            return space;
        }

        public void setSpace(String space) {
            this.space = space;
        }

    }

```