// +-------------------------------------------------------------------------
// | Copyright (C) 2016 Yunify, Inc.
// +-------------------------------------------------------------------------
// | Licensed under the Apache License, Version 2.0 (the "License");
// | you may not use this work except in compliance with the License.
// | You may obtain a copy of the License in the LICENSE file, or at:
// |
// | http://www.apache.org/licenses/LICENSE-2.0
// |
// | Unless required by applicable law or agreed to in writing, software
// | distributed under the License is distributed on an "AS IS" BASIS,
// | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// | See the License for the specific language governing permissions and
// | limitations under the License.
// +-------------------------------------------------------------------------

package com.qingstor.sdk.config;

import com.qingstor.sdk.constants.QSConstant;
import com.qingstor.sdk.exception.QSException;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class EvnContextTest {

    @Test
    public void testDefault() {
        EvnContext evnContext = new EvnContext("testkey", "test_asss");
        evnContext.setAdditionalUserAgent("\"");
        Assert.assertEquals(evnContext.getAccessKey(), "testkey");
        Assert.assertEquals(evnContext.getAccessSecret(), "test_asss");
        Assert.assertEquals(evnContext.getRequestUrl(), "https://qingstor.com");
        Assert.assertEquals(evnContext.getLog_level(), QSConstant.LOGGER_ERROR);
        String validate = evnContext.validateParam();
        Assert.assertNotNull(validate);
    }

    @Test
    public void testConfig() throws QSException {
        String config =
                "access_key_id: 'testkey'\n"
                        + "secret_access_key: 'test_asss'\n"
                        + "additional_user_agent: 'test/integration'\n"
                        + "host: qingcloud.com\n"
                        + "port: 443\n"
                        + "protocol: https\n";
        File f = new File("/tmp/config.yaml");
        boolean bConf = false;
        try {
            OutputStream output = new FileOutputStream(f);
            output.write(config.getBytes());
            output.close();
            bConf = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bConf) {

            EvnContext evnContext = EvnContext.loadFromFile("/tmp/config.yaml");
            Assert.assertEquals(evnContext.getAccessKey(), "testkey");
            Assert.assertEquals(evnContext.getAccessSecret(), "test_asss");
            Assert.assertEquals(evnContext.getRequestUrl(), "https://qingcloud.com:443");
            Assert.assertEquals(evnContext.getAdditionalUserAgent(), "test/integration");
        }
    }
}
