package macaca.client.commands;

import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Status {

    private MacacaDriver driver;
    private Utils utils;

    public Status(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public String getStatus() throws Exception {
        return utils.getStatus(DriverCommand.STATUS);
    }
}
