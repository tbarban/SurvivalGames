package org.mcsg.survivalgames.util;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcsg.survivalgames.SurvivalGames;

public class ItemReader {

	
	private static HashMap<String, Enchantment>encids;
	

	
	private static void loadIds(){
		
		encids =  new HashMap<String, Enchantment>();
		
		for(Enchantment e:Enchantment.values()){
			encids.put(e.getName().toLowerCase().replace("_", ""), e);
			//encids.put(e.getName().toLowerCase(), e);
		}
		
		
		encids.put("sharpness", Enchantment.DAMAGE_ALL);
		encids.put("dmg", Enchantment.DAMAGE_ALL);
		encids.put("fire", Enchantment.FIRE_ASPECT);
		encids.put("punch", Enchantment.ARROW_DAMAGE);
		encids.put("looting", Enchantment.LOOT_BONUS_MOBS);

//		for( String ench: encids.keySet()) {
//			SurvivalGames.debug("Found enchantment: "+ench);
//		}
	}
	
	
	
	@SuppressWarnings("deprecation")
	public static ItemStack read(String str){
		int itemid = 0;
		if(encids == null){
			loadIds();
		}
		String split[] = str.split(",");
		SurvivalGames.debug("ItemReader: reading : "+Arrays.toString(split));
		for(int a = 0; a < split.length; a++){
			split[a] = split[a].toLowerCase().trim();
		}
		if(split.length < 1){
			SurvivalGames.warning("ItemReader: empty line");
			return null;
		}
		try {
			itemid = Integer.parseInt(split[0]);
		} 
		catch( Exception e ) {
			
			Material m = Material.matchMaterial(split[0]);
			if( m == null ) {
				SurvivalGames.error("ItemReader: invalid id "+split[0]);
				return null;
			}
			itemid = m.getId();
			SurvivalGames.debug("Item "+split[0]+" is ID "+itemid);
		}
		try {
			if(split.length == 1){
				return new ItemStack(itemid);
			}else if(split.length == 2){
				return new ItemStack(itemid, Integer.parseInt(split[1]));
			}else if(split.length == 3){
				return new ItemStack(itemid, Integer.parseInt(split[1]), Short.parseShort(split[2]));
			}else{
				ItemStack i =  new ItemStack(itemid, Integer.parseInt(split[1]), Short.parseShort(split[2]));
				String encs[] = split[3].split(" ");
				for(String enc: encs){
					SurvivalGames.debug(enc);
					String e[] = enc.split(":");
					e[0] = e[0].toLowerCase().replaceAll("_", "");
					if( encids.containsKey(e[0]) ) {
						try { 
							// this may produce an invalid item
							i.addUnsafeEnchantment(encids.get(e[0]), Integer.parseInt(e[1]));
						} catch(Exception e2) {
							SurvivalGames.warning("Unknown enchantment level '"+e[1]+"' on item: "+str);
						}
					} else {
						SurvivalGames.warning("Unknown enchantment '"+e[0]+"' on item: "+str);
					}
				}
				if(split.length == 5){
					ItemMeta im = i.getItemMeta();
					im.setDisplayName(MessageUtil.replaceColors(split[4]));
					i.setItemMeta(im);
				}
				return i;
			}
		} catch( Exception e ) {
			SurvivalGames.error("ItemReader: invalid item string: "+str);			
		}
		return null;
	}
	
	public static String getFriendlyItemName(Material m){
		String str = m.toString();
		str = str.replace('_',' ');
		str = str.substring(0, 1).toUpperCase() +
				str.substring(1).toLowerCase();
		return str;
	}
	
	
}
