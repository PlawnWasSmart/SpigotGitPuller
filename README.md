# SpigotGitPuller
A spigot plugin that pull and complie plugin from github repo

**!!THIS PLUGIN IS STILL IN DEVELOPMENT. REPORT BUGS!!**


# Commands

Command | Description
--- | ---
`/pull <PluginName>` | Update plugin from github repo


# Config

```
github_token: <Your github access token> [Required if you need to access private repo]
plugins:
  <PluginName>: <GitURI>
  <PluginName>: <GitURI>
  <PluginName>: <GitURI>
```

# Permissions

Permission | Description
--- | ---
`gitpuller.pull` | Permission for the pull commanad
