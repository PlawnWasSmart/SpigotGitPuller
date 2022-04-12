package me.plawn.gitpuller.commands;

import com.google.common.base.Throwables;
import me.plawn.gitpuller.GitPuller;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;

public class Pull implements CommandExecutor {

    private boolean available = true;

    private void handleError(CommandSender sender,Exception e){
        sender.sendMessage(ChatColor.RED+"An exception occurred while pulling the repository: \n"
                +ChatColor.DARK_RED + Throwables.getStackTraceAsString(e));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String pluginName = args[0];
        sender.sendMessage(ChatColor.GREEN+String.format("Updating %s",pluginName));
        if(!available){
            sender.sendMessage(ChatColor.RED+"Another pull task is running. Please try again later");
            return false;
        }
        if(GitPuller.getInstance().getPluginConfig().getPlugins().get(pluginName)==null){
            sender.sendMessage(String.format(ChatColor.RED+"Failed to pull %s. This plugin is not linked to any github repo",pluginName));
            return false;
        }


        new BukkitRunnable(){
            @Override
            public void run() {
                available = false;
                GitPuller.getInstance().setAllowRestart(false);
                long startTime = System.currentTimeMillis();
                try{
                    String repoUri = GitPuller.getInstance().getPluginConfig().getPlugins().get(pluginName);

                    //Cloning or pulling
                    String localRepoPath = GitPuller.getInstance().getDataFolder().getAbsolutePath() + "/cache/" + pluginName;
                    String token = GitPuller.getInstance().getPluginConfig().getGithubToken();
                    if(!new File(localRepoPath).exists()){
                        sender.sendMessage(ChatColor.GREEN+"Cloning repo");
                        Git.cloneRepository()
                                .setURI(repoUri)
                                .setDirectory(new File(localRepoPath))
                                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token",token))
                                .call();
                    } else{
                        sender.sendMessage(ChatColor.GREEN+"Pulling repo");
                        FileRepository localRepo = new FileRepository(localRepoPath+"/.git");
                        new Git(localRepo)
                                .pull()
                                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token",token))
                                .call();
                    }

                    //Building
                    sender.sendMessage(ChatColor.GREEN+"Building jar");
                    ProcessBuilder builder = new ProcessBuilder(
                            String.format(
                                    "%s\\maven\\apache-maven-3.8.5\\bin\\mvn.cmd",
                                    GitPuller.getInstance().getDataFolder().getAbsolutePath()),
                            "clean",
                            "package");
                    builder.directory(new File(localRepoPath));
                    builder.redirectOutput(new File(localRepoPath+"/build.log"));
                    Process p = builder.start();
                    while(p.isAlive()){
                    }
                    if(p.exitValue()==0){
                        sender.sendMessage(ChatColor.GREEN+"Build success!");
                    } else{
                        sender.sendMessage(String.format(ChatColor.RED+"Build failed (Exit code:%s)",p.exitValue()));
                    }

                    //Writing
                    sender.sendMessage(ChatColor.GREEN+"Writing jar");
                    File targetJar;
                    File newJar;

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    Document pomxml = dbf.newDocumentBuilder().parse(localRepoPath +"\\pom.xml");
                    Element root = pomxml.getDocumentElement();
                    newJar = new File(localRepoPath+"\\target\\"+
                            root.getElementsByTagName("artifactId").item(0).getTextContent()+
                            "-"+
                            root.getElementsByTagName("version").item(0).getTextContent()
                            +".jar");

                    if(GitPuller.getInstance().isDisabling()){
                        sender.sendMessage(ChatColor.RED+"Task cancelled. Server is stopping");
                        GitPuller.getInstance().setAllowRestart(true);
                        available = true;
                        return;
                    }
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                    if(plugin!=null){
                        Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
                        getFile.setAccessible(true);
                        targetJar = (File) getFile.invoke(plugin);
                    } else{
                        targetJar = new File("plugins\\"+newJar.getName());
                    }
                    if(!newJar.exists()){
                        sender.sendMessage(ChatColor.GOLD+"Jar not exist. Creating a new jar");
                        newJar.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(targetJar);
                    out.write(Files.readAllBytes(newJar.toPath()));
                    out.close();

                    //Success
                    sender.sendMessage(String.format(ChatColor.GREEN+"Success! Time elapsed: %sms",System.currentTimeMillis()-startTime));
                } catch(Exception e){
                    handleError(sender,e);
                }
                GitPuller.getInstance().setAllowRestart(true);
                available = true;
            }

        }.runTaskAsynchronously(GitPuller.getInstance());
        return true;
    }
}
