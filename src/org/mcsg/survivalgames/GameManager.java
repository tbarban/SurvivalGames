package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.Kit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class GameManager {

	static GameManager instance = new GameManager();
	private ArrayList < Game > games = new ArrayList < Game > ();
	private SurvivalGames p;
	public static HashMap < Integer, HashMap < Block, ItemStack[] >> openedChest = new HashMap < Integer, HashMap < Block, ItemStack[] >> ();
	private ArrayList<Kit>kits = new ArrayList<Kit>();
	private HashSet<Player>kitsel = new HashSet<Player>();
	MessageManager msgmgr = MessageManager.getInstance();

	private GameManager() {

	}

	public static GameManager getInstance() {
		return instance;
	}
	
	public static FireworkEffect getFireworkEffect(Color one, Color two, Color three, Color four, Color five, Type type) {
		return FireworkEffect.builder().flicker(false).withColor(one, two, three, four).withFade(five).with(type).trail(true).build();
	}

	public void setup(SurvivalGames plugin) {
		p = plugin;
		LoadGames();
		LoadKits();
		for (Game g: getGames()) {
			openedChest.put(g.getID(), new HashMap < Block, ItemStack[] > ());
		}
	}

	public Plugin getPlugin() {
		return p;
	}

	public void reloadGames() {
		LoadGames();
	}

	public static int getRandomNum(int max, int min) {
		Random rand = new Random();
	    int ii = min + rand.nextInt(((max - (min)) + 1));
	    return ii;
	}
	
	public static Color getRandomColor() {
		int color = getRandomNum(17, 1);
		switch (color) {
		case 1: return Color.AQUA;
		case 2: return Color.BLACK;
		case 3: return Color.BLUE;
		case 4: return Color.FUCHSIA;
		case 5: return Color.GRAY;
		case 6: return Color.GREEN;
		case 7: return Color.LIME;
		case 8: return Color.MAROON;
		case 9: return Color.NAVY;
		case 10: return Color.OLIVE;
		case 11: return Color.ORANGE;
		case 12: return Color.PURPLE;
		case 13: return Color.RED;
		case 14: return Color.SILVER;
		case 15: return Color.TEAL;
		case 16: return Color.WHITE;
		case 17: return Color.YELLOW;
		default: return Color.RED;
		}
	}
	
	public static Type getRandomType() {
		int type = getRandomNum(5, 1);
		switch (type) {
		case 1: return Type.STAR;
		case 2: return Type.CREEPER;
		case 3: return Type.BURST;
		case 4: return Type.BALL_LARGE;
		case 5: return Type.BALL;
		default: return Type.STAR;
		}
	}

	public void LoadKits(){
		Set<String> kits1 = SettingsManager.getInstance().getKits().getConfigurationSection("kits").getKeys(false);
		for(String s:kits1){
			kits.add(new Kit(s));
		}
	}
	
	public static void launchFireworkDisplay(final World w, final Location loc) {
		Firework fw = (Firework) w.spawn(loc.clone().add(new Vector(getRandomNum(5, -5), 1, getRandomNum(5, -5))), Firework.class);
		FireworkMeta meta = fw.getFireworkMeta();
		FireworkEffect effect = getFireworkEffect(getRandomColor(),getRandomColor(), getRandomColor(), getRandomColor(), getRandomColor(), getRandomType());
		meta.addEffect(effect);
		meta.setPower(getRandomNum(4, 1));
		fw.setFireworkMeta(meta);
	
	}
		

	public void LoadGames() {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		games.clear();
		int no = c.getInt("sg-system.arenano", 0);
		int loaded = 0;
		int a = 1;
		while (loaded < no) {
			if (c.isSet("sg-system.arenas." + a + ".x1")) {
				//c.set("sg-system.arenas."+a+".enabled",c.getBoolean("sg-system.arena."+a+".enabled", true));
				if (c.getBoolean("sg-system.arenas." + a + ".enabled")) {
					//SurvivalGames.$(c.getString("sg-system.arenas."+a+".enabled"));
					//c.set("sg-system.arenas."+a+".vip",c.getBoolean("sg-system.arenas."+a+".vip", false));
					SurvivalGames.$("Loading Arena: " + a);
					loaded++;
					games.add(new Game(a));
					StatsManager.getInstance().addArena(a);
				}
			}
			a++;
			
		}
		LobbyManager.getInstance().clearAllSigns();
		
	}

	public int getBlockGameId(Location v) {
		for (Game g: games) {
			if (g.isBlockInArena(v)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerGameId(Player p) {
		for (Game g: games) {
			if (g.isPlayerActive(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerSpectateId(Player p) {
		for (Game g: games) {
			if (g.isSpectator(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public boolean isPlayerActive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isPlayerInactive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpectator(Player player) {
		for (Game g: games) {
			if (g.isSpectator(player)) {
				return true;
			}
		}
		return false;
	}

	public void removeFromOtherQueues(Player p, int id) {
		for (Game g: getGames()) {
			if (g.isInQueue(p) && g.getID() != id) {
				g.removeFromQueue(p);
				msgmgr.sendMessage(PrefixType.INFO, "Removed from the queue in arena " + g.getID(), p);
			}
		}
	}

	public boolean isInKitMenu(Player p){
		return kitsel.contains(p);
	}

	public void leaveKitMenu(Player p){
		kitsel.remove(p);
	}

	public void openKitMenu(Player p){
		kitsel.add(p);
	}

	public void selectKit(Player p, int i) {
		p.getInventory().clear();
		ArrayList<Kit>kits = getKits(p);
		if(i <= kits.size()){
			Kit k = getKits(p).get(i);
			if(k!=null){
				p.getInventory().setContents(k.getContents().toArray(new ItemStack[0]));
			}
		}
		p.updateInventory();

	}

	public int getGameCount() {
		return games.size();
	}

	public Game getGame(int a) {
		//int t = gamemap.get(a);
		for (Game g: games) {
			if (g.getID() == a) {
				return g;
			}
		}
		return null;
	}

	public void removePlayer(Player p, boolean b) {
		getGame(getPlayerGameId(p)).removePlayer(p, b);
	}

	public void removeSpectator(Player p) {
		getGame(getPlayerSpectateId(p)).removeSpectator(p);
	}

	public void disableGame(int id) {
		getGame(id).disable();
	}

	public void enableGame(int id) {
		getGame(id).enable();
	}

	public ArrayList < Game > getGames() {
		return games;
	}

	public GameMode getGameMode(int a) {
		for (Game g: games) {
			if (g.getID() == a) {
				return g.getMode();
			}
		}
		return null;
	}

	public ArrayList<Kit> getKits(Player p){
		ArrayList<Kit>k = new ArrayList<Kit>();
		for(Kit kit: kits){
			if(kit.canUse(p)){
				k.add(kit);
			}
		}
		return k;
	}

	//TODO: Actually make this countdown correctly
	public void startGame(int a) {
		getGame(a).countdown(10);
	}

	public void addPlayer(Player p, int g) {
		Game game = getGame(g);
		if (game == null) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.input",p, "message-No game by this ID exist!");
			return;
		}
		getGame(g).addPlayer(p);
	}

	public void autoAddPlayer(Player pl) {
		ArrayList < Game > qg = new ArrayList < Game > (5);
		for (Game g: games) {
			if (g.getMode() == Game.GameMode.WAITING) qg.add(g);
		}
		//TODO: fancy auto balance algorithm
		if (qg.size() == 0) {
			pl.sendMessage(ChatColor.RED + "No games to join");
			msgmgr.sendMessage(PrefixType.WARNING, "No games to join!", pl);
			return;
		}
		qg.get(0).addPlayer(pl);
	}

	public WorldEditPlugin getWorldEdit() {
		return p.getWorldEdit();
	}

	public void createArenaFromSelection(Player pl) {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		//SettingsManager s = SettingsManager.getInstance();

		WorldEditPlugin we = p.getWorldEdit();
		Selection sel = we.getSelection(pl);
		if (sel == null) {
			msgmgr.sendMessage(PrefixType.WARNING, "You must make a WorldEdit Selection first!", pl);
			return;
		}
		Location max = sel.getMaximumPoint();
		Location min = sel.getMinimumPoint();

		/* if(max.getWorld()!=SettingsManager.getGameWorld() || min.getWorld()!=SettingsManager.getGameWorld()){
            pl.sendMessage(ChatColor.RED+"Wrong World!");
            return;
        }*/

		int no = c.getInt("sg-system.arenano") + 1;
		c.set("sg-system.arenano", no);
		if (games.size() == 0) {
			no = 1;
		} else no = games.get(games.size() - 1).getID() + 1;
		SettingsManager.getInstance().getSpawns().set(("spawns." + no), null);
		c.set("sg-system.arenas." + no + ".world", max.getWorld().getName());
		c.set("sg-system.arenas." + no + ".x1", max.getBlockX());
		c.set("sg-system.arenas." + no + ".y1", max.getBlockY());
		c.set("sg-system.arenas." + no + ".z1", max.getBlockZ());
		c.set("sg-system.arenas." + no + ".x2", min.getBlockX());
		c.set("sg-system.arenas." + no + ".y2", min.getBlockY());
		c.set("sg-system.arenas." + no + ".z2", min.getBlockZ());
		c.set("sg-system.arenas." + no + ".enabled", true);

		SettingsManager.getInstance().saveSystemConfig();
		hotAddArena(no);
		pl.sendMessage(ChatColor.GREEN + "Arena ID " + no + " Succesfully added");

	}

	private void hotAddArena(int no) {
		Game game = new Game(no);
		games.add(game);
		StatsManager.getInstance().addArena(no);
		//SurvivalGames.$("game added "+ games.size()+" "+SettingsManager.getInstance().getSystemConfig().getInt("gs-system.arenano"));
	}

	public void hotRemoveArena(int no) {
		for (Game g: games.toArray(new Game[0])) {
			if (g.getID() == no) {
				games.remove(getGame(no));
			}
		}
	}

	public void gameEndCallBack(int id) {
		// Chest reset is now done by a separate class
		//getGame(id).setRBStatus("clearing chest");
		//openedChest.put(id, new HashMap < Block, ItemStack[] > ());
	}

	public String getStringList(int gid){
		Game g = getGame(gid);
		StringBuilder sb = new StringBuilder();
		Player[][]players = g.getPlayers();

		sb.append(ChatColor.GREEN+"<---------------------[ Alive: "+players[0].length+" ]--------------------->\n"+ChatColor.GREEN+" ");
		for(Player p: players[0]){
			sb.append(p.getName()+",");
		}
		sb.append("\n\n");
		sb.append(ChatColor.RED+  "<---------------------[ Dead: "+players[1].length+" ]---------------------->\n"+ChatColor.GREEN+" ");
		for(Player p: players[1]){
			sb.append(p.getName()+",");
		}
		sb.append("\n\n");

		return sb.toString();
	}


}