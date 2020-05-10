package com.daxiang.core.mobile.ios;


import com.android.ddmlib.*;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.log.LogReceiver;
import com.android.sdklib.AndroidVersion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
public class IosIDevice implements IDevice {

    private String mobileId;
    private boolean isRealMobile;

    public IosIDevice(String mobileId, boolean isRealMobile) {
        this.mobileId = mobileId;
        this.isRealMobile = isRealMobile;
    }

    @Override
    public String getSerialNumber() {
        return mobileId;
    }

    @Override
    public String getAvdName() {
        return null;
    }

    @Override
    public DeviceState getState() {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 0;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public boolean arePropertiesSet() {
        return false;
    }

    @Override
    public String getPropertySync(String name) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return null;
    }

    @Override
    public String getPropertyCacheOrSync(String name) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return null;
    }

    @Override
    public boolean supportsFeature(Feature feature) {
        return false;
    }

    @Override
    public boolean supportsFeature(HardwareFeature feature) {
        return false;
    }

    @Override
    public String getMountPoint(String name) {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isEmulator() {
        return !isRealMobile;
    }

    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public boolean isBootLoader() {
        return false;
    }

    @Override
    public boolean hasClients() {
        return false;
    }

    @Override
    public Client[] getClients() {
        return new Client[0];
    }

    @Override
    public Client getClient(String applicationName) {
        return null;
    }

    @Override
    public SyncService getSyncService() throws TimeoutException, AdbCommandRejectedException, IOException {
        return null;
    }

    @Override
    public FileListingService getFileListingService() {
        return null;
    }

    @Override
    public RawImage getScreenshot() throws TimeoutException, AdbCommandRejectedException, IOException {
        return null;
    }

    @Override
    public RawImage getScreenshot(long timeout, TimeUnit unit) throws TimeoutException, AdbCommandRejectedException, IOException {
        return null;
    }

    @Override
    public void startScreenRecorder(String remoteFilePath, ScreenRecorderOptions options, IShellOutputReceiver receiver) throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {

    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver, int maxTimeToOutputResponse) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {

    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {

    }

    @Override
    public void runEventLogService(LogReceiver receiver) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public void runLogService(String logname, LogReceiver receiver) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public void createForward(int localPort, int remotePort) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public void createForward(int localPort, String remoteSocketName, DeviceUnixSocketNamespace namespace) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public void removeForward(int localPort, int remotePort) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public void removeForward(int localPort, String remoteSocketName, DeviceUnixSocketNamespace namespace) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public String getClientName(int pid) {
        return null;
    }

    @Override
    public void pushFile(String local, String remote) throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {

    }

    @Override
    public void pullFile(String remote, String local) throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {

    }

    @Override
    public void installPackage(String packageFilePath, boolean reinstall, String... extraArgs) throws InstallException {

    }

    @Override
    public void installPackages(List<File> apks, boolean reinstall, List<String> installOptions, long timeout, TimeUnit timeoutUnit) throws InstallException {

    }

    @Override
    public String syncPackageToDevice(String localFilePath) throws TimeoutException, AdbCommandRejectedException, IOException, SyncException {
        return null;
    }

    @Override
    public void installRemotePackage(String remoteFilePath, boolean reinstall, String... extraArgs) throws InstallException {

    }

    @Override
    public void removeRemotePackage(String remoteFilePath) throws InstallException {

    }

    @Override
    public String uninstallPackage(String packageName) throws InstallException {
        return null;
    }

    @Override
    public void reboot(String into) throws TimeoutException, AdbCommandRejectedException, IOException {

    }

    @Override
    public boolean root() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return false;
    }

    @Override
    public boolean isRoot() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return false;
    }

    @Override
    public Integer getBatteryLevel() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return null;
    }

    @Override
    public Integer getBatteryLevel(long freshnessMs) throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return null;
    }

    @Override
    public Future<Integer> getBattery() {
        return null;
    }

    @Override
    public Future<Integer> getBattery(long freshnessTime, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public List<String> getAbis() {
        return null;
    }

    @Override
    public int getDensity() {
        return 0;
    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public AndroidVersion getVersion() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver, long maxTimeToOutputResponse, TimeUnit maxTimeUnits) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {

    }

    @Override
    public Future<String> getSystemProperty(String name) {
        return null;
    }
}
