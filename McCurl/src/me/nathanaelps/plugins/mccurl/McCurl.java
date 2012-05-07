package me.nathanaelps.plugins.mccurl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class McCurl extends JavaPlugin {
	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static McCurl plugin;
	
	public void onEnable() {
    	pluginName = this.getDescription().getName();
    	pluginVersion = this.getDescription().getVersion();
    	server = this.getServer();
    	plugin = this;

    	this.getConfig();

        logInfo("Enabled!");
	}
	
	public void onDisable() {
        logInfo("Disabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("mccurlr")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (!player.hasPermission("mccurl.reload")) {return false;}
			}
			this.reloadConfig();
			logInfo("Config Reloaded!");
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("mccall")) {
			ConfigurationSection call = this.getConfig().getConfigurationSection("mccall."+args[0]);
			if(call==null){return false;}
			

			String playerName = "console";
			int playerX = 0;
			int playerY = 0;
			int playerZ = 0;
			String playerWorld = "0";
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				playerName = player.getDisplayName();
				playerX = player.getLocation().getBlockX();
				playerY = player.getLocation().getBlockY();
				playerZ = player.getLocation().getBlockZ();
				playerWorld = player.getLocation().getWorld().getName();
				String permission = call.getString("permission");
				if((permission != "none") && (permission != null)) {
					if (!player.hasPermission("mccurl.mccall."+permission)) {return false;}
				}
			}

			String playerData = "pname="+playerName+"&pworld="+playerWorld+"&px="+playerX+"&py="+playerY+"&pz="+playerZ;
			
			String url = call.getString("url");
			if(url==null){return false;}
			url = url +"?";
			

			List<String> parameters = call.getStringList("parameters");
			if(parameters == null){return false;}
	    	Iterator<String> keyIterator = parameters.iterator();
	    	
	    	int i = 1;
	    	while(keyIterator.hasNext()){
	    		String key = (String) keyIterator.next();
	    		if(i<args.length) {
	    			url = url + key + "=" + args[i] + "&";
	    			i++;
	    		} else {
	    			url = url + key + "=null&";
	    		}
	    	}
	    	
			url = url + playerData;
			
			
			new Thread(callUrlRunnable(url)).start();

			return true;
		}
		if(cmd.getName().equalsIgnoreCase("mccurl")) {
			if(args.length==0) {return false;}
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (!player.hasPermission("mccurl.use")) {return false;}
			}
			
			String argument = StringUtils.join(args,"%20");
			
			new Thread(callUrlRunnable(argument)).start();
			
			return true;// callUrl(argument);
		}
		return false;
	}
	
	private Runnable callUrlRunnable(String argument) {
		logInfo("Attempting to curl " + argument);
		URL url = null;
		HttpURLConnection connection = null;
		InputStream stream = null;
		try { url = new URL(argument);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logInfo("ERROR: Malformed URL! Does it include the the protocol?");
			}
		try { connection = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			logInfo("ERROR: 10 FAIL 20 GOTO 10. Don't know why. Send the report.");
			}
		try { connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			e.printStackTrace();
			logInfo("ERROR: Could not GET. Re-establish planetary alignment, then try again!");
			}
		try { connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
			logInfo("ERROR: Could not create connection! Don't know why!");
			}
		try { stream = connection.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			logInfo("ERROR: I don't know why, send the report. Is it a 404?");
			}
		try { stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			logInfo("ERROR: Could not close stream... which shouldn't matter, but it might if this is a recurring problem.");
			}
		return null;
	}
	
	private void logInfo(String input) {
        System.out.println("[" + this + "] " + input);		
	}
	@SuppressWarnings("unused")
	private void logInfo(int input) {
		String output = ((Integer) input).toString();
		logInfo(output);
	}
}
