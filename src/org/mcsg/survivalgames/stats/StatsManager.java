package org.mcsg.survivalgames.stats;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.util.DatabaseManager;



public class StatsManager {


    private static StatsManager instance = new StatsManager();
    
    private ArrayList<PreparedStatement> queue = new ArrayList<PreparedStatement>();
    public ArrayList<PreparedStatement> toRun = new ArrayList<PreparedStatement>();
    private DatabaseDumper dumper = new DatabaseDumper();
    private DatabaseManager dbman = DatabaseManager.getInstance();
    
    //private HashMap<Player, PlayerStatsSession>pstats = new HashMap<Player, PlayerStatsSession>();
    
    private HashMap<Integer, HashMap<Player, PlayerStatsSession>>arenas  = new HashMap<Integer, HashMap<Player, PlayerStatsSession>>();
    
    
    private boolean enabled = true;
    
    MessageManager msgmgr;

    private StatsManager(){
    	msgmgr = MessageManager.getInstance();;
    }

    public static StatsManager getInstance(){
        return instance;
    }

    public void setup(Plugin p, boolean b){
    	enabled = b;
    	String playerQuery = "CREATE TABLE sg_playerstats(id int NOT NULL AUTO_INCREMENT PRIMARY KEY, player text, points int, wins int, kills int, death int)";
    	if (b) {
    		try {
    			PreparedStatement setup = dbman.createStatement(playerQuery);
    			
    			DatabaseMetaData dbm = dbman.getMysqlConnection().getMetaData();
    			ResultSet tables = dbm.getTables(null, null, "sg_playerstats", null);
    			
    			if (!tables.next()) {
    				setup.execute();
    			}
	    	} catch (Exception except) {
	    		except.printStackTrace();
	    	}
    	}
    }
    	
    	
    	
      /*  enabled = b;
        if(b){
            try{
                PreparedStatement s = dbman.createStatement(" CREATE TABLE "+SettingsManager.getSqlPrefix() + 
                        "playerstats(id int NOT NULL AUTO_INCREMENT PRIMARY KEY, gameno int,arenaid int, player text, points int,position int," +
                        " kills int, death int, killed text,time int, ks1 int, ks2 int,ks3 int, ks4 int, ks5 int)");

                PreparedStatement s1 = dbman.createStatement(" CREATE TABLE "+SettingsManager.getSqlPrefix() + 
                        "gamestats(gameno int NOT NULL AUTO_INCREMENT PRIMARY KEY, arenaid int, players int, winner text, time int )");


                DatabaseMetaData dbm = dbman.getMysqlConnection().getMetaData();
                ResultSet tables = dbm.getTables(null, null, SettingsManager.getSqlPrefix()+"playerstats", null);
                ResultSet tables1 = dbm.getTables(null, null, SettingsManager.getSqlPrefix()+"gamestats", null);

                if (tables.next()) { }
                else {
                    s.execute();
                }
                if (tables1.next()) { }
                else {
                    s1.execute();
                }
            }catch(Exception e){e.printStackTrace();}

        }*/
    

    public void addArena(int arenaid){
        arenas.put(arenaid, new HashMap<Player, PlayerStatsSession>());
    }



    public void addPlayer(Player p, int arenaid){
        arenas.get(arenaid).put(p, new PlayerStatsSession(p, arenaid));
    }

    public void removePlayer(Player p, int id){
        arenas.get(id).remove(p);
    }

    public void playerDied(Player p, int pos, int arenaid,long time){
        /*    System.out.println("player null "+(p == null));
        System.out.println("arena null "+(arenas == null));
        System.out.println("arenagetplayer null "+(arenas.get(arenaid).get(p) == null));*/

        arenas.get(arenaid).get(p).died(pos, time);
    }

    public void playerWin(Player p, int arenaid, long time){
        arenas.get(arenaid).get(p).win(time);
    }


