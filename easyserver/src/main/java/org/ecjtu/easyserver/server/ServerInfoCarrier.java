package org.ecjtu.easyserver.server;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by KerriGan on 2017/7/21.
 */

public class ServerInfoCarrier implements Serializable {

    public DeviceInfo deviceInfo;

    public List<File> sharedFileList;

    public String ip;

    public String iconPath;

}
