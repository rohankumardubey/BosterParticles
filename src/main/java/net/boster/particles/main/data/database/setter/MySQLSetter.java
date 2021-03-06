package net.boster.particles.main.data.database.setter;

import net.boster.particles.main.BosterParticles;
import net.boster.particles.main.data.database.MySqlConnectionUtils;
import net.boster.particles.main.utils.LogType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class MySQLSetter implements DataSetter {

    @Override
    public void setUserData(String uuid, String w, String o) {
        MySqlConnectionUtils.setMySqlUserValue(uuid, w, o);
    }

    @Override
    public String getUserData(String uuid, String value) {
        return MySqlConnectionUtils.getMySqlValue(uuid, value);
    }

    @Override
    public FileConfiguration configuration(String uuid) {
        FileConfiguration c = new YamlConfiguration();
        String data = getUserData(uuid, "data");
        if(data != null) {
            try {
                c.loadFromString(decode(data));
            } catch (InvalidConfigurationException e) {
                BosterParticles.getInstance().log("Could not load " + uuid + "'s configuration!", LogType.ERROR);
            }
        }
        return c;
    }

    @Override
    public void save(String uuid, FileConfiguration file) {
        setUserData(uuid, "data", encode(file.saveToString()));
    }

    @Override
    public void deleteUser(String uuid) {
        MySqlConnectionUtils.deleteUser(uuid);
    }

    public static String encode(String s) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(s);
            outputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            return s;
        }
    }

    public static String decode(String s) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(s));
        try {
            BukkitObjectInputStream inputStream = new BukkitObjectInputStream(byteArrayInputStream);
            String r = inputStream.readObject().toString();
            inputStream.close();
            return r;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return s;
    }
}