    public void addKill(Player p, Player killed, int arenaid){
        PlayerStatsSession s = arenas.get(arenaid).get(p);

        int kslevel = s.addKill(killed);
        if(kslevel > 3){
        	msgmgr.broadcastFMessage(PrefixType.INFO, "killstreak.level"+((kslevel>5)?5:kslevel), "player-"+p.getName());
        }
        else if(kslevel > 0){
            for (Player pl : GameManager.getInstance().getGame(arenaid).getAllPlayers()) {
            	msgmgr.sendFMessage(PrefixType.INFO, "killstreak.level"+((kslevel>5)?5:kslevel), pl, "player-"+p.getName());
            }
        }
    }
   
    
    @SuppressWarnings("unused")
	public void saveGame(int arenaID, Player winner, int playerCount, long gameLength) {
    	if (enabled) {
    		int gameNum = 0;
    		Game game = GameManager.getInstance().getGame(arenaID);    		
    		try {
    			String gameQuery = "SELECT * FROM `sg_gamestats` ORDER BY `gameno` DESC LIMIT 1";
        		String insertQuery = "INSERT INTO `sg_gamestats` VALUES(NULL, " + arenaID + ", " + playerCount + ", " + winner.getName() + ", " + gameLength + ")";

    			long respondTime = new Date().getTime();
    			PreparedStatement statement = dbman.createStatement(gameQuery);
    			
    			ResultSet results = statement.executeQuery();
    			results.next();
    			
    			//gameNum = results.getInt(1) + 1;
    			if (respondTime + 5000 < new Date().getTime()) {
    				System.out.println("Database timeout. Check connection between server and database.");
    				
    			}
    			PreparedStatement toInsert = dbman.createStatement(insertQuery);
    			addSQL(toInsert);
    			
    		} catch (SQLException except) {
    			except.printStackTrace();
    			game.setRBStatus("Error: getno");
    		}
    		
    		
    		
    		for (PlayerStatsSession stats:arenas.get(arenaID).values()) {
    			String pName = stats.getPlayerName();
    			String search = "SELECT * FROM `sg_playerstats` WHERE `player` = '" + pName + "'";

    			try {
	    			PreparedStatement searchPlayer = dbman.createStatement(search);
	    			ResultSet searchResult = searchPlayer.executeQuery();

	    			if (searchResult.next()) {
	    				int points = searchResult.getInt("points") + stats.getPoints();
	    				int kills = searchResult.getInt("kills") + stats.getKills();
	    				int deaths = searchResult.getInt("death") + stats.getDeaths();
	    				int wins = searchResult.getInt("wins");
	    				if (stats.getPosition() == 1) {
	    					wins++;
	    				}
	    				String queryPoints = "UPDATE `sg_playerstats` SET `points`=" + points + ",";
	    				String queryWins = "`wins`=" + wins + ",";
	    				String queryKills = "`kills`=" + kills + ",";
	    				String queryDeaths = "`death`=" + deaths + " ";
	    				String queryPlayer = "WHERE `player`='" + pName + "'";
	    				String querySend = queryPoints + queryWins + queryKills + queryDeaths + queryPlayer;
	    				
	    				PreparedStatement toSend = dbman.createStatement(querySend);
	    				addSQL(toSend);
	    				

	    				
	    			} else {
	    				int points = stats.getPoints();
	    				int kills = stats.getKills();
	    				int deaths = stats.getDeaths();
	    				int wins = 0;
	    				if (stats.getPosition() == 1) {
	    					wins++;
	    				}
	    				
	    				String queryInsert = "INSERT INTO `sg_playerstats` (`player`, `points`, `wins`, `kills`, `death`) ";
	    				String queryValues = "VALUES(\"" + pName + "\"," + points + "," + wins + "," + kills + "," + deaths + ")";
	    				String querySend = queryInsert + queryValues;

	    				PreparedStatement toSend = dbman.createStatement(querySend);
	    				addSQL(toSend);
	    				
	    			}
	    			
    			} catch (SQLException except) {
    				except.printStackTrace();
    			}
    			
    		}
    		
    		arenas.get(arenaID).clear();
    	}
    }

    private void addSQL(PreparedStatement s){
        queue.add(s);
        if(!dumper.isAlive()){
            dumper = new DatabaseDumper();
            dumper.start();
        }
    }
    
    class DatabaseDumper extends Thread {

        public void run(){
            while(queue.size()>0){
                PreparedStatement s = queue.remove(0);
                try{
                    s.executeUpdate();
                    s.close();
                }catch(Exception e){     dbman.connect();}
            }
        }
    }
}