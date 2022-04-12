package me.plawn.gitpuller;

import me.plawn.gitpuller.commands.Pull;
import org.bukkit.plugin.java.JavaPlugin;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import java.io.File;
import java.io.IOException;

public final class GitPuller extends JavaPlugin {

    private static GitPuller instance;
    private Config pluginConfig;
    private boolean allowRestart = true;
    private boolean disabling = false;

    public GitPuller(){
        instance = this;
    }

    @Override
    public void onEnable() {
        pluginConfig = new Config();
        pluginConfig.load();
        this.getCommand("pull").setExecutor(new Pull());
        File mavenDir = new File(getDataFolder()+"/maven");
        if(!mavenDir.exists()||!mavenDir.isDirectory()){
            mavenDir.delete();
            System.out.println("Extracting maven");
            try {
                ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP).extract(getResource("apache-maven-3.8.5-bin.tar.gz"),mavenDir);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to extract maven");
                setEnabled(false);
            }
        }
    }

    @Override
    public void onDisable() {
        disabling = true;
        while(!allowRestart){
            System.out.println("Waiting for tasks to finish");
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Config getPluginConfig() {
        return pluginConfig;
    }

    public boolean isAllowRestart() {
        return allowRestart;
    }

    public void setAllowRestart(boolean allowRestart) {
        this.allowRestart = allowRestart;
    }

    public boolean isDisabling() {
        return disabling;
    }

    public static GitPuller getInstance() {
        return instance;
    }
}
