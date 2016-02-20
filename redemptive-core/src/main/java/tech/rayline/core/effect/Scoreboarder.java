package tech.rayline.core.effect;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import tech.rayline.core.plugin.RedemptivePlugin;
import tech.rayline.core.util.RunnableShorthand;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This represents a "Scoreboard Type" which can be connected with a score boarding function which populates a scoreboard.
 *
 * This class is not to be extended, and simply instantiated with the function needed
 */
@Data
public final class Scoreboarder implements Runnable {
    private final static String OBJECTIVE = "obj" + ThreadLocalRandom.current().nextInt(10000);
    private final static int MAX_STRING_LENGTH = 64, MAX_ROWS = 15;

    private final RedemptivePlugin plugin;
    private final Action1<PlayerScoreboardState> scoreboardingFunction;
    private final Set<PlayerScoreboardState> scoreboardStates = new HashSet<>();
    private BukkitTask task;

    public void disable() {
        task.cancel();
        task = null;
    }

    public Scoreboarder enable() {
        if (task == null)
            task = RunnableShorthand.forPlugin(plugin).with(this).repeat(1);
        return this;
    }

    public void addPlayer(final Player player) {
        if (getStateFor(player).isPresent())
            return;

        Subscription subscribe = plugin
                .observeEvent(PlayerQuitEvent.class)
                .filter(new Func1<PlayerQuitEvent, Boolean>() {
                    @Override
                    public Boolean call(PlayerQuitEvent event) {
                        return event.getPlayer().equals(player);
                    }
                })
                .take(1)
                .subscribe(new Action1<PlayerQuitEvent>() {
                    @Override
                    public void call(PlayerQuitEvent event) {
                        Scoreboarder.this.removePlayer(player);
                    }
                });
        scoreboardStates.add(new PlayerScoreboardState(player, subscribe));
    }

    public Optional<PlayerScoreboardState> getStateFor(final Player player) {
        return scoreboardStates.stream().filter(new Predicate<PlayerScoreboardState>() {
            @Override
            public boolean test(PlayerScoreboardState state) {
                return state.getPlayer().equals(player);
            }
        }).findFirst();
    }

    public void removePlayer(Player player) {
        Optional<PlayerScoreboardState> stateFor = getStateFor(player);
        if (stateFor.isPresent()) {
            PlayerScoreboardState state = stateFor.get();
            scoreboardStates.remove(state);
            state.clear();
        }
    }

    @Override
    public void run() {
        scoreboardStates.forEach(new Consumer<PlayerScoreboardState>() {
            @Override
            public void accept(PlayerScoreboardState playerScoreboardState) {
                playerScoreboardState.update();
            }
        });
    }

    @Data
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode(of = {"player", "scoreboard"})
    public final class PlayerScoreboardState {
        private final Player player;
        private final Subscription playerQuitSubscription;
        private final BiMap<Integer, String> lines = HashBiMap.create();
        private final Scoreboard scoreboard;
        private final Objective scoreboardObjective;

        private String title;
        private int nullIndex = 0;
        private transient int internalCounter;

        public PlayerScoreboardState(Player player, Subscription playerQuitSubscription) {
            this.player = player;
            this.playerQuitSubscription = playerQuitSubscription;

            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
            scoreboardObjective = scoreboard.registerNewObjective(OBJECTIVE, "dummy");
            scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        private void update() {
            internalCounter = MAX_ROWS + 1;
            scoreboardingFunction.call(this);
            for (int i = --internalCounter; i >= 0; i--)
                removeLine(i);
        }

        public void removeLine(int x) {
            if (lines.containsKey(x))
                scoreboard.resetScores(lines.get(x));
            lines.remove(x);
        }

        public void set(int id, String text) {
            text = text.substring(0, Math.min(text.length(), MAX_STRING_LENGTH));
            while (text.endsWith("§")) text = text.substring(0, text.length()-1);
            if (lines.containsKey(id)) {
                if (lines.get(id).equals(text) || (ChatColor.stripColor(lines.get(id)).trim().equals("") && ChatColor.stripColor(text).trim().equals(""))) return;
                else removeLine(id);
            }
            if (lines.containsValue(text)) lines.inverse().remove(text);
            lines.put(id, text);
            scoreboardObjective.getScore(text).setScore(id);
        }

        public PlayerScoreboardState then(String message) {
            set(--internalCounter, message);
            return this;
        }

        public PlayerScoreboardState skipLine() {
            set(--internalCounter, nextNull());
            return this;
        }

        public PlayerScoreboardState setTitle(String title) {
            if (this.title != null && this.title.equals(title)) return this;
            this.title = title;
            scoreboardObjective.setDisplayName(title);
            player.setScoreboard(scoreboard);
            return this;
        }

        public String nextNull() {
            String s;
            do {
                nullIndex = (nullIndex + 1) % ChatColor.values().length;
                s = ChatColor.values()[nullIndex].toString();
            } while (lines.containsValue(s));
            return s;
        }

        public void clear() {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
