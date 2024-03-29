package org.mcsg.survivalgames.commands;

import org.bukkit.entity.Player;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.stats.StatsWallManager;



public class SetStatsWall implements SubCommand {

    public boolean onCommand(Player player, String[] args) {
        StatsWallManager.getInstance().setStatsSignsFromSelection(player);
        return false;
    }

    
    public String help(Player p){
        return "/sg setstatswall - "+ SettingsManager.getInstance().getMessageConfig().getString("messages.help.setstatswall", "Sets the stats wall");
    }

	public String permission() {
		return null;
	}
}
