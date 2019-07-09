package com.daxiang.android.action;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Created by jiangyitao.
 */
public class Click {

    private AndroidDriver driver;

    public Click(AndroidDriver driver) {
        this.driver = driver;
    }

    public void excute(Object id) throws Exception {
        String _id = (String) id;
        driver.findElement(By.id(_id)).click();
    }
}
