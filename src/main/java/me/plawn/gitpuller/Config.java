package me.plawn.gitpuller;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class Config {
    private HashMap<String,String> plugins = new HashMap<>();
    private String githubToken;

    public void load(){
        FileConfiguration config = GitPuller.getInstance().getConfig();

        config.options().copyDefaults(true);
        GitPuller.getInstance().saveConfig();

        if(config.getConfigurationSection("plugins")!=null){
            for(String key:config.getConfigurationSection("plugins").getKeys(false)){
                plugins.put(key,config.getConfigurationSection("plugins").getString(key));
            }
        }
        githubToken = config.getString("github_token");
    }

    public String getGithubToken() {
        return githubToken;
    }

    public HashMap<String, String> getPlugins() {
        return plugins;
    }
}
