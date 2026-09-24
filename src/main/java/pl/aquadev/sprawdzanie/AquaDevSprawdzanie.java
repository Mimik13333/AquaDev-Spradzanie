package pl.aquadev.sprawdzanie;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class AquaDevSprawdzanie extends JavaPlugin implements Listener {
 static class Check { UUID player,staff; long start; Check(UUID p,UUID s){player=p;staff=s;start=System.currentTimeMillis();} }
 final Map<UUID,Check> checks=new HashMap<>(); Location checker;
 public void onEnable(){saveDefaultConfig();load();getServer().getPluginManager().registerEvents(this,this);
  for(String c:new String[]{"aquadev-sprawdz","aquadev-ustawsprawdzarke","aquadev-gui","przyznajsie"}) Objects.requireNonNull(getCommand(c)).setExecutor(this);}
 public boolean onCommand(CommandSender s,Command c,String l,String[] a){
  if(!(s instanceof Player p))return true; String n=c.getName().toLowerCase();
  if(n.equals("aquadev-sprawdz")){
   if(!p.hasPermission("aquadev.admin")){p.sendMessage(m("no-permission"));return true;}
   if(a.length==1&&a[0].equalsIgnoreCase("lista")){list(p);return true;}
   if(a.length!=1){p.sendMessage(m("usage"));return true;}
   Player t=Bukkit.getPlayerExact(a[0]);if(t==null){p.sendMessage(m("not-found"));return true;}
   if(checks.containsKey(t.getUniqueId())){p.sendMessage(m("already"));return true;}
   checks.put(t.getUniqueId(),new Check(t.getUniqueId(),p.getUniqueId()));p.sendMessage(m("started","%player%",t.getName()));
   t.sendMessage(m("target","%staff%",p.getName()));if(checker!=null)t.teleport(checker);return true;
  }
  if(n.equals("aquadev-ustawsprawdzarke")){if(!p.hasPermission("aquadev.admin")){p.sendMessage(m("no-permission"));return true;}checker=p.getLocation();save();p.sendMessage(m("set"));return true;}
  if(n.equals("aquadev-gui")){if(!p.hasPermission("aquadev.admin")){p.sendMessage(m("no-permission"));return true;}gui(p);return true;}
  if(n.equals("przyznajsie")){Check x=checks.remove(p.getUniqueId());if(x==null){p.sendMessage(m("no-check"));return true;}ban(p,"3d",m("confess"));return true;}
  return true;
 }
 void list(Player p){p.sendMessage(m("list-header"));if(checks.isEmpty()){p.sendMessage(m("list-empty"));return;}for(Check x:checks.values()){Player a=Bukkit.getPlayer(x.player),b=Bukkit.getPlayer(x.staff);if(a!=null&&b!=null)p.sendMessage(m("list-line","%player%",a.getName(),"%staff%",b.getName(),"%time%",time(System.currentTimeMillis()-x.start)));}}
 String time(long ms){long sec=ms/1000;long min=sec/60;sec%=60;return min+" min "+sec+" sek";}
 void gui(Player p){Inventory i=Bukkit.createInventory(null,27,"§b§lAquaDev §8» §fSprawdzanie");fill(i);i.setItem(10,item(Material.LIME_WOOL,"&a✔ Czysty","&7Kończy sprawdzanie"));i.setItem(12,item(Material.RED_WOOL,"&c✘ Cheaty","&7Ban: &f7 dni"));i.setItem(14,item(Material.ORANGE_WOOL,"&6✘ Brak współpracy","&7Ban: &f14 dni"));i.setItem(16,item(Material.BLUE_WOOL,"&9✘ Przyznał się","&7Ban: &f3 dni"));p.openInventory(i);}
 @EventHandler public void click(InventoryClickEvent e){if(!e.getView().getTitle().contains("AquaDev"))return;e.setCancelled(true);if(!(e.getWhoClicked() instanceof Player p))return;int s=e.getRawSlot();if(s==10||s==12||s==14||s==16)select(p,s);}
 void select(Player staff,int slot){Player target=null;for(Check x:checks.values())if(x.staff.equals(staff.getUniqueId())){target=Bukkit.getPlayer(x.player);break;}if(target==null){staff.sendMessage(m("no-check"));return;}checks.remove(target.getUniqueId());if(slot==10){staff.sendMessage(m("clean","%player%",target.getName()));}else if(slot==12)ban(target,"7d",m("banned-cheats","%player%",target.getName()));else if(slot==14)ban(target,"14d",m("banned-coop","%player%",target.getName()));else ban(target,"3d",m("banned-confess","%player%",target.getName()));staff.closeInventory();}
 void ban(Player p,String dur,String message){p.kickPlayer(message+"\n\n§7Czas bana: §f"+dur);Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(),message,new Date(System.currentTimeMillis()+duration(dur)), "AquaDev-Spradzanie");}
 long duration(String d){return Long.parseLong(d.replace("d",""))*86400000L;}
 @EventHandler public void quit(PlayerQuitEvent e){Check x=checks.remove(e.getPlayer().getUniqueId());if(x!=null){Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(e.getPlayer().getName(),"Wylogowanie podczas sprawdzania — ban 14 dni",new Date(System.currentTimeMillis()+14*86400000L),"AquaDev-Spradzanie");}}
 @EventHandler public void cmd(PlayerCommandPreprocessEvent e){Check x=checks.get(e.getPlayer().getUniqueId());if(x==null)return;String q=e.getMessage().split(" ")[0].toLowerCase();if(!q.equals("/mag")&&!q.equals("/help")&&!q.equals("/msg")&&!q.equals("/tell")&&!q.equals("/w")&&!q.equals("/whisper")&&!q.equals("/gamma")&&!q.equals("/przyznajsię")&&!q.equals("/przyznajsie"))e.setCancelled(true);}
 @EventHandler public void breakBlock(BlockBreakEvent e){
  if(checks.containsKey(e.getPlayer().getUniqueId())) e.setCancelled(true);
 }
 @EventHandler public void placeBlock(BlockPlaceEvent e){
  if(checks.containsKey(e.getPlayer().getUniqueId())) e.setCancelled(true);
 }
 @EventHandler public void interact(PlayerInteractEvent e){
  if(checks.containsKey(e.getPlayer().getUniqueId())){
   // During checking, prevent interactions that could change the world.
   switch(e.getAction()){
    case RIGHT_CLICK_BLOCK, LEFT_CLICK_BLOCK -> {
     if(e.getClickedBlock()!=null && e.getClickedBlock().getType().isInteractable()) e.setCancelled(true);
    }
    default -> {}
   }
  }
 }
 void fill(Inventory i){ItemStack x=item(Material.GRAY_STAINED_GLASS_PANE," ");for(int z=0;z<i.getSize();z++)i.setItem(z,x);}
 ItemStack item(Material mat,String name,String... lore){ItemStack i=new ItemStack(mat);ItemMeta m=i.getItemMeta();m.setDisplayName(ChatColor.translateAlternateColorCodes('&',name));i.setItemMeta(m);return i;}
 String m(String k,String...r){String x=ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages."+k,""));for(int i=0;i+1<r.length;i+=2)x=x.replace(r[i],r[i+1]);return x;}
 void save(){getConfig().set("checker.world",checker.getWorld().getName());getConfig().set("checker.x",checker.getX());getConfig().set("checker.y",checker.getY());getConfig().set("checker.z",checker.getZ());saveConfig();}
 void load(){if(getConfig().getString("checker.world")!=null){World w=Bukkit.getWorld(getConfig().getString("checker.world"));if(w!=null)checker=new Location(w,getConfig().getDouble("checker.x"),getConfig().getDouble("checker.y"),getConfig().getDouble("checker.z"));}}
}
