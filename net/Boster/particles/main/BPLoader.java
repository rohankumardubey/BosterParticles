package net.Boster.particles.main;

import net.Boster.particles.main.data.PlayerData;
import net.Boster.particles.main.data.database.DataConverter;
import net.Boster.particles.main.data.database.MySqlConnection;
import net.Boster.particles.main.data.database.setter.FileSetter;
import net.Boster.particles.main.data.database.setter.MySQLSetter;
import net.Boster.particles.main.files.MenuFile;
import net.Boster.particles.main.gui.ParticlesGUI;
import net.Boster.particles.main.utils.CustomTrails;
import net.Boster.particles.main.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.SQLException;

public class BPLoader {

    private final BosterParticles plugin;

    public boolean LoadPlayerTrailsAsync = true;

    public BPLoader(BosterParticles plugin) {
        this.plugin = plugin;
    }

    public void load() {
        loadFiles();
        loadMenus();
        loadDataSetter();
        DataConverter.load();
        CustomTrails.load();

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(PlayerData.get(p) == null) {
                        PlayerData.load(p);
                    }
                }
            }
        }.runTaskLater(plugin, 20);
    }

    public void loadFiles() {
        File f = new File(plugin.getDataFolder() + "/menus");
        if(!f.exists() || f.exists() && f.listFiles().length == 0) {
            plugin.saveResource("menus/particles.yml", false);
        }
        File nf = new File(plugin.getDataFolder() + "/menus");
        if(nf.listFiles().length > 0) {
            for(File fl : nf.listFiles()) {
                if(!fl.getName().endsWith(".yml")) continue;

                MenuFile file = new MenuFile(fl);
                file.create();
            }
        }
        LoadPlayerTrailsAsync = plugin.getConfig().getBoolean("Settings.LoadPlayerTrailsAsync", true);
    }

    private void loadDataSetter() {
        if(BosterParticles.getInstance().getConfig().getBoolean("MySql.Enabled")) {
            if(MySqlConnection.getConnection() != null) {
                try {
                    BosterParticles.getInstance().log("Closing connection to database", LogType.INFO);
                    MySqlConnection.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            String host = BosterParticles.getInstance().getConfig().getString("MySql.host");
            int port = BosterParticles.getInstance().getConfig().getInt("MySql.port");
            String user = BosterParticles.getInstance().getConfig().getString("MySql.user");
            String password = BosterParticles.getInstance().getConfig().getString("MySql.password");
            String db = BosterParticles.getInstance().getConfig().getString("MySql.database");

            if(host == null || user == null || password == null || db == null) {
                return;
            }

            MySqlConnection.connect(host, port, db, user, password);
            plugin.setDataSetter(new MySQLSetter());
        } else {
            plugin.setDataSetter(new FileSetter());
        }
    }

    public void loadMenus() {
        for(MenuFile file : MenuFile.files()) {
            new ParticlesGUI(file);
        }
    }

    public void loadPlayers() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerData.load(p);
        }
    }

    public void unLoadFiles() {
        MenuFile.clearAll();
    }

    public void unloadMenus() {
        ParticlesGUI.clearAll();
    }

    public void unloadPlayers() {
        PlayerData.clearAll();
    }

    public void reload() {
        unloadMenus();
        unLoadFiles();
        unloadPlayers();
        plugin.reloadConfig();
        loadFiles();
        loadMenus();
        loadDataSetter();
        DataConverter.load();
        CustomTrails.load();
        loadPlayers();
    }
}
