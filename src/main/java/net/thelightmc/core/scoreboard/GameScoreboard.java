package net.thelightmc.core.scoreboard;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class GameScoreboard {

    private Scoreboard scoreboard;
    private Objective objective;
    private BiMap<String, GameboardEntry> entries;

    private int teamId;

    public GameScoreboard(String title) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("spigobjective", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        setTitle(title);

        this.entries = HashBiMap.create();
        this.teamId = 1;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setTitle(String title) {
        objective.setDisplayName(title);
    }

    public BiMap<String, GameboardEntry> getEntries() {
        return HashBiMap.create(entries);
    }

    public GameboardEntry getEntry(String key) {
        return entries.get(key);
    }

    public GameboardEntry add(String name, int value) {
        return add((String) null, name, value, true);
    }

    public GameboardEntry add(Enum key, String name, int value) {
        return add(key.name(), name, value);
    }

    public GameboardEntry add(String key, String name, int value) {
        return add(key, name, value, false);
    }

    public GameboardEntry add(Enum key, String name, int value, boolean overwrite) {
        return add(key.name(), name, value, overwrite);
    }

    public GameboardEntry add(String key, String name, int value, boolean overwrite) {
        if (key == null && !contains(name)) {
            throw new IllegalArgumentException("Entry could not be found with the supplied name and no key was supplied");
        }

        if (overwrite && contains(name)) {
            GameboardEntry entry = getEntryByName(name);
            if (key != null && entries.get(key) != entry) {
                throw new IllegalArgumentException("Supplied key references a different score than the one to be overwritten");
            }

            entry.setValue(value);
            return entry;
        }

        if (entries.get(key) != null) {
            throw new IllegalArgumentException("Score already exists with that key");
        }

        int count = 0;
        String origName = name;
        if (!overwrite) {
            Map<Integer, String> created = create(name);
            for (Map.Entry<Integer, String> entry : created.entrySet()) {
                count = entry.getKey();
                name = entry.getValue();
            }
        }

        GameboardEntry entry = new GameboardEntry(key, this, value, origName, count);
        entry.update(name);
        entries.put(key, entry);
        return entry;
    }

    public void remove(String key) {
        remove(getEntry(key));
    }

    public void remove(GameboardEntry entry) {
        if (entry.getSpigboard() != this) {
            throw new IllegalArgumentException("Supplied entry does not belong to this Spigboard");
        }

        String key = entries.inverse().get(entry);
        if (key != null) {
            entries.remove(key);
        }

        entry.remove();
    }

    private Map<Integer, String> create(String name) {
        // Bukkit.getLogger().info("Name: '" + name + "' (" + name.length() + ") contains? " + contains(name));
        int count = 0;
        while (contains(name)) {
            name = ChatColor.RESET + name;
            count++;
            // Bukkit.getLogger().info("Name: '" + name + "' (" + name.length() + ") contains? " + contains(name) + " (" + ++count + ")");
        }

        if (name.length() > 48) {
            name = name.substring(0, 47);
            // Bukkit.getLogger().info("Name: '" + name + "' (" + name.length() + ") contains? " + contains(name) + " (trim)");
        }

        if (contains(name)) {
            throw new IllegalArgumentException("Could not find a suitable replacement name for '" + name + "'");
        }

        Map<Integer, String> created = new HashMap<>();
        created.put(count, name);
        return created;
    }

    public int getTeamId() {
        return teamId++;
    }

    public GameboardEntry getEntryByName(String name) {
        for (GameboardEntry entry : entries.values()) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }

        return null;
    }

    public boolean contains(String name) {
        for (GameboardEntry entry : entries.values()) {
            if (entry.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void add(Player player) {
        player.setScoreboard(scoreboard);
    }

}